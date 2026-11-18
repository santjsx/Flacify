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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import java.nio.ByteBuffer
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
        }

        override fun handleBuffer(buffer: ByteBuffer) {
            if (_hapticMode.value != HapticMode.OFF) {
                ringBuffer.write(buffer, streamChannelCount, streamSampleRate, streamEncoding)
            }
        }
    })

    // Alias for backward compatibility
    val tapProcessor = teeProcessor

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val dspExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "FlacifyMusicHapticsDSP").apply {
            priority = Thread.MAX_PRIORITY
        }
    }
    private val scope = CoroutineScope(dspExecutor.asCoroutineDispatcher() + SupervisorJob())

    private val _hapticMode = MutableStateFlow(HapticMode.OFF)
    val hapticMode: StateFlow<HapticMode> = _hapticMode.asStateFlow()

    private var activeJob: Job? = null
    private val pcmChunk = FloatArray(1024)

    init {
        // Observe persisted user preference
        scope.launch {
            userPreferencesRepository.musicHapticsMode.collectLatest { mode ->
                _hapticMode.value = mode
                hardwareController.activeMode = mode
                if (mode == HapticMode.OFF) {
                    hardwareController.stop()
                }
            }
        }
    }

    /**
     * Resolves perceived audio output latency in milliseconds.
     * Bluetooth A2DP typically introduces 120-160ms of pipeline delay,
     * whereas built-in speakers and wired audio have ~20-30ms latency.
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
                if (isBluetooth) return 140L
            }
            return 25L
        }

    fun setMode(mode: HapticMode) {
        scope.launch {
            userPreferencesRepository.setMusicHapticsMode(mode)
            hardwareController.triggerPreview(mode)
        }
    }

    fun start() {
        if (_hapticMode.value == HapticMode.OFF) return
        stop()

        activeJob = scope.launch {
            val frameDelayNs = 16_666_667L // 60 Hz interval (~16.6ms)

            while (isActive) {
                val startNs = System.nanoTime()

                // Keep DSP sample rate synchronized with active stream
                dspAnalyzer.updateSampleRate(ringBuffer.sampleRate)

                // Drain decoded PCM samples from lock-free buffer
                val samplesRead = ringBuffer.read(pcmChunk, pcmChunk.size)

                if (samplesRead > 0 && _hapticMode.value != HapticMode.OFF) {
                    val frame = dspAnalyzer.analyzeBlock(pcmChunk, samplesRead)
                    hardwareController.dispatchFrame(frame)
                }

                val elapsedNs = System.nanoTime() - startNs
                val sleepNs = frameDelayNs - elapsedNs
                if (sleepNs > 0) {
                    delay(sleepNs / 1_000_000L)
                }
            }
        }
    }

    fun stop() {
        activeJob?.cancel()
        activeJob = null
        hardwareController.stop()
    }
}
