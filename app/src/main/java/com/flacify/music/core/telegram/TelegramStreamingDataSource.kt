package com.flacify.music.core.telegram

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.drinkless.tdlib.TdApi
import java.io.*
import javax.inject.Inject

internal fun readableBytes(position: Long, start: Long, prefix: Long): Long =
    if (position < start) 0 else (prefix - (position - start)).coerceAtLeast(0)

@UnstableApi
class TelegramStreamingDataSource(
    private val client: TelegramClientManager,
    private val repository: TelegramChannelRepository
) : BaseDataSource(true) {
    private class Session(val uri: Uri, val account: Long, var position: Long, var remaining: Long) {
        val job = Job()
        var watch: TelegramClientManager.Watch? = null
        var file: TdApi.File? = null
        var handle: RandomAccessFile? = null
        var path = ""
        var started = false
    }
    @Volatile private var session: Session? = null

    private fun <T> await(s: Session, action: suspend () -> T): T {
        try {
            return runBlocking(s.job) { withTimeout(45_000) { action() } }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw InterruptedIOException("Telegram read interrupted").apply { initCause(e) }
        } catch (e: CancellationException) {
            throw IOException("Telegram read canceled or timed out", e)
        }
    }

    override fun open(dataSpec: DataSpec): Long {
        transferInitializing(dataSpec)
        val uri = dataSpec.uri
        val parts = uri.pathSegments
        val chat = parts.getOrNull(0)?.toLongOrNull()
        val message = parts.getOrNull(1)?.toLongOrNull()
        if (uri.host != "audio" || parts.size != 2 || chat == null || message == null) throw IOException("Invalid Telegram audio URI")
        val s = Session(uri, client.account(), dataSpec.position, dataSpec.length)
        session = s
        try {
            val file = await(s) { repository.audio(MessageKey(chat, message)).audio }
            synchronized(s) {
                if (!s.job.isActive || client.account() != s.account) throw IOException("Telegram session closed")
                s.watch = client.watch(file.id)
            }
            if (file.size > 0) {
                val available = file.size - s.position
                if (available < 0) throw IOException("Seek is beyond the end of the track")
                s.remaining = if (s.remaining == C.LENGTH_UNSET.toLong()) available else minOf(s.remaining, available)
            }
            s.file = if (file.local.isDownloadingCompleted || s.remaining == 0L) file
                else await(s) { client.send(TdApi.DownloadFile(file.id, 32, s.position, 0, false)) }
            synchronized(s) {
                if (!s.job.isActive) throw IOException("Telegram source closed")
                s.started = true
                transferStarted(dataSpec)
            }
            return s.remaining
        } catch (e: Exception) {
            close()
            throw e
        }
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        val s = session ?: throw IOException("Telegram source closed")
        val watch = s.watch ?: throw IOException("Telegram source is not open")
        var retried = false
        while (true) {
            if (!s.job.isActive || Thread.currentThread().isInterrupted || client.account() != s.account) throw InterruptedIOException("Telegram session ended")
            if (s.remaining == 0L) return C.RESULT_END_OF_INPUT
            val update = watch.latest.value
            val file = update ?: s.file ?: throw IOException("Missing Telegram file")
            val local = file.local
            val available = if (local.isDownloadingCompleted) {
                (file.size.takeIf { it > 0 } ?: File(local.path).length()) - s.position
            } else readableBytes(s.position, local.downloadOffset, local.downloadedPrefixSize)
            if (available > 0 && local.path.isNotEmpty()) {
                val count = synchronized(s) {
                    if (!s.job.isActive) throw InterruptedIOException()
                    if (s.path != local.path || s.handle == null) {
                        s.handle?.close()
                        s.handle = RandomAccessFile(local.path, "r")
                        s.path = local.path
                    }
                    val input = s.handle!!
                    input.seek(s.position)
                    val requested = minOf(length.toLong(), available, if (s.remaining < 0) Long.MAX_VALUE else s.remaining)
                    input.read(buffer, offset, requested.toInt())
                }
                if (count <= 0) throw EOFException("Telegram cache ended inside a downloaded range")
                s.position += count
                if (s.remaining >= 0) s.remaining -= count
                bytesTransferred(count)
                return count
            }
            if (local.isDownloadingCompleted) return C.RESULT_END_OF_INPUT
            if (!local.isDownloadingActive || s.position < local.downloadOffset) {
                if (!local.canBeDownloaded || retried) throw IOException("Telegram could not supply the requested range")
                retried = true
                s.file = await(s) { client.send(TdApi.DownloadFile(file.id, 32, s.position, 0, false)) }
                if (watch.latest.compareAndSet(update, s.file)) continue
            }
            await(s) { watch.latest.first { it !== update } }
        }
    }

    override fun getUri(): Uri? = session?.uri
    override fun close() {
        val s = session ?: return
        session = null
        s.job.cancel()
        synchronized(s) {
            s.watch?.let(client::release)
            try { s.handle?.close() } finally { if (s.started) transferEnded() }
        }
    }

    class Factory @Inject constructor(private val client: TelegramClientManager, private val repo: TelegramChannelRepository) : DataSource.Factory {
        override fun createDataSource() = TelegramStreamingDataSource(client, repo)
    }
}

// Media3 still handles content, file, HTTP, and asset URIs through its normal factory.
@UnstableApi
internal class MusicDataSource(private val local: DataSource, private val telegram: () -> DataSource) : DataSource {
    private var active: DataSource? = null
    private val listeners = mutableListOf<TransferListener>()
    override fun addTransferListener(listener: TransferListener) {
        local.addTransferListener(listener)
        listeners.add(listener)
        active?.takeIf { it !== local }?.addTransferListener(listener)
    }
    override fun open(dataSpec: DataSpec): Long {
        val source = if (dataSpec.uri.scheme == "telegram") telegram().also { remote -> listeners.forEach(remote::addTransferListener) } else local
        active = source
        return source.open(dataSpec)
    }
    override fun read(buffer: ByteArray, offset: Int, length: Int) =
        (active ?: throw IOException("Source closed")).read(buffer, offset, length)
    override fun getUri() = active?.uri
    override fun getResponseHeaders() = active?.responseHeaders.orEmpty()
    override fun close() { try { active?.close() } finally { active = null } }
}
