package com.flacify.music

import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.PlaylistRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.data.repository.UserPreferencesRepository
import com.flacify.music.core.model.Album
import com.flacify.music.core.model.AlbumSortOption
import com.flacify.music.core.model.Artist
import com.flacify.music.core.model.Playlist
import com.flacify.music.core.model.Song
import com.flacify.music.core.model.SortOption
import com.flacify.music.feature.library.LibraryTab
import com.flacify.music.feature.library.LibraryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val songRepository: SongRepository = mockk(relaxed = true)
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)
    private val playlistRepository: PlaylistRepository = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)

    private val samplePlaylist = Playlist(
        id = 1L,
        name = "Favorites 2026",
        trackCount = 1,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private val sampleAlbum = Album(
        id = 10L,
        title = "Random Access Memories",
        artistId = 20L,
        artistName = "Daft Punk",
        trackCount = 13,
        year = 2013
    )

    private val sampleArtist = Artist(
        id = 20L,
        name = "Daft Punk",
        trackCount = 13,
        albumCount = 1
    )

    private val sampleSong = Song(
        id = 101L,
        title = "Get Lucky",
        artistId = 20L,
        artistName = "Daft Punk",
        albumId = 10L,
        albumTitle = "Random Access Memories",
        durationMs = 248000L,
        mediaUri = "content://media/101"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { songRepository.getSongCount() } returns flowOf(13)
        coEvery { songRepository.getAlbumCount() } returns flowOf(1)
        coEvery { songRepository.getArtistCount() } returns flowOf(1)
        coEvery { songRepository.getAllAlbums() } returns flowOf(listOf(sampleAlbum))
        coEvery { songRepository.getAllArtists() } returns flowOf(listOf(sampleArtist))
        coEvery { songRepository.getSongsByAlbum(10L) } returns flowOf(listOf(sampleSong))
        coEvery { songRepository.getSongsByArtist(20L) } returns flowOf(listOf(sampleSong))
        coEvery { playlistRepository.getAllPlaylists() } returns flowOf(listOf(samplePlaylist))
        coEvery { playlistRepository.getSongsForPlaylist(1L) } returns flowOf(listOf(sampleSong))
        coEvery { playbackRepository.currentSong } returns MutableStateFlow(null)
        coEvery { playbackRepository.isPlaying } returns MutableStateFlow(false)
        coEvery { userPreferencesRepository.albumSortOption } returns MutableStateFlow(AlbumSortOption.TITLE_ASC)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun tabSelection_updatesSelectedTab() = runTest(testDispatcher) {
        val viewModel = LibraryViewModel(songRepository, playbackRepository, playlistRepository, userPreferencesRepository)
        assertEquals(LibraryTab.SONGS, viewModel.selectedTab.value)

        viewModel.selectTab(LibraryTab.PLAYLISTS)
        assertEquals(LibraryTab.PLAYLISTS, viewModel.selectedTab.value)

        viewModel.selectTab(LibraryTab.ALBUMS)
        assertEquals(LibraryTab.ALBUMS, viewModel.selectedTab.value)

        viewModel.selectTab(LibraryTab.ARTISTS)
        assertEquals(LibraryTab.ARTISTS, viewModel.selectedTab.value)
    }

    @Test
    fun sortOption_updatesSortState() = runTest(testDispatcher) {
        val viewModel = LibraryViewModel(songRepository, playbackRepository, playlistRepository, userPreferencesRepository)
        assertEquals(SortOption.TITLE_ASC, viewModel.sortOption.value)

        viewModel.selectSortOption(SortOption.DATE_ADDED)
        assertEquals(SortOption.DATE_ADDED, viewModel.sortOption.value)
    }

    @Test
    fun albumSortOption_delegatesToUserPreferences() = runTest(testDispatcher) {
        val viewModel = LibraryViewModel(songRepository, playbackRepository, playlistRepository, userPreferencesRepository)
        viewModel.selectAlbumSortOption(AlbumSortOption.YEAR_DESC)
        advanceUntilIdle()
        coVerify { userPreferencesRepository.setAlbumSortOption(AlbumSortOption.YEAR_DESC) }
    }

    @Test
    fun albumSelection_loadsAlbumSongs() = runTest(testDispatcher) {
        val viewModel = LibraryViewModel(songRepository, playbackRepository, playlistRepository, userPreferencesRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.selectedAlbumSongs.collect()
        }
        advanceUntilIdle()

        viewModel.selectAlbum(sampleAlbum)
        advanceUntilIdle()

        assertEquals(sampleAlbum, viewModel.selectedAlbum.value)
        assertEquals(1, viewModel.selectedAlbumSongs.value.size)
        assertEquals("Get Lucky", viewModel.selectedAlbumSongs.value.first().title)
    }

    @Test
    fun playlistSelection_loadsPlaylistSongs() = runTest(testDispatcher) {
        val viewModel = LibraryViewModel(songRepository, playbackRepository, playlistRepository, userPreferencesRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.selectedPlaylistSongs.collect()
        }
        advanceUntilIdle()

        viewModel.selectPlaylist(samplePlaylist)
        advanceUntilIdle()

        assertEquals(samplePlaylist, viewModel.selectedPlaylist.value)
        assertEquals(1, viewModel.selectedPlaylistSongs.value.size)
        assertEquals("Get Lucky", viewModel.selectedPlaylistSongs.value.first().title)
    }

    @Test
    fun playQueue_delegatesToPlaybackRepository() = runTest(testDispatcher) {
        val viewModel = LibraryViewModel(songRepository, playbackRepository, playlistRepository, userPreferencesRepository)
        val queue = listOf(sampleSong)

        viewModel.playQueue(queue, 0)
        coVerify { playbackRepository.playSong(sampleSong, queue) }
    }
}

// test
