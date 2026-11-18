package com.flacify.music.core.audio.haptics

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-fidelity haptic event packet containing classified musical transients,
 * dynamic onset velocity, and continuous melodic envelopes.
 */
data class HapticFrame(
    val bassIntensity: Float,      // 0.0f to 1.0f (Sub-bass continuous foundation)
    val vocalIntensity: Float,     // 0.0f to 1.0f (Midrange / vocal envelope)
    val isKickDrum: Boolean,       // True on kick drum / heavy beat onset
    val isSnareHit: Boolean,       // True on snare / clap onset
    val isHiHatTick: Boolean,      // True on crisp hi-hat / cymbal tick
    val dynamicScale: Float = 0.8f // Dynamic velocity multiplier (0.35f to 1.0f)
) {
    // Backward compatibility properties
    val transientTrigger: Boolean get() = isKickDrum
    val highTransient: Boolean get() = isHiHatTick || isSnareHit
}

/**
 * Apple-grade Real-Time Audio DSP Analyzer implementing:
 * 1. Direct Form II Transposed Biquad Filter Bank (Sub-Bass 60Hz, Mid/Snare 1800Hz, High-Pass 5000Hz).
 * 2. SuperFlux Spectral Difference Onset Detection with moving mean/variance adaptive thresholding.
 * 3. Dynamic Velocity Scaling (distinguishing heavy downbeats from subtle ghost notes).
 * 4. Millisecond-accurate refractory debouncing per frequency band.
 */
class HapticAudioAnalyzer(
    sampleRate: Int = 44100
) {
    private var currentSampleRate = sampleRate

    // 1. Direct Form II Transposed Biquad Filters
    // Kick drum fundamental: 60Hz, Q=1.0
    private var subBassFilter = BiquadFilter.createBandpass(sampleRate, centerFreq = 60f, q = 1.0f)
    // Snare / clap / vocal punch: 1800Hz, Q=0.8
    private var midSnareFilter = BiquadFilter.createBandpass(sampleRate, centerFreq = 1800f, q = 0.8f)
    // Mid vocal phrasing: 1000Hz, Q=0.7
    private var midVocalFilter = BiquadFilter.createBandpass(sampleRate, centerFreq = 1000f, q = 0.7f)
    // Crisp percussion / hi-hats: 5000Hz high-pass
    private var highPassFilter = BiquadFilter.createHighpass(sampleRate, cutoffFreq = 5000f, q = 0.707f)

    // 2. Continuous RMS Envelopes & AGC Trackers
    private var bassEnergyEnvelope = 0f
    private var vocalEnergyEnvelope = 0f
    private var peakBassTracker = 0.08f
    private var peakVocalTracker = 0.08f

    // 3. Spectral Flux Tracking
    private var prevBassRms = 0f
    private var prevSnareRms = 0f
    private var prevHighRms = 0f
    private var peakFluxTracker = 0.08f

    // 4. Moving History for Adaptive Thresholding (40 frames)
    private val historySize = 40
    private val bassFluxHistory = FloatArray(historySize)
    private val snareFluxHistory = FloatArray(historySize)
    private val highFluxHistory = FloatArray(historySize)
    private var historyIndex = 0
    private var historyFilled = false

    // 5. Refractory Window Counters (in samples)
    private var processedSamplesTotal = 0L
    private var lastKickSample = -100_000L
    private var lastSnareSample = -100_000L
    private var lastHiHatSample = -100_000L

    fun updateSampleRate(newRate: Int) {
        if (newRate > 0 && newRate != currentSampleRate) {
            currentSampleRate = newRate
            subBassFilter = BiquadFilter.createBandpass(newRate, centerFreq = 60f, q = 1.0f)
            midSnareFilter = BiquadFilter.createBandpass(newRate, centerFreq = 1800f, q = 0.8f)
            midVocalFilter = BiquadFilter.createBandpass(newRate, centerFreq = 1000f, q = 0.7f)
            highPassFilter = BiquadFilter.createHighpass(newRate, cutoffFreq = 5000f, q = 0.707f)
        }
    }

    fun analyzeBlock(pcmSamples: FloatArray, count: Int): HapticFrame {
        if (count <= 0) return HapticFrame(0f, 0f, isKickDrum = false, isSnareHit = false, isHiHatTick = false, dynamicScale = 0.8f)

        processedSamplesTotal += count

        var bassSumSq = 0f
        var snareSumSq = 0f
        var vocalSumSq = 0f
        var highSumSq = 0f

        for (i in 0 until count) {
            val sample = pcmSamples[i]

            val bassSample = subBassFilter.process(sample)
            bassSumSq += bassSample * bassSample

            val snareSample = midSnareFilter.process(sample)
            snareSumSq += snareSample * snareSample

            val vocalSample = midVocalFilter.process(sample)
            vocalSumSq += vocalSample * vocalSample

            val highSample = highPassFilter.process(sample)
            highSumSq += highSample * highSample
        }

        // 1. Instantaneous Band RMS
        val currentBassRms = sqrt(bassSumSq / count)
        val currentSnareRms = sqrt(snareSumSq / count)
        val currentVocalRms = sqrt(vocalSumSq / count)
        val currentHighRms = sqrt(highSumSq / count)

        // 2. Smooth Envelopes (Fast Attack ~12ms, Smooth Decay ~150ms)
        bassEnergyEnvelope = updateEnvelope(bassEnergyEnvelope, currentBassRms, attack = 0.45f, decay = 0.08f)
        vocalEnergyEnvelope = updateEnvelope(vocalEnergyEnvelope, currentVocalRms, attack = 0.35f, decay = 0.06f)

        // 3. Adaptive Peak AGC
        peakBassTracker = max(peakBassTracker * 0.995f, bassEnergyEnvelope).coerceAtLeast(0.02f)
        peakVocalTracker = max(peakVocalTracker * 0.995f, vocalEnergyEnvelope).coerceAtLeast(0.02f)

        val normalizedBass = (bassEnergyEnvelope / peakBassTracker).coerceIn(0f, 1f)
        val normalizedVocal = (vocalEnergyEnvelope / peakVocalTracker).coerceIn(0f, 1f)

        // 4. Half-Wave Rectified Spectral Difference Flux: ΔE = max(0, E[t] - E[t-1])
        val bassFlux = max(0f, currentBassRms - prevBassRms)
        val snareFlux = max(0f, currentSnareRms - prevSnareRms)
        val highFlux = max(0f, currentHighRms - prevHighRms)

        prevBassRms = currentBassRms
        prevSnareRms = currentSnareRms
        prevHighRms = currentHighRms

        // 5. Update Sliding History & Calculate Moving Statistics (μ + k*σ)
        bassFluxHistory[historyIndex] = bassFlux
        snareFluxHistory[historyIndex] = snareFlux
        highFluxHistory[historyIndex] = highFlux
        historyIndex = (historyIndex + 1) % historySize
        if (historyIndex == 0) historyFilled = true

        val validCount = if (historyFilled) historySize else historyIndex.coerceAtLeast(1)

        val (bassMean, bassStdDev) = calculateStats(bassFluxHistory, validCount)
        val (snareMean, snareStdDev) = calculateStats(snareFluxHistory, validCount)
        val (highMean, highStdDev) = calculateStats(highFluxHistory, validCount)

        // 6. Refractory Periods (90ms kick, 80ms snare, 50ms hi-hat)
        val kickRefractorySamples = (currentSampleRate * 0.090).toLong()
        val snareRefractorySamples = (currentSampleRate * 0.080).toLong()
        val hiHatRefractorySamples = (currentSampleRate * 0.050).toLong()

        val kickAllowed = (processedSamplesTotal - lastKickSample) >= kickRefractorySamples
        val snareAllowed = (processedSamplesTotal - lastSnareSample) >= snareRefractorySamples
        val hiHatAllowed = (processedSamplesTotal - lastHiHatSample) >= hiHatRefractorySamples

        // 7. Onset Detection Logic
        val kickThreshold = max(0.020f, bassMean + 1.5f * bassStdDev)
        val isKickOnset = kickAllowed && bassFlux > kickThreshold && normalizedBass > 0.25f

        val snareThreshold = max(0.018f, snareMean + 1.6f * snareStdDev)
        val isSnareOnset = snareAllowed && snareFlux > snareThreshold && currentSnareRms > 0.02f

        val highThreshold = max(0.015f, highMean + 1.5f * highStdDev)
        val isHiHatOnset = hiHatAllowed && highFlux > highThreshold && currentHighRms > 0.015f

        // Spectral Dominance Arbitration between Snare (1.8kHz) and Hi-Hat (5kHz+)
        val isHighDominant = isHiHatOnset && (highFlux >= snareFlux || currentHighRms > currentSnareRms)
        val isSnareDominant = isSnareOnset && !isHighDominant

        // 8. Dynamic Velocity Scaling
        val activeFlux = when {
            isKickOnset -> bassFlux
            isSnareDominant -> snareFlux
            isHighDominant -> highFlux
            else -> 0f
        }

        if (activeFlux > 0f) {
            peakFluxTracker = max(peakFluxTracker * 0.992f, activeFlux).coerceAtLeast(0.04f)
        }
        val velocity = if (activeFlux > 0f) {
            (activeFlux / peakFluxTracker).pow(0.65f).coerceIn(0.35f, 1.0f)
        } else {
            0.8f
        }

        if (isKickOnset) lastKickSample = processedSamplesTotal
        if (isSnareDominant) lastSnareSample = processedSamplesTotal
        if (isHighDominant) lastHiHatSample = processedSamplesTotal

        return HapticFrame(
            bassIntensity = normalizedBass,
            vocalIntensity = normalizedVocal,
            isKickDrum = isKickOnset,
            isSnareHit = isSnareDominant && !isKickOnset, // Priority to kicks
            isHiHatTick = isHighDominant && !isKickOnset,
            dynamicScale = velocity
        )
    }

    private fun updateEnvelope(current: Float, target: Float, attack: Float, decay: Float): Float {
        val factor = if (target > current) attack else decay
        return current + factor * (target - current)
    }

    private fun calculateStats(history: FloatArray, count: Int): Pair<Float, Float> {
        var sum = 0f
        for (i in 0 until count) {
            sum += history[i]
        }
        val mean = sum / count

        var sumSqDiff = 0f
        for (i in 0 until count) {
            val diff = history[i] - mean
            sumSqDiff += diff * diff
        }
        val variance = sumSqDiff / count
        val stdDev = sqrt(variance)

        return Pair(mean, stdDev)
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
