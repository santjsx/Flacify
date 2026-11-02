package com.flacify.music

import com.flacify.music.core.audio.timer.SleepTimerManager
import com.flacify.music.core.audio.timer.SleepTimerState
import com.flacify.music.core.data.repository.PlaybackRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepTimerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val playbackRepository: PlaybackRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startTimer_activatesCountdownState() {
        val manager = SleepTimerManager(playbackRepository)
        manager.startTimer(15) // 15 minutes

        testDispatcher.scheduler.advanceTimeBy(100)
        val state = manager.timerState.value

        assertTrue(state is SleepTimerState.Active)
        val active = state as SleepTimerState.Active
        assertEquals(900L, active.totalSeconds) // 15 * 60 = 900
    }

    @Test
    fun cancelTimer_setsInactiveState() {
        val manager = SleepTimerManager(playbackRepository)
        manager.startTimer(10)
        manager.cancelTimer()

        assertEquals(SleepTimerState.Inactive, manager.timerState.value)
    }
}

