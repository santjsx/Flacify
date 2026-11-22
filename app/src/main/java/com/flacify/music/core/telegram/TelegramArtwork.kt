package com.flacify.music.core.telegram

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.flacify.music.core.data.repository.PlaybackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.drinkless.tdlib.TdApi
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

internal fun artworkFiles(audio: TdApi.Audio): List<TdApi.File> =
    (listOfNotNull(audio.albumCoverThumbnail?.file) + audio.externalAlbumCovers.orEmpty().map { it.file })
        .distinctBy { it.id }

@Singleton
class TelegramArtwork @Inject constructor(
    private val client: TelegramClientManager,
    private val dao: TelegramDao,
    private val playback: PlaybackRepository,
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val permits = Semaphore(3)
    private var paused = false
    private val pending = ConcurrentHashMap.newKeySet<String>()

    @Synchronized fun load(account: Long, key: MessageKey, audio: TdApi.Audio) {
        if (paused) return
        val identity = "$account/${key.chatId}/${key.messageId}"
        if (!pending.add(identity)) return
        scope.launch {
            try {
                permits.withPermit { resolve(account, key, audio) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Artwork is optional. Opening or refreshing the track retries it.
            } finally {
                pending.remove(identity)
            }
        }
    }

    suspend fun clearWith(block: suspend () -> Unit) {
        val jobs = synchronized(this) {
            paused = true
            scope.coroutineContext[Job]!!.children.toList()
        }
        try {
            jobs.forEach { it.cancel() }
            jobs.joinAll()
            withContext(Dispatchers.IO) {
                File(context.cacheDir, "telegram-artwork").listFiles()?.forEach { it.delete() }
            }
            block()
        } finally {
            synchronized(this) { paused = false }
        }
    }

    private suspend fun publish(account: Long, key: MessageKey, path: String) {
        if (client.account() != account || !File(path).isFile) return
        dao.setArtwork(account, key.chatId, key.messageId, path)
        withContext(Dispatchers.Main.immediate) {
            if (client.account() == account) {
                playback.updateArtwork("telegram://audio/${key.chatId}/${key.messageId}", File(path).toUri().toString())
            }
        }
    }

    internal suspend fun resolve(account: Long, key: MessageKey, audio: TdApi.Audio) {
        if (client.account() != account) return
        val local = audio.audio.local
        if (local != null && local.isDownloadingCompleted && File(local.path).isFile) {
            val retriever = MediaMetadataRetriever()
            val picture = try {
                retriever.setDataSource(local.path)
                val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).orEmpty()
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty()
                val albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST).orEmpty()
                if (client.account() == account) dao.setTags(account, key.chatId, key.messageId, album, albumArtist, artist)
                retriever.embeddedPicture
            } catch (_: Exception) { null } finally { retriever.release() }
            if (picture != null) {
                val directory = File(context.cacheDir, "telegram-artwork").apply { mkdirs() }
                val image = File(directory, "$account-${key.chatId}-${key.messageId}-${picture.contentHashCode()}.jpg")
                image.writeBytes(picture)
                publish(account, key, image.absolutePath)
                return
            }
        }
        val miniature = audio.albumCoverMinithumbnail?.data
        if (miniature != null && miniature.isNotEmpty()) {
            val directory = File(context.cacheDir, "telegram-artwork").apply { mkdirs() }
            val image = File(directory, "$account-${key.chatId}-${key.messageId}-preview-${miniature.contentHashCode()}.jpg")
            image.writeBytes(miniature)
            publish(account, key, image.absolutePath)
        }
        for (cover in artworkFiles(audio)) {
            try {
                val file = if (cover.local.isDownloadingCompleted && File(cover.local.path).isFile) cover
                    else withTimeout(30_000) { client.send(TdApi.DownloadFile(cover.id, 4, 0, 0, true)) }
                if (file.local.isDownloadingCompleted && File(file.local.path).isFile) {
                    publish(account, key, file.local.path)
                    return
                }
            } catch (_: TimeoutCancellationException) {
                // Try the next available cover.
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // A missing thumbnail can have an external cover fallback.
            }
        }
    }
}
