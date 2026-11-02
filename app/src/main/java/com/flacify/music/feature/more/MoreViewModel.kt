package com.flacify.music.feature.more

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.data.repository.ThemeMode
import com.flacify.music.core.data.repository.UserPreferencesRepository
import com.flacify.music.core.update.GitHubUpdateManager
import com.flacify.music.core.update.UpdateCheckResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class Available(
        val version: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val sizeBytes: Long
    ) : UpdateUiState
    data class UpToDate(val version: String) : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val gitHubUpdateManager: GitHubUpdateManager
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val minTrackDurationSec: StateFlow<Int> = userPreferencesRepository.minTrackDurationSec
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15)

    val songCount: StateFlow<Int> = songRepository.getSongCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val albumCount: StateFlow<Int> = songRepository.getAlbumCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val artistCount: StateFlow<Int> = songRepository.getArtistCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _updateUiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateUiState: StateFlow<UpdateUiState> = _updateUiState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun setMinTrackDurationSec(seconds: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setMinTrackDurationSec(seconds)
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateUiState.value = UpdateUiState.Checking
            when (val result = gitHubUpdateManager.checkForUpdates()) {
                is UpdateCheckResult.UpdateAvailable -> {
                    _updateUiState.value = UpdateUiState.Available(
                        version = result.latestVersion,
                        releaseNotes = result.releaseNotes,
                        downloadUrl = result.downloadUrl,
                        sizeBytes = result.apkSizeBytes
                    )
                }
                is UpdateCheckResult.UpToDate -> {
                    _updateUiState.value = UpdateUiState.UpToDate("1.0.0")
                }
                is UpdateCheckResult.Error -> {
                    _updateUiState.value = UpdateUiState.Error(result.message)
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateUiState.value = UpdateUiState.Idle
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    fun clearArtworkCache(context: Context) {
        Coil.imageLoader(context).diskCache?.clear()
        Coil.imageLoader(context).memoryCache?.clear()
    }
}

