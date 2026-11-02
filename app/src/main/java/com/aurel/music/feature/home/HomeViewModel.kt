package com.aurel.music.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.data.scanner.ScanStatus
import com.aurel.music.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val artistCount: Int = 0,
    val recentlyAdded: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val isScanning: Boolean = false,
    val scanMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    val songCount = songRepository.getSongCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val albumCount = songRepository.getAlbumCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val artistCount = songRepository.getArtistCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentlyAdded = songRepository.getRecentlyAddedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed = songRepository.getMostPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentPlayingSong = playbackRepository.currentSong
    val isPlaying = playbackRepository.isPlaying

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    fun scanLibrary() {
        viewModelScope.launch {
            _scanStatus.value = ScanStatus.Scanning(0, 0)
            val result = songRepository.scanLibrary()
            _scanStatus.value = result
        }
    }

    fun playSong(song: Song, queue: List<Song>) {
        playbackRepository.playSong(song, queue)
    }

    fun togglePlayPause() {
        if (playbackRepository.isPlaying.value) {
            playbackRepository.pause()
        } else {
            playbackRepository.resume()
        }
    }

    fun skipToNext() {
        playbackRepository.skipToNext()
    }
}

