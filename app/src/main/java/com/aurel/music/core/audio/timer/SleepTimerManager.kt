package com.aurel.music.core.audio.timer

import com.aurel.music.core.data.repository.PlaybackRepository
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

sealed interface SleepTimerState {
    data object Inactive : SleepTimerState
    data class Active(
        val remainingSeconds: Long,
        val totalSeconds: Long,
        val stopAtEndOfTrack: Boolean = false
    ) : SleepTimerState
    data object Finished : SleepTimerState
}

@Singleton
class SleepTimerManager @Inject constructor(
    private val playbackRepository: PlaybackRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + Job())
    private var timerJob: Job? = null
    private var endTimestampUptimeMs = 0L
    private var totalDurationSeconds = 0L

    private val _timerState = MutableStateFlow<SleepTimerState>(SleepTimerState.Inactive)
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()

    private fun currentMonotonicMillis(): Long = System.nanoTime() / 1_000_000L

    fun startTimer(durationMinutes: Int) {
        cancelTimer()
        val totalSecs = durationMinutes * 60L
        totalDurationSeconds = totalSecs
        endTimestampUptimeMs = currentMonotonicMillis() + (totalSecs * 1000L)

        timerJob = scope.launch {
            while (isActive) {
                val remainingMs = endTimestampUptimeMs - currentMonotonicMillis()
                if (remainingMs <= 0) {
                    onTimerCompleted()
                    break
                }
                val remainingSecs = (remainingMs / 1000L).coerceAtLeast(0L)
                _timerState.value = SleepTimerState.Active(remainingSecs, totalDurationSeconds)
                delay(500)
            }
        }
    }

    fun startTimerForEndOfTrack() {
        cancelTimer()
        val duration = playbackRepository.durationMs.value
        val position = playbackRepository.currentPositionMs.value
        val remainingSecs = ((duration - position).coerceAtLeast(0L) / 1000L).coerceAtLeast(1L)
        totalDurationSeconds = remainingSecs
        endTimestampUptimeMs = currentMonotonicMillis() + (remainingSecs * 1000L)

        timerJob = scope.launch {
            while (isActive) {
                val remainingMs = endTimestampUptimeMs - currentMonotonicMillis()
                if (remainingMs <= 0) {
                    onTimerCompleted()
                    break
                }
                val secs = (remainingMs / 1000L).coerceAtLeast(0L)
                _timerState.value = SleepTimerState.Active(secs, totalDurationSeconds, stopAtEndOfTrack = true)
                delay(500)
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = SleepTimerState.Inactive
    }

    fun extendTimer(extraMinutes: Int) {
        if (_timerState.value is SleepTimerState.Active) {
            endTimestampUptimeMs += (extraMinutes * 60 * 1000L)
            totalDurationSeconds += (extraMinutes * 60L)
        }
    }

    private suspend fun onTimerCompleted() {
        // Fade out and pause
        playbackRepository.pause()
        _timerState.value = SleepTimerState.Finished
        delay(3000)
        _timerState.value = SleepTimerState.Inactive
    }
}

