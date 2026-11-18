package com.flacify.music

import com.flacify.music.core.audio.haptics.HapticAudioAnalyzer
import com.flacify.music.core.audio.haptics.PcmRingBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

class MusicHapticsTest {

    @Test
    fun pcmRingBuffer_writeAndRead_preservesDownmixedSamples() {
        val ringBuffer = PcmRingBuffer(1024)

        // Generate 100 stereo 16-bit PCM frames
        val numFrames = 100
        val byteBuffer = ByteBuffer.allocate(numFrames * 2 * 2).order(ByteOrder.nativeOrder())
        val shortBuffer = byteBuffer.asShortBuffer()

        for (i in 0 until numFrames) {
            shortBuffer.put(16384.toShort()) // Channel 1 (approx 0.5f)
            shortBuffer.put(16384.toShort()) // Channel 2 (approx 0.5f)
        }

        ringBuffer.write(byteBuffer, channels = 2, rate = 44100)
        assertEquals(44100, ringBuffer.sampleRate)

        val destination = FloatArray(100)
        val readCount = ringBuffer.read(destination, 100)

        assertEquals(100, readCount)
        assertEquals(0.5f, destination[0], 0.01f)
        assertEquals(0.5f, destination[99], 0.01f)
    }

    @Test
    fun pcmRingBuffer_overflow_dropsOldestGracefully() {
        val capacity = 64
        val ringBuffer = PcmRingBuffer(capacity)

        // Write 80 mono frames (exceeding 64)
        val byteBuffer = ByteBuffer.allocate(80 * 2).order(ByteOrder.nativeOrder())
        val shortBuffer = byteBuffer.asShortBuffer()
        for (i in 0 until 80) {
            shortBuffer.put((i * 100).toShort())
        }

        ringBuffer.write(byteBuffer, channels = 1, rate = 44100)

        val destination = FloatArray(capacity)
        val readCount = ringBuffer.read(destination, capacity)

        assertEquals(capacity, readCount)
    }

    @Test
    fun hapticAudioAnalyzer_subBassSineWave_drivesBassEnvelope() {
        val sampleRate = 44100
        val analyzer = HapticAudioAnalyzer(sampleRate)

        // Generate 60 Hz pure sub-bass sine wave (1024 samples)
        val numSamples = 1024
        val samples = FloatArray(numSamples)
        for (i in 0 until numSamples) {
            samples[i] = sin(2.0 * Math.PI * 60.0 * i / sampleRate).toFloat() * 0.8f
        }

        val frame = analyzer.analyzeBlock(samples, numSamples)

        // Sub-bass should have strong energy
        assertTrue("Bass intensity should be significant for 60Hz input", frame.bassIntensity > 0.3f)
    }

    @Test
    fun hapticAudioAnalyzer_midVocalSineWave_drivesVocalEnvelope() {
        val sampleRate = 44100
        val analyzer = HapticAudioAnalyzer(sampleRate)

        // Generate 1000 Hz vocal/midrange sine wave (1024 samples)
        val numSamples = 1024
        val samples = FloatArray(numSamples)
        for (i in 0 until numSamples) {
            samples[i] = sin(2.0 * Math.PI * 1000.0 * i / sampleRate).toFloat() * 0.8f
        }

        val frame = analyzer.analyzeBlock(samples, numSamples)

        // Vocal/midrange band should be activated
        assertTrue("Vocal intensity should be significant for 1000Hz input", frame.vocalIntensity > 0.2f)
    }

    @Test
    fun hapticAudioAnalyzer_silence_producesZeroIntensity() {
        val analyzer = HapticAudioAnalyzer(44100)
        val silence = FloatArray(1024)

        val frame = analyzer.analyzeBlock(silence, 1024)

        assertEquals(0f, frame.bassIntensity, 0.05f)
        assertEquals(0f, frame.vocalIntensity, 0.05f)
        assertFalse(frame.transientTrigger)
        assertFalse(frame.highTransient)
    }
}
