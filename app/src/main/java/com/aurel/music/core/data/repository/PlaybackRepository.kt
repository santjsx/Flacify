package com.aurel.music.core.data.repository

import com.aurel.music.core.model.PlaybackState
import com.aurel.music.core.model.RepeatMode
import com.aurel.music.core.model.ShuffleMode
import com.aurel.music.core.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlaybackRepository {
    val currentSong: StateFlow<Song?>
    val playbackState: StateFlow<PlaybackState>
    val isPlaying: StateFlow<Boolean>
    val currentPositionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    val repeatMode: StateFlow<RepeatMode>
    val shuffleMode: StateFlow<ShuffleMode>
    val queue: StateFlow<List<Song>>

    fun playSong(song: Song, newQueue: List<Song> = emptyList())
    fun playQueue(songs: List<Song>, startIndex: Int = 0)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun skipToNext()
    fun skipToPrevious()
    fun toggleRepeatMode()
    fun toggleShuffleMode()
    fun removeFromQueue(index: Int)
    fun moveQueueItem(fromIndex: Int, toIndex: Int)
    fun clearQueue()
    fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)
}

