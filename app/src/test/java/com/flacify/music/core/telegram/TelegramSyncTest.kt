package com.flacify.music.core.telegram

import io.mockk.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.flacify.music.feature.telegram.TelegramViewModel
import com.flacify.music.core.data.repository.PlaybackRepository
import kotlinx.coroutines.runBlocking
import org.drinkless.tdlib.TdApi
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelegramSyncTest {
    @Test fun cacheStatisticsFailureDoesNotPreventDiscovery() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val client = mockk<TelegramClientManager>(relaxed = true)
            every { client.auth } returns MutableStateFlow<TelegramAuthState>(TelegramAuthState.Ready(7, "User"))
            val repo = mockk<TelegramChannelRepository>(relaxed = true)
            val cache = mockk<TelegramCacheManager>()
            coEvery { cache.sizeBytes() } throws java.io.IOException("Unavailable")
            val vm = TelegramViewModel(client, repo, cache, mockk<PlaybackRepository>(relaxed = true))
            advanceUntilIdle()
            coVerify(exactly = 1) { repo.getUserChannels() }
            assertFalse(vm.discovering.value)
            assertEquals("", vm.status.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private val client = mockk<TelegramClientManager>(relaxed = true)
    private val dao = mockk<TelegramDao>(relaxed = true)
    private fun repository(): TelegramChannelRepository {
        every { client.account() } returns 7
        every { client.updates } returns MutableSharedFlow()
        return TelegramChannelRepository(client, dao, mockk(relaxed = true))
    }

    private fun document(name: String, mime: String = "application/octet-stream") = TdApi.MessageDocument().apply {
        document = TdApi.Document().apply {
            fileName = name
            mimeType = mime
            document = TdApi.File().apply { id = 42; size = 100 }
        }
    }

    @Test fun audioDocumentsArePlayableButOtherDocumentsAreExcluded() {
        val content = document("Album.FLAC")
        val audio = telegramAudio(content)!!
        assertEquals("Album", audio.title)
        assertEquals(42, audio.audio.id)
        assertNull(telegramAudio(document("book.pdf")))
        assertNotNull(telegramAudio(document("recording", "audio/mpeg")))
    }

    @Test fun independentCursorsContinueThroughDocumentPagesWithoutMusic() = runBlocking {
        val repo = repository()
        val filters = mutableListOf<TdApi.SearchChatMessages>()
        coEvery { client.send(capture(filters)) } answers {
            TdApi.FoundChatMessages().apply {
                messages = emptyArray()
                nextFromMessageId = if (firstArg<TdApi.SearchChatMessages>().filter is TdApi.SearchMessagesFilterDocument) 20 else 0
            }
        }
        val channel = TelegramChannel(7, 10, "Music")
        val next = repo.page(channel, TelegramPageCursor())
        assertEquals(TelegramPageCursor(-1, 20), next)
        assertTrue(next.hasMore)
        filters.clear()
        val end = repo.page(channel, next)
        assertEquals(1, filters.size)
        assertTrue(filters.single().filter is TdApi.SearchMessagesFilterDocument)
        assertFalse(end.hasMore)
    }

    @Test fun indexedDocumentResolvesForPlaybackWithRefreshedFileId() = runBlocking {
        val repo = repository()
        coEvery { dao.indexed(7, 10, 20) } returns true
        coEvery { client.send(any<TdApi.GetMessage>()) } returns TdApi.Message().apply {
            content = document("Song.flac")
        }
        assertEquals(42, repo.audio(MessageKey(10, 20)).audio.id)
        coVerify { dao.refreshFileId(7, 10, 20, 42) }
    }

    @Test fun discoveryIncludesSavedMessagesAndGroupsAfterLoadingChats() = runBlocking {
        val repo = repository()
        coEvery { client.send(ofType<TdApi.CreatePrivateChat>()) } returns TdApi.Chat().apply { id = 7 }
        coEvery { client.send(ofType<TdApi.LoadChats>()) } throws TelegramError(404, "All loaded")
        coEvery { client.send(ofType<TdApi.GetChats>()) } returns TdApi.Chats().apply { chatIds = longArrayOf(10) }
        coEvery { client.send(ofType<TdApi.GetChat>()) } returns TdApi.Chat().apply {
            id = 10
            title = "Music group"
            type = TdApi.ChatTypeBasicGroup(10)
        }
        repo.getUserChannels()
        coVerify { dao.saveChannels(listOf(TelegramChannel(7, 7, "Saved Messages"))) }
        coVerify { dao.saveChannels(listOf(TelegramChannel(7, 10, "Music group"))) }
    }
}
