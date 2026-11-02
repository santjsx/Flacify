package com.flacify.music

import android.content.Context
import app.cash.turbine.test
import com.flacify.music.core.data.lyrics.LyricLine
import com.flacify.music.core.data.lyrics.Lyrics
import com.flacify.music.core.data.repository.LyricsRepository
import com.flacify.music.core.data.repository.LyricsResult
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.model.RepeatMode
import com.flacify.music.core.model.ShuffleMode
import com.flacify.music.core.model.Song
import com.flacify.music.feature.nowplaying.LyricsUiState
import com.flacify.music.feature.nowplaying.NowPlayingViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NowPlayingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)
    private val songRepository: SongRepository = mockk(relaxed = true)
    private val lyricsRepository: LyricsRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val currentSongFlow = MutableStateFlow<Song?>(null)

    private val testSong = Song(
        id = 1L,
        mediaUri = "content://media/1",
        title = "Viva La Vida",
        artistId = 1L,
        artistName = "Coldplay",
        albumId = 1L,
        albumTitle = "Viva La Vida",
        durationMs = 240000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { playbackRepository.currentSong } returns currentSongFlow
        every { playbackRepository.isPlaying } returns MutableStateFlow(false)
        every { playbackRepository.currentPositionMs } returns MutableStateFlow(0L)
        every { playbackRepository.durationMs } returns MutableStateFlow(0L)
        every { playbackRepository.repeatMode } returns MutableStateFlow(RepeatMode.OFF)
        every { playbackRepository.shuffleMode } returns MutableStateFlow(ShuffleMode.OFF)
        every { playbackRepository.queue } returns MutableStateFlow(emptyList())
        every { songRepository.isFavoriteFlow(any()) } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun lyricsUiState_whenCurrentSongChanges_updatesState() = runTest {
        val testLyrics = Lyrics(
            lines = listOf(LyricLine(1000L, "I used to rule the world")),
            isSynchronized = true
        )
        every { lyricsRepository.getLyrics(testSong) } returns flowOf(
            LyricsResult.Loading,
            LyricsResult.Success(testLyrics, isInstrumental = false, source = "lrclib")
        )

        val viewModel = NowPlayingViewModel(
            playbackRepository = playbackRepository,
            songRepository = songRepository,
            lyricsRepository = lyricsRepository,
            context = context
        )

        viewModel.lyricsUiState.test {
            // Initial state with no song
            val initial = awaitItem()
            assertTrue(initial is LyricsUiState.Empty || initial is LyricsUiState.Loading)

            // Emit current song
            currentSongFlow.value = testSong
            testDispatcher.scheduler.advanceUntilIdle()

            val success = awaitItem()
            assertTrue(success is LyricsUiState.Success)
            assertEquals("I used to rule the world", (success as LyricsUiState.Success).lyrics.lines[0].text)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
