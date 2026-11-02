package com.aurel.music

import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.model.Song
import com.aurel.music.feature.search.SearchViewModel
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
}

