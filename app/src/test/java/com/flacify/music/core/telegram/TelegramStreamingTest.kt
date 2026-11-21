package com.flacify.music.core.telegram

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import io.mockk.*
import org.drinkless.tdlib.TdApi
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@UnstableApi
class TelegramStreamingTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun normalizesOnlyPublicChannelLinks() {
        assertEquals("music_feed", channelUsername(" @music_feed "))
        assertEquals("music_feed", channelUsername("https://t.me/music_feed/"))
        assertEquals("music_feed", channelUsername("t.me/music_feed"))
        assertThrows(IllegalArgumentException::class.java) { channelUsername("https://example.com/music_feed") }
        assertThrows(IllegalArgumentException::class.java) { channelUsername("https://t.me/+invite") }
    }

    private val client = mockk<TelegramClientManager>(relaxed = true)
    private val repository = mockk<TelegramChannelRepository>()
    private val watch = TelegramClientManager.Watch(9)
    private val uri = mockk<Uri> {
        every { host } returns "audio"
        every { pathSegments } returns listOf("-123", "77")
    }

    private fun source(position: Long, length: Long, prefix: Int): TelegramStreamingDataSource {
        val disk = folder.newFile()
        disk.writeBytes(ByteArray(20) { it.toByte() })
        val remote = TdApi.File().apply {
            id = 9
            size = 20
            local = TdApi.LocalFile().apply {
                path = disk.absolutePath
                canBeDownloaded = true
                isDownloadingActive = true
                downloadOffset = position
                downloadedPrefixSize = prefix.toLong()
            }
        }
        every { client.account() } returns 123L
        every { client.watch(9) } returns watch
        coEvery { repository.audio(MessageKey(-123, 77)) } returns TdApi.Audio().apply { audio = remote }
        coEvery { client.send(match<TdApi.DownloadFile> { it.offset == position && it.priority == 32 && !it.synchronous }) } returns remote
        val source = TelegramStreamingDataSource(client, repository)
        assertEquals(length, source.open(DataSpec.Builder().setUri(uri).setPosition(position).setLength(length).build()))
        return source
    }

    @Test fun readsOnlyTheDownloadedPrefixAndHonorsSeekAndRequestedLength() {
        val source = source(position = 8, length = 5, prefix = 2)
        try {
            val output = ByteArray(5)
            assertEquals(2, source.read(output, 0, 5))
            val refreshed = TdApi.File().apply {
                id = 9
                size = 20
                local = TdApi.LocalFile().apply {
                    path = folder.root.listFiles()!!.single().absolutePath
                    downloadOffset = 8
                    downloadedPrefixSize = 8
                    isDownloadingActive = true
                }
            }
            watch.latest.value = refreshed
            assertEquals(3, source.read(output, 2, 3))
            assertArrayEquals(byteArrayOf(8, 9, 10, 11, 12), output)
            assertEquals(C.RESULT_END_OF_INPUT, source.read(output, 0, 1))
        } finally { source.close() }
        verify(exactly = 1) { client.release(watch) }
    }

    @Test fun closingSourceUnblocksAReaderWaitingForItsFirstByte() {
        val source = source(position = 8, length = 5, prefix = 0)
        val executor = Executors.newSingleThreadExecutor()
        try {
            val future = executor.submit<Boolean> {
                try {
                    source.read(ByteArray(1), 0, 1)
                    false
                } catch (_: IOException) {
                    true
                }
            }
            runBlocking { withTimeout(5_000) { watch.latest.subscriptionCount.first { it > 0 } } }
            source.close()
            assertTrue(future.get(2, TimeUnit.SECONDS))
        } finally {
            source.close()
            executor.shutdownNow()
        }
    }
}
