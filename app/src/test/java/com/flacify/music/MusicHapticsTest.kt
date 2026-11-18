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

        // Generate 60 Hz pure sub-bass sine wave (512 samples)
        val numSamples = 512
        val samples = FloatArray(numSamples)
        for (i in 0 until numSamples) {
            samples[i] = sin(2.0 * Math.PI * 60.0 * i / sampleRate).toFloat() * 0.8f
        }

        val frame = analyzer.analyzeBlock(samples, numSamples)

        // Sub-bass should have strong energy
        assertTrue("Bass intensity should be significant for 60Hz input", frame.bassIntensity > 0.2f)
    }

    @Test
    fun hapticAudioAnalyzer_midVocalSineWave_drivesVocalEnvelope() {
        val sampleRate = 44100
        val analyzer = HapticAudioAnalyzer(sampleRate)

        // Generate 1000 Hz vocal/midrange sine wave (512 samples)
        val numSamples = 512
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
        val silence = FloatArray(512)

        val frame = analyzer.analyzeBlock(silence, 512)

        assertEquals(0f, frame.bassIntensity, 0.05f)
        assertEquals(0f, frame.vocalIntensity, 0.05f)
        assertFalse(frame.isKickDrum)
        assertFalse(frame.isSnareHit)
        assertFalse(frame.isHiHatTick)
        assertFalse(frame.transientTrigger)
    }

    @Test
    fun hapticAudioAnalyzer_kickDrop_detectsTransientOnsetAndVelocity() {
        val sampleRate = 44100
        val analyzer = HapticAudioAnalyzer(sampleRate)

        // Prime the analyzer with quiet ambience (3 blocks)
        val quiet = FloatArray(512) { 0.005f }
        for (i in 0 until 3) {
            analyzer.analyzeBlock(quiet, 512)
        }

        // Sudden 60Hz heavy kick drum hit
        val kick = FloatArray(512)
        for (i in 0 until 512) {
            kick[i] = (sin(2.0 * Math.PI * 60.0 * i / sampleRate) * 0.9f).toFloat()
        }

        val kickFrame = analyzer.analyzeBlock(kick, 512)

        assertTrue("Kick drum hit should be detected as isKickDrum", kickFrame.isKickDrum)
        assertTrue("Kick drum should also map to transientTrigger for backward compat", kickFrame.transientTrigger)
        assertTrue("Dynamic velocity should be at least 0.35f", kickFrame.dynamicScale >= 0.35f)
    }

    @Test
    fun hapticAudioAnalyzer_hiHatImpulse_detectsHiHatTick() {
        val sampleRate = 44100
        val analyzer = HapticAudioAnalyzer(sampleRate)

        // Prime with quiet ambience
        val quiet = FloatArray(512) { 0.002f }
        for (i in 0 until 3) {
            analyzer.analyzeBlock(quiet, 512)
        }

        // Sudden 8000Hz high-frequency tick (hi-hat)
        val hiHat = FloatArray(512)
        for (i in 0 until 512) {
            hiHat[i] = (sin(2.0 * Math.PI * 8000.0 * i / sampleRate) * 0.6f).toFloat()
        }

        val hiHatFrame = analyzer.analyzeBlock(hiHat, 512)

        assertTrue("Hi-hat burst should trigger isHiHatTick", hiHatFrame.isHiHatTick)
        assertTrue("Hi-hat should map to highTransient", hiHatFrame.highTransient)
    }

    @Test
    fun hapticAudioAnalyzer_kickRefractoryPeriod_suppressesRapidConsecutiveHits() {
        val sampleRate = 44100
        val analyzer = HapticAudioAnalyzer(sampleRate)

        val quiet = FloatArray(512) { 0.005f }
        for (i in 0 until 3) analyzer.analyzeBlock(quiet, 512)

        val kick = FloatArray(512)
        for (i in 0 until 512) kick[i] = (sin(2.0 * Math.PI * 60.0 * i / sampleRate) * 0.9f).toFloat()

        val firstFrame = analyzer.analyzeBlock(kick, 512)
        assertTrue("First kick hit must trigger", firstFrame.isKickDrum)

        // Immediately following block (only 11.6ms later, well inside 90ms refractory window)
        val secondFrame = analyzer.analyzeBlock(kick, 512)
        assertFalse("Second kick within 11.6ms must be suppressed by refractory window", secondFrame.isKickDrum)
    }
}
