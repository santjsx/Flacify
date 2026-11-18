package com.flacify.music.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.PlaylistRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.data.scanner.ScanStatus
import com.flacify.music.core.model.Playlist
import com.flacify.music.core.model.Song
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
    private val playbackRepository: PlaybackRepository,
    private val playlistRepository: PlaylistRepository
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

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
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

    fun playNext(song: Song) {
        val currentQueue = playbackRepository.queue.value.toMutableList()
        val current = playbackRepository.currentSong.value
        val currentIndex = if (current != null) currentQueue.indexOfFirst { it.id == current.id } else -1
        if (currentIndex >= 0 && currentIndex < currentQueue.size) {
            currentQueue.add(currentIndex + 1, song)
        } else {
            currentQueue.add(song)
        }
        if (current == null) {
            playbackRepository.playSong(song, currentQueue)
        }
    }

    fun toggleFavorite(song: Song) {
        val newFav = !song.isFavorite
        playbackRepository.updateFavoriteStatus(song.id, newFav)
        viewModelScope.launch {
            songRepository.setFavorite(song.id, newFav)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun createPlaylist(name: String, songIdToAdd: Long? = null) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(trimmed)
            if (songIdToAdd != null) {
                playlistRepository.addSongToPlaylist(playlistId, songIdToAdd)
            }
        }
    }
}

