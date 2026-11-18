package com.flacify.music.core.audio.haptics

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import com.flacify.music.core.data.repository.UserPreferencesRepository
import com.flacify.music.core.model.HapticMode
import com.flacify.music.core.model.HapticStrength
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import java.nio.ByteBuffer
import java.util.ArrayDeque
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class MusicHapticsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val ringBuffer = PcmRingBuffer(16384)
    val hardwareController = HardwareHapticController(context)
    private val dspAnalyzer = HapticAudioAnalyzer()

    @Volatile
    private var streamSampleRate: Int = 44100
    @Volatile
    private var streamChannelCount: Int = 2
    @Volatile
    private var streamEncoding: Int = C.ENCODING_PCM_16BIT

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val dspExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "FlacifyMusicHapticsDSP").apply {
            priority = Thread.MAX_PRIORITY
        }
    }
    private val scope = CoroutineScope(dspExecutor.asCoroutineDispatcher() + SupervisorJob())

    private val _hapticMode = MutableStateFlow(HapticMode.OFF)
    val hapticMode: StateFlow<HapticMode> = _hapticMode.asStateFlow()

    private val _hapticStrength = MutableStateFlow(HapticStrength.BALANCED)
    val hapticStrength: StateFlow<HapticStrength> = _hapticStrength.asStateFlow()

    private val _syncOffsetMs = MutableStateFlow(0)
    val syncOffsetMs: StateFlow<Int> = _syncOffsetMs.asStateFlow()

    private var activeJob: Job? = null
    // 512 samples gives ~11.6ms resolution at 44.1kHz (doubled temporal accuracy)
    private val pcmChunk = FloatArray(512)

    private class ScheduledHapticFrame(
        val targetNanoTime: Long,
        val frame: HapticFrame
    )

    private val scheduledQueue = ArrayDeque<ScheduledHapticFrame>(128)

    /**
     * Official Media3 non-destructive pass-through audio processor.
     * Safely taps decoded PCM from any format (FLAC, MP3, AAC, Float, 24-bit) without throwing
     * UnhandledAudioFormatException and without altering downstream audio.
     */
    val teeProcessor = TeeAudioProcessor(object : TeeAudioProcessor.AudioBufferSink {
        override fun flush(sampleRate: Int, channelCount: Int, encoding: Int) {
            streamSampleRate = sampleRate
            streamChannelCount = channelCount
            streamEncoding = encoding
            ringBuffer.clear()
            synchronized(scheduledQueue) {
                scheduledQueue.clear()
            }
            hardwareController.stop()
        }

        override fun handleBuffer(buffer: ByteBuffer) {
            if (_hapticMode.value != HapticMode.OFF) {
                ringBuffer.write(buffer, streamChannelCount, streamSampleRate, streamEncoding)
            }
        }
    })

    // Alias for backward compatibility
    val tapProcessor = teeProcessor

    init {
        // Observe persisted user preferences
        scope.launch {
            userPreferencesRepository.musicHapticsMode.collectLatest { mode ->
                _hapticMode.value = mode
                hardwareController.activeMode = mode
                if (mode == HapticMode.OFF) {
                    synchronized(scheduledQueue) {
                        scheduledQueue.clear()
                    }
                    hardwareController.stop()
                }
            }
        }

        scope.launch {
            userPreferencesRepository.musicHapticsStrength.collectLatest { strength ->
                _hapticStrength.value = strength
                hardwareController.activeStrength = strength
            }
        }

        scope.launch {
            userPreferencesRepository.musicHapticsSyncOffsetMs.collectLatest { offset ->
                _syncOffsetMs.value = offset
            }
        }
    }

    /**
     * Resolves perceived audio output latency in milliseconds.
     * Bluetooth A2DP/LE introduces ~160ms of pipeline delay,
     * whereas built-in speakers and wired audio have ~65ms latency (AudioTrack buffer + HAL).
     */
    val currentAudioLatencyMs: Long
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                val isBluetooth = devices.any { device ->
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
                        device.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                        device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER
                    ))
                }
                if (isBluetooth) return 160L
            }
            return 65L
        }

    val currentAudioRouteName: String
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                val isBluetooth = devices.any { device ->
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
                        device.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                        device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER
                    ))
                }
                if (isBluetooth) return "Bluetooth Audio (~160ms base sync)"
            }
            return "Speaker / Wired (~65ms base sync)"
        }

    fun setMode(mode: HapticMode) {
        scope.launch {
            userPreferencesRepository.setMusicHapticsMode(mode)
            hardwareController.triggerPreview(mode)
        }
    }

    fun setStrength(strength: HapticStrength) {
        scope.launch {
            userPreferencesRepository.setMusicHapticsStrength(strength)
            hardwareController.triggerPreview(_hapticMode.value, strength)
        }
    }

    fun setSyncOffset(offsetMs: Int) {
        scope.launch {
            userPreferencesRepository.setMusicHapticsSyncOffsetMs(offsetMs)
        }
    }

    fun triggerTestBeat() {
        hardwareController.triggerPreview(_hapticMode.value)
    }

    fun start() {
        if (_hapticMode.value == HapticMode.OFF) return
        stop()

        activeJob = scope.launch {
            while (isActive) {
                val loopStartNs = System.nanoTime()

                // 1. Keep DSP sample rate synchronized with active stream
                dspAnalyzer.updateSampleRate(ringBuffer.sampleRate)

                // 2. Compute dynamic presentation lead time
                // Net Delay = base latency + user offset - 8ms motor rise time
                val baseLatencyMs = currentAudioLatencyMs
                val userOffsetMs = _syncOffsetMs.value
                val netDelayMs = (baseLatencyMs + userOffsetMs - 8L).coerceAtLeast(0L)
                val netDelayNs = netDelayMs * 1_000_000L

                // 3. Drain and analyze newly arrived PCM chunks from ring buffer
                while (isActive && ringBuffer.availableRead >= pcmChunk.size) {
                    val samplesRead = ringBuffer.read(pcmChunk, pcmChunk.size)
                    if (samplesRead > 0 && _hapticMode.value != HapticMode.OFF) {
                        val frame = dspAnalyzer.analyzeBlock(pcmChunk, samplesRead)
                        val targetTime = System.nanoTime() + netDelayNs

                        synchronized(scheduledQueue) {
                            // Guard against queue growth if paused
                            if (scheduledQueue.size > 96) {
                                scheduledQueue.pollFirst()
                            }
                            scheduledQueue.addLast(ScheduledHapticFrame(targetTime, frame))
                        }
                    }
                }

                // 4. Dispatch ready frames whose presentation timestamp has arrived
                val nowNs = System.nanoTime()
                while (isActive) {
                    val nextFrame: ScheduledHapticFrame? = synchronized(scheduledQueue) {
                        val head = scheduledQueue.peekFirst()
                        if (head != null && head.targetNanoTime <= nowNs + 1_500_000L) { // 1.5ms window
                            scheduledQueue.pollFirst()
                        } else {
                            null
                        }
                    }

                    if (nextFrame != null) {
                        hardwareController.dispatchFrame(nextFrame.frame)
                    } else {
                        break
                    }
                }

                // 5. Adaptive high-precision timing throttle (approx 8ms polling tick)
                val nextWaitNs: Long = synchronized(scheduledQueue) {
                    val head = scheduledQueue.peekFirst()
                    if (head != null) {
                        (head.targetNanoTime - System.nanoTime()).coerceIn(1_000_000L, 8_000_000L)
                    } else {
                        8_000_000L
                    }
                }

                val sleepMs = nextWaitNs / 1_000_000L
                if (sleepMs > 0) {
                    delay(sleepMs)
                } else {
                    yield()
                }
            }
        }
    }

    fun stop() {
        activeJob?.cancel()
        activeJob = null
        synchronized(scheduledQueue) {
            scheduledQueue.clear()
        }
        hardwareController.stop()
    }
}
