package com.flacify.music.core.audio.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.flacify.music.core.model.HapticMode

/**
 * Low-level hardware controller interfacing with Android's [VibratorManager] and [Vibrator].
 * Utilizes high-precision haptic primitives on API 31+ (Android 12+) with amplitude-modulated
 * waveform fallbacks for older devices.
 */
class HardwareHapticController(
    private val context: Context
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val hasPrimitiveSupport: Boolean by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator != null) {
            try {
                vibrator!!.areAllPrimitivesSupported(
                    VibrationEffect.Composition.PRIMITIVE_THUD,
                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                    VibrationEffect.Composition.PRIMITIVE_LOW_TICK
                )
            } catch (_: Exception) {
                false
            }
        } else {
            false
        }
    }

    private val hapticAttributes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        VibrationAttributes.Builder()
            .setUsage(VibrationAttributes.USAGE_MEDIA)
            .build()
    } else {
        null
    }

    @Volatile
    var activeMode: HapticMode = HapticMode.FULL_MIX

    private var lastTransientTimestamp = 0L
    private var lastResonanceTimestamp = 0L

    fun dispatchFrame(frame: HapticFrame) {
        val motor = vibrator ?: return
        if (activeMode == HapticMode.OFF) return

        val now = System.currentTimeMillis()

        when (activeMode) {
            HapticMode.FULL_MIX -> {
                // 1. Kick Drum / Beat Drop Transient Priority (Max 11 per second)
                if (frame.transientTrigger && (now - lastTransientTimestamp > 85L)) {
                    lastTransientTimestamp = now
                    triggerBeatTransient(motor, frame.bassIntensity)
                    return
                }

                // 2. High-Frequency Percussion Tick
                if (frame.highTransient && (now - lastTransientTimestamp > 60L)) {
                    triggerPercussionTick(motor)
                    return
                }

                // 3. Continuous Bass Rumble Waveform (sub-bass foundation)
                if (frame.bassIntensity > 0.40f && (now - lastResonanceTimestamp > 32L)) {
                    lastResonanceTimestamp = now
                    playContinuousResonance(motor, frame.bassIntensity * 0.65f)
                }
            }

            HapticMode.VOCALS_ONLY -> {
                // Smooth melodic vocal phrasing swell
                if (frame.vocalIntensity > 0.15f && (now - lastResonanceTimestamp > 30L)) {
                    lastResonanceTimestamp = now
                    playContinuousResonance(motor, frame.vocalIntensity)
                }
            }

            HapticMode.OFF -> Unit
        }
    }

    fun triggerPreview(mode: HapticMode) {
        val motor = vibrator ?: return
        when (mode) {
            HapticMode.FULL_MIX -> {
                triggerBeatTransient(motor, 0.9f)
            }
            HapticMode.VOCALS_ONLY -> {
                playContinuousResonance(motor, 0.8f)
            }
            HapticMode.OFF -> Unit
        }
    }

    private fun triggerBeatTransient(motor: Vibrator, intensity: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasPrimitiveSupport) {
            val scale = intensity.coerceIn(0.25f, 1.0f)
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, scale)
                .compose()
            vibrateWithAttributes(motor, effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = (intensity * 255).toInt().coerceIn(40, 255)
            motor.vibrate(VibrationEffect.createOneShot(25L, amplitude))
        }
    }

    private fun triggerPercussionTick(motor: Vibrator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasPrimitiveSupport) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.7f)
                .compose()
            vibrateWithAttributes(motor, effect)
        }
    }

    private fun playContinuousResonance(motor: Vibrator, normalizedIntensity: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = (normalizedIntensity * 160).toInt().coerceIn(1, 180)
            val effect = VibrationEffect.createOneShot(20L, amplitude)
            vibrateWithAttributes(motor, effect)
        }
    }

    private fun vibrateWithAttributes(motor: Vibrator, effect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hapticAttributes != null) {
            motor.vibrate(effect, hapticAttributes)
        } else {
            motor.vibrate(effect)
        }
    }

    fun stop() {
        vibrator?.cancel()
    }
}
