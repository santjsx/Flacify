package com.flacify.music.core.telegram

import android.content.Context
import android.net.Uri
import com.flacify.music.core.data.repository.PlaybackRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.drinkless.tdlib.TdApi
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class TelegramArtworkTest {
    @get:Rule val folder = TemporaryFolder()

    private fun cover(id: Int, path: String = "") = TdApi.File().apply {
        this.id = id
        local = TdApi.LocalFile().apply {
            this.path = path
            isDownloadingCompleted = path.isNotEmpty()
        }
    }

    @Test fun externalCoversWorkWhenSenderThumbnailIsMissing() {
        val external = cover(12)
        val audio = TdApi.Audio().apply {
            externalAlbumCovers = arrayOf(TdApi.Thumbnail().apply { file = external })
        }
        assertEquals(listOf(external), artworkFiles(audio))
    }

    @Test fun completionUpdatesTheMessageAndActivePlayerWithoutAUniqueId() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        mockkStatic(Uri::class)
        val uri = mockk<Uri>()
        every { Uri.fromFile(any()) } returns uri
        every { uri.toString() } returns "file:///cover.jpg"
        try {
            val client = mockk<TelegramClientManager>()
            val dao = mockk<TelegramDao>(relaxed = true)
            val playback = mockk<PlaybackRepository>(relaxed = true)
            every { client.account() } returns 1
            val image = folder.newFile("cover.jpg")
            image.writeBytes(byteArrayOf(1, 2))
            val complete = cover(12, image.absolutePath)
            coEvery { client.send(any<TdApi.DownloadFile>()) } returns complete
            val audio = TdApi.Audio().apply {
                this.audio = cover(1)
                albumCoverThumbnail = TdApi.Thumbnail().apply { file = cover(12) }
            }
            val loader = TelegramArtwork(client, dao, playback, mockk<Context>())
            loader.resolve(1, MessageKey(10, 20), audio)
            coVerify { client.send(match<TdApi.DownloadFile> { it.synchronous && it.priority == 4 }) }
            coVerify { dao.setArtwork(1, 10, 20, image.absolutePath) }
            verify { playback.updateArtwork("telegram://audio/10/20", any()) }
        } finally { unmockkStatic(Uri::class); Dispatchers.resetMain() }
    }
}
