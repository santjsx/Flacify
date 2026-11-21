package com.flacify.music.core.telegram

import org.drinkless.tdlib.TdApi
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramCacheManager @Inject constructor(
    private val client: TelegramClientManager, private val dao: TelegramDao
) {
    suspend fun sizeBytes(): Long = client.send(TdApi.GetStorageStatistics(0)).byChat.sumOf { chat ->
        chat.byFileType.filter { it.fileType is TdApi.FileTypeAudio || it.fileType is TdApi.FileTypeThumbnail }
            .sumOf { it.size }
    }

    suspend fun clear() = client.withIdleCache {
        val account = client.account()
        fun checkIdle() {
            if (client.account() != account) throw IOException("Telegram account changed.")
        }
        checkIdle()
        for (key in dao.keys(account)) {
            checkIdle()
            val message = try { client.send(TdApi.GetMessageLocally(key.chatId, key.messageId)) }
                catch (e: TelegramError) { if (e.code == 404) continue else throw e }
            val audio = (message.content as? TdApi.MessageAudio)?.audio ?: continue
            for (file in listOfNotNull(audio.audio, audio.albumCoverThumbnail?.file)) {
                if (file.local.canBeDeleted && file.local.downloadedSize > 0) {
                    checkIdle()
                    client.send(TdApi.DeleteFile(file.id))
                }
            }
        }
        checkIdle()
        // Also reclaim orphaned cache entries for deleted messages.
        client.send(TdApi.OptimizeStorage(0, 0, 0, 0,
            arrayOf(TdApi.FileTypeAudio(), TdApi.FileTypeThumbnail()), longArrayOf(), longArrayOf(), false, 0))
        dao.clearArtwork(account)
    }
}
