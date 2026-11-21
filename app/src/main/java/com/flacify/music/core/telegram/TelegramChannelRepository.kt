package com.flacify.music.core.telegram

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterIsInstance
import org.drinkless.tdlib.TdApi
import java.io.IOException
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

private val usernamePattern = Regex("[A-Za-z][A-Za-z0-9_]{0,31}")

internal fun channelUsername(input: String): String {
    val text = input.trim()
    val username = if (text.contains('/')) {
        val uri = runCatching { URI(if (text.startsWith("t.me/", true)) "https://$text" else text) }.getOrNull()
        require(uri != null && uri.scheme.equals("https", true) && uri.host.equals("t.me", true) &&
            uri.userInfo == null && uri.port == -1) { "Enter a public @username or t.me/channel link." }
        uri.path.trim('/')
    } else text.removePrefix("@")
    require(usernamePattern.matches(username)) { "Enter a public @username or t.me/channel link." }
    return username
}

@Singleton
class TelegramChannelRepository @Inject constructor(
    private val client: TelegramClientManager,
    val dao: TelegramDao
) {
    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            client.updates.filterIsInstance<TdApi.UpdateFile>().collect { update ->
                val file = update.file
                val account = (client.auth.value as? TelegramAuthState.Ready)?.accountId
                if (account != null && file.local.isDownloadingCompleted && file.remote.uniqueId.isNotEmpty()) {
                    dao.thumbnailReady(account, file.remote.uniqueId, file.local.path)
                }
            }
        }
    }

    suspend fun getUserChannels() {
        val account = client.account()
        for (list in listOf(TdApi.ChatListMain(), TdApi.ChatListArchive())) {
            var limit = 100
            var seen = emptySet<Long>()
            while (true) {
                val ids = client.send(TdApi.GetChats(list, limit)).chatIds.toSet()
                for (batch in (ids - seen).chunked(12)) {
                    val channels = coroutineScope {
                        batch.map { id -> async { channel(account, client.send(TdApi.GetChat(id))) } }.awaitAll().filterNotNull()
                    }
                    requireAccount(account)
                    dao.saveChannels(channels)
                }
                seen = ids
                try {
                    client.send(TdApi.LoadChats(list, 100))
                } catch (e: TelegramError) {
                    if (e.code != 404) throw e
                    if (ids.size < limit) break
                }
                limit += 100
            }
        }
    }

    suspend fun resolvePublicChannel(input: String): TelegramChannel {
        val account = client.account()
        val chat = client.send(TdApi.SearchPublicChat(channelUsername(input)))
        val channel = channel(account, chat) ?: throw IOException("That username belongs to a user or group. Choose a channel.")
        requireAccount(account)
        dao.saveChannels(listOf(channel))
        return channel
    }

    private fun channel(account: Long, chat: TdApi.Chat) =
        if ((chat.type as? TdApi.ChatTypeSupergroup)?.isChannel == true) TelegramChannel(account, chat.id, chat.title) else null

    private fun requireAccount(account: Long) {
        if (client.account() != account) throw IOException("Telegram account changed.")
    }

    suspend fun page(channel: TelegramChannel, cursor: Long): Long {
        requireAccount(channel.accountId)
        val page = client.send(TdApi.SearchChatMessages(channel.chatId, null, "", null, cursor, 0, 50, TdApi.SearchMessagesFilterAudio()))
        val covers = mutableListOf<TdApi.File>()
        val tracks = page.messages.mapNotNull { message ->
            val audio = (message.content as? TdApi.MessageAudio)?.audio ?: return@mapNotNull null
            val cover = audio.albumCoverThumbnail?.file
            if (cover != null && !cover.local.isDownloadingCompleted) covers.add(cover)
            TelegramTrackEntity(channel.accountId, channel.chatId, message.id, audio.audio.id,
                audio.title.ifBlank { audio.fileName }, audio.performer.ifBlank { "Unknown artist" }, audio.fileName,
                audio.duration * 1000L, audio.audio.size, audio.mimeType,
                cover?.local?.takeIf { it.isDownloadingCompleted }?.path, message.date.toLong(),
                cover?.remote?.uniqueId)
        }
        requireAccount(channel.accountId)
        dao.saveTracks(tracks)
        for (cover in covers.distinctBy { it.id }) {
            try {
                val file = client.send(TdApi.DownloadFile(cover.id, 4, 0, 0, false))
                if (file.local.isDownloadingCompleted) dao.thumbnailReady(channel.accountId, file.remote.uniqueId, file.local.path)
            } catch (_: TelegramError) {
                // A removed thumbnail does not invalidate the page of audio metadata.
            }
        }
        return page.nextFromMessageId.takeUnless { it == cursor } ?: 0
    }

    suspend fun audio(key: MessageKey): TdApi.Audio {
        val account = client.account()
        if (!dao.indexed(account, key.chatId, key.messageId)) throw IOException("Select this track from a connected channel first.")
        val message = client.send(TdApi.GetMessage(key.chatId, key.messageId))
        val audio = (message.content as? TdApi.MessageAudio)?.audio ?: throw IOException("This message no longer contains audio.")
        requireAccount(account)
        dao.refreshFileId(account, key.chatId, key.messageId, audio.audio.id)
        return audio
    }
}
