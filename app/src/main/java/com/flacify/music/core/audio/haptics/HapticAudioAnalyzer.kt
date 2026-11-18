package com.flacify.music.core.audio.haptics

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class HapticFrame(
    val bassIntensity: Float,      // 0.0f to 1.0f (Sub-bass & kick drum envelope)
    val vocalIntensity: Float,     // 0.0f to 1.0f (Melodic swell envelope)
    val transientTrigger: Boolean, // True on sharp kick/bass drop onsets
    val highTransient: Boolean     // True on crisp hi-hat / snare transient ticks
)

/**
 * Real-time audio analysis engine implementing a Direct Form II Transposed
 * Biquad filter bank, spectral flux onset detector, and adaptive Auto Gain Control (AGC).
 */
class HapticAudioAnalyzer(
    sampleRate: Int = 44100
) {
    // 1. Direct Form II Transposed Biquad Filters
    private var subBassFilter = BiquadFilter.createBandpass(sampleRate, centerFreq = 65f, q = 0.8f)
    private var midVocalFilter = BiquadFilter.createBandpass(sampleRate, centerFreq = 1200f, q = 0.7f)
    private var highPassFilter = BiquadFilter.createHighpass(sampleRate, cutoffFreq = 4200f, q = 0.707f)

    private var currentSampleRate = sampleRate

    // 2. Dynamic Envelopes & Moving Averages
    private var bassEnergyEnvelope = 0f
    private var vocalEnergyEnvelope = 0f
    private var previousHighEnergy = 0f

    // 3. Adaptive AGC Parameters
    private var peakBassTracker = 0.08f
    private var peakVocalTracker = 0.08f

    fun updateSampleRate(newRate: Int) {
        if (newRate > 0 && newRate != currentSampleRate) {
            currentSampleRate = newRate
            subBassFilter = BiquadFilter.createBandpass(newRate, centerFreq = 65f, q = 0.8f)
            midVocalFilter = BiquadFilter.createBandpass(newRate, centerFreq = 1200f, q = 0.7f)
            highPassFilter = BiquadFilter.createHighpass(newRate, cutoffFreq = 4200f, q = 0.707f)
        }
    }

    fun analyzeBlock(pcmSamples: FloatArray, count: Int): HapticFrame {
        if (count <= 0) return HapticFrame(0f, 0f, transientTrigger = false, highTransient = false)

        var bassSumSq = 0f
        var vocalSumSq = 0f
        var highSumSq = 0f

        for (i in 0 until count) {
            val sample = pcmSamples[i]

            val bassSample = subBassFilter.process(sample)
            bassSumSq += bassSample * bassSample

            val vocalSample = midVocalFilter.process(sample)
            vocalSumSq += vocalSample * vocalSample

            val highSample = highPassFilter.process(sample)
            highSumSq += highSample * highSample
        }

        // Instantaneous RMS calculation
        val currentBassRms = sqrt(bassSumSq / count)
        val currentVocalRms = sqrt(vocalSumSq / count)
        val currentHighRms = sqrt(highSumSq / count)

        // Asymmetric Envelope Follower (Fast Attack 12ms, Smooth Decay 180ms)
        bassEnergyEnvelope = updateEnvelope(bassEnergyEnvelope, currentBassRms, attack = 0.45f, decay = 0.08f)
        vocalEnergyEnvelope = updateEnvelope(vocalEnergyEnvelope, currentVocalRms, attack = 0.35f, decay = 0.06f)

        // Adaptive AGC Peak Normalization
        peakBassTracker = max(peakBassTracker * 0.995f, bassEnergyEnvelope).coerceAtLeast(0.02f)
        peakVocalTracker = max(peakVocalTracker * 0.995f, vocalEnergyEnvelope).coerceAtLeast(0.02f)

        val normalizedBass = (bassEnergyEnvelope / peakBassTracker).coerceIn(0f, 1f)
        val normalizedVocal = (vocalEnergyEnvelope / peakVocalTracker).coerceIn(0f, 1f)

        // Spectral Flux Onset Detection on Bass (Beat drops) and Highs (Percussion ticks)
        val bassFlux = currentBassRms - bassEnergyEnvelope
        val isBassOnset = bassFlux > (peakBassTracker * 0.55f) && normalizedBass > 0.4f

        val highFlux = currentHighRms - previousHighEnergy
        previousHighEnergy = currentHighRms
        val isHighOnset = highFlux > 0.045f

        return HapticFrame(
            bassIntensity = normalizedBass,
            vocalIntensity = normalizedVocal,
            transientTrigger = isBassOnset,
            highTransient = isHighOnset
        )
    }

    private fun updateEnvelope(current: Float, target: Float, attack: Float, decay: Float): Float {
        val factor = if (target > current) attack else decay
        return current + factor * (target - current)
    }
}

/**
 * Standard Audio Biquad Filter (Direct Form II Transposed) for high numerical stability.
 */
class BiquadFilter private constructor(
    private var b0: Float, private var b1: Float, private var b2: Float,
    private var a1: Float, private var a2: Float
) {
    private var d1 = 0f
    private var d2 = 0f

    fun process(input: Float): Float {
        val output = b0 * input + d1
        d1 = b1 * input - a1 * output + d2
        d2 = b2 * input - a2 * output
        return output
    }

    companion object {
        fun createBandpass(sampleRate: Int, centerFreq: Float, q: Float): BiquadFilter {
            val w0 = (2.0 * Math.PI * centerFreq / sampleRate).toFloat()
            val alpha = (sin(w0.toDouble()) / (2.0 * q)).toFloat()

            val b0 = alpha
            val b1 = 0f
            val b2 = -alpha
            val a0 = 1f + alpha
            val a1 = (-2f * cos(w0))
            val a2 = 1f - alpha

            return BiquadFilter(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0)
        }

        fun createHighpass(sampleRate: Int, cutoffFreq: Float, q: Float): BiquadFilter {
            val w0 = (2.0 * Math.PI * cutoffFreq / sampleRate).toFloat()
            val cosW0 = cos(w0.toDouble()).toFloat()
            val alpha = (sin(w0.toDouble()) / (2.0 * q)).toFloat()

            val b0 = (1f + cosW0) / 2f
            val b1 = -(1f + cosW0)
            val b2 = (1f + cosW0) / 2f
            val a0 = 1f + alpha
            val a1 = -2f * cosW0
            val a2 = 1f - alpha

            return BiquadFilter(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0)
        }
    }
}
