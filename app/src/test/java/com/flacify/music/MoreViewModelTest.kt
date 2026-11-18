package com.flacify.music

import com.flacify.music.core.audio.haptics.MusicHapticsManager
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.data.repository.ThemeMode
import com.flacify.music.core.data.repository.UserPreferencesRepository
import com.flacify.music.core.model.HapticMode
import com.flacify.music.core.model.HapticStrength
import com.flacify.music.core.update.GitHubUpdateManager
import com.flacify.music.core.update.UpdateCheckResult
import com.flacify.music.feature.more.MoreViewModel
import com.flacify.music.feature.more.UpdateUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val songRepository: SongRepository = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val gitHubUpdateManager: GitHubUpdateManager = mockk(relaxed = true)
    private val musicHapticsManager: MusicHapticsManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { songRepository.getSongCount() } returns flowOf(500)
        coEvery { songRepository.getAlbumCount() } returns flowOf(50)
        coEvery { songRepository.getArtistCount() } returns flowOf(30)
        coEvery { userPreferencesRepository.themeMode } returns flowOf(ThemeMode.SYSTEM)
        coEvery { userPreferencesRepository.minTrackDurationSec } returns flowOf(15)
        coEvery { userPreferencesRepository.musicHapticsMode } returns flowOf(HapticMode.FULL_MIX)
        coEvery { userPreferencesRepository.musicHapticsStrength } returns flowOf(HapticStrength.BALANCED)
        coEvery { userPreferencesRepository.musicHapticsSyncOffsetMs } returns flowOf(0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun themeMode_updatesSuccessfully() = runTest(testDispatcher) {
        val viewModel = MoreViewModel(songRepository, userPreferencesRepository, gitHubUpdateManager, musicHapticsManager)
        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        coVerify { userPreferencesRepository.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun musicHapticsMode_updatesSuccessfully() = runTest(testDispatcher) {
        val viewModel = MoreViewModel(songRepository, userPreferencesRepository, gitHubUpdateManager, musicHapticsManager)
        viewModel.setMusicHapticsMode(HapticMode.VOCALS_ONLY)
        advanceUntilIdle()

        verify { musicHapticsManager.setMode(HapticMode.VOCALS_ONLY) }
    }

    @Test
    fun musicHapticsStrength_updatesSuccessfully() = runTest(testDispatcher) {
        val viewModel = MoreViewModel(songRepository, userPreferencesRepository, gitHubUpdateManager, musicHapticsManager)
        viewModel.setMusicHapticsStrength(HapticStrength.POWERFUL)
        advanceUntilIdle()

        verify { musicHapticsManager.setStrength(HapticStrength.POWERFUL) }
    }

    @Test
    fun musicHapticsSyncOffset_updatesSuccessfully() = runTest(testDispatcher) {
        val viewModel = MoreViewModel(songRepository, userPreferencesRepository, gitHubUpdateManager, musicHapticsManager)
        viewModel.setMusicHapticsSyncOffsetMs(25)
        advanceUntilIdle()

        verify { musicHapticsManager.setSyncOffset(25) }
    }

    @Test
    fun musicHapticsTestBeat_triggersCorrectly() = runTest(testDispatcher) {
        val viewModel = MoreViewModel(songRepository, userPreferencesRepository, gitHubUpdateManager, musicHapticsManager)
        viewModel.triggerTestBeat()
        advanceUntilIdle()

        verify { musicHapticsManager.triggerTestBeat() }
    }

    @Test
    fun checkForUpdates_availableRelease_updatesUiState() = runTest(testDispatcher) {
        coEvery { gitHubUpdateManager.checkForUpdates(any(), any()) } returns UpdateCheckResult.UpdateAvailable(
            latestVersion = "1.2.0",
            releaseNotes = "Fix audio focus and improve scrubber smoothness",
            downloadUrl = "https://github.com/flacify/releases/download/v1.2.0/app-release.apk",
            apkSizeBytes = 15_000_000L
        )

        val viewModel = MoreViewModel(songRepository, userPreferencesRepository, gitHubUpdateManager, musicHapticsManager)
        viewModel.checkForUpdates()
        advanceUntilIdle()

        assertTrue(viewModel.updateUiState.value is UpdateUiState.Available)
        val state = viewModel.updateUiState.value as UpdateUiState.Available
        assertEquals("1.2.0", state.version)
        assertEquals(15_000_000L, state.sizeBytes)
    }

    @Test
    fun checkForUpdates_upToDate_updatesUiState() = runTest(testDispatcher) {
        coEvery { gitHubUpdateManager.checkForUpdates(any(), any()) } returns UpdateCheckResult.UpToDate

        val viewModel = MoreViewModel(songRepository, userPreferencesRepository, gitHubUpdateManager, musicHapticsManager)
        viewModel.checkForUpdates()
        advanceUntilIdle()

        assertTrue(viewModel.updateUiState.value is UpdateUiState.UpToDate)
    }
}

