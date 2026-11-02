package com.flacify.music.feature.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
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

    // Reactive single source of truth for favorite status of current track
    val isFavorite: StateFlow<Boolean> = playbackRepository.currentSong
        .flatMapLatest { song ->
            if (song != null) {
                songRepository.isFavoriteFlow(song.id)
            } else {
                flowOf(false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

    fun toggleFavorite(song: Song? = currentSong.value) {
        val target = song ?: currentSong.value ?: return
        val newFav = if (target.id == currentSong.value?.id) {
            !isFavorite.value
        } else {
            !target.isFavorite
        }
        // 1. Immediately update in-memory state for instant feedback
        playbackRepository.updateFavoriteStatus(target.id, newFav)
        // 2. Persist to Room database asynchronously
        viewModelScope.launch {
            songRepository.setFavorite(target.id, newFav)
        }
    }

    fun playQueueItem(song: Song) {
        playbackRepository.playSong(song, queue.value)
    }

    fun removeFromQueue(index: Int) {
        playbackRepository.removeFromQueue(index)
    }

    fun clearQueue() {
        playbackRepository.clearQueue()
    }
}

