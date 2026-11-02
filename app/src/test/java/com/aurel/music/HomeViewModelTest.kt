package com.aurel.music

import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.model.Song
import com.aurel.music.feature.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val songRepository: SongRepository = mockk(relaxed = true)
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { songRepository.getSongCount() } returns flowOf(42)
        coEvery { songRepository.getAlbumCount() } returns flowOf(5)
        coEvery { songRepository.getArtistCount() } returns flowOf(3)
        coEvery { songRepository.getRecentlyAddedSongs() } returns flowOf(emptyList())
        coEvery { songRepository.getMostPlayedSongs() } returns flowOf(emptyList())
        coEvery { playbackRepository.currentSong } returns MutableStateFlow(null)
        coEvery { playbackRepository.isPlaying } returns MutableStateFlow(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun playSong_delegatesToPlaybackRepository() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(songRepository, playbackRepository)
        val song = Song(
            id = 10L,
            mediaUri = "content://song/10",
            title = "Test",
            artistId = 1L,
            artistName = "Artist",
            albumId = 1L,
            albumTitle = "Album",
            durationMs = 120000L
        )

        viewModel.playSong(song, listOf(song))
        coVerify { playbackRepository.playSong(song, listOf(song)) }
    }
}
