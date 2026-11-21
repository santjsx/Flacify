package com.flacify.music

import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.model.Song
import com.flacify.music.feature.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val songRepository: SongRepository = mockk(relaxed = true)
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { playbackRepository.currentSong } returns MutableStateFlow(null)
        coEvery { playbackRepository.isPlaying } returns MutableStateFlow(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateQuery_updatesStateFlow() {
        val viewModel = SearchViewModel(songRepository, playbackRepository)
        viewModel.updateQuery("Radiohead")
        assertEquals("Radiohead", viewModel.searchQuery.value)
    }

    @Test
    fun clearQuery_resetsToBlank() {
        val viewModel = SearchViewModel(songRepository, playbackRepository)
        viewModel.updateQuery("Beatles")
        viewModel.clearQuery()
        assertEquals("", viewModel.searchQuery.value)
    }
    @Test
    fun changingQueryDuringDebouncePublishesOnlyTheLatestResult() = runTest(testDispatcher) {
        coEvery { songRepository.searchSongs(any()) } returns emptyList()
        val viewModel = SearchViewModel(songRepository, playbackRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.searchResults.collect() }
        viewModel.updateQuery("Radio")
        advanceTimeBy(200)
        viewModel.updateQuery("Head")
        advanceTimeBy(299)
        coVerify(exactly = 0) { songRepository.searchSongs(any()) }
        advanceTimeBy(1)
        runCurrent()

        assertEquals("Head", viewModel.searchResults.value.query)
        coVerify(exactly = 1) { songRepository.searchSongs("Head") }
        viewModel.clearQuery()
        runCurrent()
        assertEquals("", viewModel.searchResults.value.query)
    }

}

