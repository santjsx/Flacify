package com.flacify.music.feature.nowplaying

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flacify.music.core.data.lyrics.Lyrics
import com.flacify.music.core.data.repository.LyricsRepository
import com.flacify.music.core.data.repository.LyricsResult
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LyricsUiState {
    data object Loading : LyricsUiState
    data class Success(
        val lyrics: Lyrics,
        val isInstrumental: Boolean = false,
        val source: String = "unknown"
    ) : LyricsUiState
    data class Empty(val reason: String = "No lyrics available") : LyricsUiState
}

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,
    private val songRepository: SongRepository,
    private val lyricsRepository: LyricsRepository,
    @ApplicationContext private val context: Context
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

    private val lyricsRefreshTrigger = MutableStateFlow(0)

    val lyricsUiState: StateFlow<LyricsUiState> = combine(currentSong, lyricsRefreshTrigger) { song, _ -> song }
        .flatMapLatest { song ->
            if (song != null) {
                lyricsRepository.getLyrics(song).map { result ->
                    when (result) {
                        is LyricsResult.Loading -> LyricsUiState.Loading
                        is LyricsResult.Success -> LyricsUiState.Success(
                            lyrics = result.lyrics,
                            isInstrumental = result.isInstrumental,
                            source = result.source
                        )
                        is LyricsResult.Empty -> LyricsUiState.Empty(result.reason)
                    }
                }
            } else {
                flowOf(LyricsUiState.Empty("No track playing"))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LyricsUiState.Loading
        )

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

    fun refreshLyrics() {
        lyricsRefreshTrigger.value++
    }

    fun importLyricsFile(uri: Uri) {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }
                if (!content.isNullOrBlank()) {
                    lyricsRepository.importLyrics(song.id, content)
                    lyricsRefreshTrigger.value++
                }
            } catch (_: Exception) {
                // Ignore file read error
            }
        }
    }
}
