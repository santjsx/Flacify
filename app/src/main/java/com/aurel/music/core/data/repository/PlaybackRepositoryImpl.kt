package com.aurel.music.core.data.repository

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.aurel.music.core.audio.service.AurelPlaybackService
import com.aurel.music.core.model.PlaybackState
import com.aurel.music.core.model.RepeatMode
import com.aurel.music.core.model.ShuffleMode
import com.aurel.music.core.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class PlaybackRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PlaybackRepository {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + Job())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    override val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    override val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    override val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    override val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private var positionTrackerJob: Job? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, AurelPlaybackService::class.java)
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future

        future.addListener({
            try {
                val controller = future.get()
                mediaController = controller
                setupPlayerListener(controller)
                syncInitialState(controller)
            } catch (e: Exception) {
                _playbackState.value = PlaybackState.Error("Failed to connect to audio service", e)
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val songId = mediaItem?.mediaId?.toLongOrNull()
                val activeSong = _queue.value.find { it.id == songId }
                _currentSong.value = activeSong
                _durationMs.value = controller.duration.coerceAtLeast(0L)
                _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
            }

            override fun onPlaybackStateChanged(state: Int) {
                _playbackState.value = when (state) {
                    Player.STATE_IDLE -> PlaybackState.Idle
                    Player.STATE_BUFFERING -> PlaybackState.Buffering
                    Player.STATE_READY -> if (controller.isPlaying) PlaybackState.Playing else PlaybackState.Paused
                    Player.STATE_ENDED -> PlaybackState.Ended
                    else -> PlaybackState.Idle
                }
                _durationMs.value = controller.duration.coerceAtLeast(0L)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    _playbackState.value = PlaybackState.Playing
                    startPositionTracking()
                } else {
                    if (controller.playbackState == Player.STATE_READY) {
                        _playbackState.value = PlaybackState.Paused
                    }
                    stopPositionTracking()
                }
            }

            override fun onRepeatModeChanged(repeatModeInt: Int) {
                _repeatMode.value = when (repeatModeInt) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleMode.value = if (shuffleModeEnabled) ShuffleMode.ON else ShuffleMode.OFF
            }
        })
    }

    private fun syncInitialState(controller: MediaController) {
        _isPlaying.value = controller.isPlaying
        _durationMs.value = controller.duration.coerceAtLeast(0L)
        _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
        _repeatMode.value = when (controller.repeatMode) {
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }
        _shuffleMode.value = if (controller.shuffleModeEnabled) ShuffleMode.ON else ShuffleMode.OFF
    }

    private fun startPositionTracking() {
        stopPositionTracking()
        positionTrackerJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
                    }
                }
                delay(250) // 4 updates/sec for smooth non-janky scrubber updates
            }
        }
    }

    private fun stopPositionTracking() {
        positionTrackerJob?.cancel()
        positionTrackerJob = null
    }

    override fun playSong(song: Song, newQueue: List<Song>) {
        val tracks = if (newQueue.isNotEmpty()) newQueue else listOf(song)
        val startIndex = tracks.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playQueue(tracks, startIndex)
    }

    override fun playQueue(songs: List<Song>, startIndex: Int) {
        _queue.value = songs
        val controller = mediaController ?: return

        val mediaItems = songs.map { it.toMediaItem() }
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
    }

    override fun pause() {
        mediaController?.pause()
    }

    override fun resume() {
        mediaController?.play()
    }

    override fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        mediaController?.seekTo(positionMs)
    }

    override fun skipToNext() {
        mediaController?.let {
            if (it.hasNextMediaItem()) {
                it.seekToNextMediaItem()
            }
        }
    }

    override fun skipToPrevious() {
        mediaController?.let {
            if (it.currentPosition > 3000L || !it.hasPreviousMediaItem()) {
                it.seekTo(0L)
            } else {
                it.seekToPreviousMediaItem()
            }
        }
    }

    override fun toggleRepeatMode() {
        val nextMode = _repeatMode.value.next()
        _repeatMode.value = nextMode
        mediaController?.repeatMode = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    override fun toggleShuffleMode() {
        val nextMode = _shuffleMode.value.toggle()
        _shuffleMode.value = nextMode
        mediaController?.shuffleModeEnabled = (nextMode == ShuffleMode.ON)
    }

    override fun removeFromQueue(index: Int) {
        val current = _queue.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _queue.value = current
            mediaController?.removeMediaItem(index)
        }
    }

    override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val current = _queue.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _queue.value = current
            mediaController?.moveMediaItem(fromIndex, toIndex)
        }
    }

    override fun clearQueue() {
        _queue.value = emptyList()
        mediaController?.clearMediaItems()
        _currentSong.value = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
    }

    override fun updateFavoriteStatus(songId: Long, isFavorite: Boolean) {
        if (_currentSong.value?.id == songId) {
            _currentSong.value = _currentSong.value?.copy(isFavorite = isFavorite)
        }
        _queue.value = _queue.value.map {
            if (it.id == songId) it.copy(isFavorite = isFavorite) else it
        }
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumTitle)
            .setArtworkUri("content://media/external/audio/albumart/$albumId".toUri())
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(mediaUri)
            .setMediaMetadata(metadata)
            .build()
    }
}

