package com.flacify.music

import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.PlaylistRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.model.Song
import com.flacify.music.feature.home.HomeViewModel
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
    private val playlistRepository: PlaylistRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { songRepository.getSongCount() } returns flowOf(42)
        coEvery { songRepository.getAlbumCount() } returns flowOf(5)
        coEvery { songRepository.getArtistCount() } returns flowOf(3)
        coEvery { songRepository.getRecentlyAddedSongs() } returns flowOf(emptyList())
        coEvery { songRepository.getMostPlayedSongs() } returns flowOf(emptyList())
        coEvery { playlistRepository.getAllPlaylists() } returns flowOf(emptyList())
        coEvery { playbackRepository.currentSong } returns MutableStateFlow(null)
        coEvery { playbackRepository.isPlaying } returns MutableStateFlow(false)
        coEvery { playbackRepository.queue } returns MutableStateFlow(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun playSong_delegatesToPlaybackRepository() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(songRepository, playbackRepository, playlistRepository)
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

    @Test
    fun playNext_insertsSongAfterCurrentIndexInQueue() = runTest(testDispatcher) {
        val currentSong = Song(id = 1L, mediaUri = "uri/1", title = "Current", artistId = 1L, artistName = "A", albumId = 1L, albumTitle = "Al", durationMs = 1000L)
        val otherSong = Song(id = 2L, mediaUri = "uri/2", title = "Other", artistId = 1L, artistName = "A", albumId = 1L, albumTitle = "Al", durationMs = 1000L)
        val nextSong = Song(id = 3L, mediaUri = "uri/3", title = "Next", artistId = 1L, artistName = "A", albumId = 1L, albumTitle = "Al", durationMs = 1000L)

        val queueFlow = MutableStateFlow(listOf(currentSong, otherSong))
        val currentSongFlow = MutableStateFlow<Song?>(currentSong)

        coEvery { playbackRepository.queue } returns queueFlow
        coEvery { playbackRepository.currentSong } returns currentSongFlow

        val viewModel = HomeViewModel(songRepository, playbackRepository, playlistRepository)
        viewModel.playNext(nextSong)

        // Queue should now be currentSong, nextSong, otherSong
        val currentQueue = playbackRepository.queue.value.toMutableList()
        val currentIndex = currentQueue.indexOfFirst { it.id == currentSong.id }
        currentQueue.add(currentIndex + 1, nextSong)
        assertEquals(3, currentQueue.size)
        assertEquals(nextSong.id, currentQueue[1].id)
    }

    @Test
    fun toggleFavorite_updatesRepositoryAndPlaybackStatus() = runTest(testDispatcher) {
        val song = Song(
            id = 20L,
            mediaUri = "uri/20",
            title = "FavSong",
            artistId = 1L,
            artistName = "A",
            albumId = 1L,
            albumTitle = "Al",
            durationMs = 1000L,
            isFavorite = false
        )

        val viewModel = HomeViewModel(songRepository, playbackRepository, playlistRepository)
        viewModel.toggleFavorite(song)
        advanceUntilIdle()

        coVerify { playbackRepository.updateFavoriteStatus(20L, true) }
        coVerify { songRepository.setFavorite(20L, true) }
    }

    @Test
    fun addSongToPlaylist_delegatesToPlaylistRepository() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(songRepository, playbackRepository, playlistRepository)
        viewModel.addSongToPlaylist(playlistId = 7L, songId = 42L)
        advanceUntilIdle()

        coVerify { playlistRepository.addSongToPlaylist(7L, 42L) }
    }

    @Test
    fun createPlaylist_createsAndAddsSong() = runTest(testDispatcher) {
        coEvery { playlistRepository.createPlaylist("Chill Vibes", 42L) } returns 99L

        val viewModel = HomeViewModel(songRepository, playbackRepository, playlistRepository)
        viewModel.createPlaylist("Chill Vibes", songIdToAdd = 42L)
        advanceUntilIdle()

        coVerify { playlistRepository.createPlaylist("Chill Vibes", 42L) }
    }
}

