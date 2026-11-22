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

data class TelegramPageCursor(val audio: Long = 0, val documents: Long = 0) {
    val hasMore get() = audio >= 0 || documents >= 0
}

internal fun telegramAudio(content: TdApi.MessageContent): TdApi.Audio? = when (content) {
    is TdApi.MessageAudio -> content.audio
    is TdApi.MessageDocument -> content.document.let { document ->
        val extension = document.fileName.substringAfterLast('.', "").lowercase()
        if (document.mimeType.startsWith("audio/") || extension in setOf("flac", "mp3", "m4a", "aac", "ogg", "opus", "wav", "alac", "aiff")) {
            TdApi.Audio().apply {
                audio = document.document
                fileName = document.fileName
                title = document.fileName.substringBeforeLast('.')
                performer = ""
                mimeType = if (document.mimeType.startsWith("audio/")) document.mimeType else when (extension) {
                    "flac" -> "audio/flac"
                    "wav" -> "audio/wav"
                    "mp3" -> "audio/mpeg"
                    "m4a", "alac" -> "audio/mp4"
                    "aiff" -> "audio/aiff"
                    else -> "audio/$extension"
                }
                albumCoverThumbnail = document.thumbnail
                albumCoverMinithumbnail = document.minithumbnail
            }
        } else null
    }
    else -> null
}

@Singleton
class TelegramChannelRepository @Inject constructor(
    private val client: TelegramClientManager,
    val dao: TelegramDao,
    private val artwork: TelegramArtwork
) {
    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            client.updates.filterIsInstance<TdApi.UpdateFile>().collect { update ->
                val file = update.file
                val account = (client.auth.value as? TelegramAuthState.Ready)?.accountId
                if (account != null && file.local.isDownloadingCompleted && file.remote.uniqueId.isNotEmpty()) {
                    dao.thumbnailReady(account, file.remote.uniqueId, file.local.path)
                }
                if (account != null && file.local.isDownloadingCompleted) {
                    for (key in dao.keysForFile(account, file.id)) {
                        try {
                            val message = client.send(TdApi.GetMessageLocally(key.chatId, key.messageId))
                            telegramAudio(message.content)?.let { artwork.load(account, key, it) }
                        } catch (_: TelegramError) {
                            // A removed message does not stop processing subsequent artwork.
                        }
                    }
                }
            }
        }
    }

    suspend fun getUserChannels() {
        val account = client.account()
        val saved = client.send(TdApi.CreatePrivateChat(account, false))
        dao.saveChannels(listOf(TelegramChannel(account, saved.id, "Saved Messages")))
        for (list in listOf(TdApi.ChatListMain(), TdApi.ChatListArchive())) {
            var limit = 100
            var seen = emptySet<Long>()
            while (true) {
                val finished = try {
                    client.send(TdApi.LoadChats(list, 100))
                    false
                } catch (e: TelegramError) {
                    if (e.code != 404) throw e
                    true
                }
                val ids = client.send(TdApi.GetChats(list, limit)).chatIds.toSet()
                for (batch in (ids - seen).chunked(12)) {
                    val channels = coroutineScope {
                        batch.map { id -> async { channel(account, client.send(TdApi.GetChat(id))) } }.awaitAll().filterNotNull()
                    }
                    requireAccount(account)
                    dao.saveChannels(channels)
                }
                seen = ids
                if (finished && ids.size < limit) break
                limit += 100
            }
        }
    }

    suspend fun resolvePublicChannel(input: String): TelegramChannel {
        val account = client.account()
        val chat = client.send(TdApi.SearchPublicChat(channelUsername(input)))
        val channel = channel(account, chat) ?: throw IOException("Choose a channel or group that contains music.")
        requireAccount(account)
        dao.saveChannels(listOf(channel))
        return channel
    }

    private fun channel(account: Long, chat: TdApi.Chat) =
        if (chat.type is TdApi.ChatTypeSupergroup || chat.type is TdApi.ChatTypeBasicGroup) TelegramChannel(account, chat.id, chat.title) else null

    private fun requireAccount(account: Long) {
        if (client.account() != account) throw IOException("Telegram account changed.")
    }

    suspend fun page(channel: TelegramChannel, cursor: TelegramPageCursor): TelegramPageCursor {
        val audio = page(channel, cursor.audio, TdApi.SearchMessagesFilterAudio())
        val documents = page(channel, cursor.documents, TdApi.SearchMessagesFilterDocument())
        return TelegramPageCursor(audio, documents)
    }

    private suspend fun page(channel: TelegramChannel, cursor: Long, filter: TdApi.SearchMessagesFilter): Long {
        if (cursor < 0) return -1
        requireAccount(channel.accountId)
        val page = client.send(TdApi.SearchChatMessages(channel.chatId, null, "", null, cursor, 0, 50, filter))
        val covers = mutableListOf<Pair<MessageKey, TdApi.Audio>>()
        val tracks = page.messages.mapNotNull { message ->
            val audio = telegramAudio(message.content) ?: return@mapNotNull null
            val cover = audio.albumCoverThumbnail?.file
            covers.add(MessageKey(channel.chatId, message.id) to audio)
            TelegramTrackEntity(channel.accountId, channel.chatId, message.id, audio.audio.id,
                audio.title.ifBlank { audio.fileName }, audio.performer.ifBlank { "Unknown artist" }, audio.fileName,
                audio.duration * 1000L, audio.audio.size, audio.mimeType,
                cover?.local?.takeIf { it.isDownloadingCompleted }?.path, message.date.toLong(),
                cover?.remote?.uniqueId)
        }
        requireAccount(channel.accountId)
        dao.saveTracks(tracks)
        for ((key, audio) in covers) artwork.load(channel.accountId, key, audio)
        return page.nextFromMessageId.takeIf { it != 0L && it != cursor } ?: -1
    }

    suspend fun audio(key: MessageKey): TdApi.Audio {
        val account = client.account()
        if (!dao.indexed(account, key.chatId, key.messageId)) throw IOException("Select this track from a connected channel first.")
        val message = client.send(TdApi.GetMessage(key.chatId, key.messageId))
        val audio = telegramAudio(message.content) ?: throw IOException("This message no longer contains audio.")
        requireAccount(account)
        dao.refreshFileId(account, key.chatId, key.messageId, audio.audio.id)
        artwork.load(account, key, audio)
        return audio
    }
}
