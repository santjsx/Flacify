package com.aurel.music.feature.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    val currentSong = playbackRepository.currentSong
    val isPlaying = playbackRepository.isPlaying
    val currentPositionMs = playbackRepository.currentPositionMs
    val durationMs = playbackRepository.durationMs
    val repeatMode = playbackRepository.repeatMode
    val shuffleMode = playbackRepository.shuffleMode
    val queue = playbackRepository.queue

    fun togglePlayPause() {
        if (playbackRepository.isPlaying.value) {
            playbackRepository.pause()
        } else {
            playbackRepository.resume()
        }
    }

    fun seekTo(positionMs: Long) {
        playbackRepository.seekTo(positionMs)
    }

    fun skipToNext() {
        playbackRepository.skipToNext()
    }

    fun skipToPrevious() {
        playbackRepository.skipToPrevious()
    }

    fun toggleRepeat() {
        playbackRepository.toggleRepeatMode()
    }

    fun toggleShuffle() {
        playbackRepository.toggleShuffleMode()
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songRepository.setFavorite(song.id, !song.isFavorite)
        }
    }

    fun playQueueItem(song: Song) {
        playbackRepository.playSong(song, queue.value)
    }
}
