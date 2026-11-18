package com.flacify.music.core.audio.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.flacify.music.core.model.HapticMode
import com.flacify.music.core.model.HapticStrength

/**
 * Low-level hardware controller interfacing with Android's [VibratorManager] and [Vibrator].
 * Utilizes high-precision haptic primitives on API 31+ (Android 12+) with amplitude-modulated
 * waveform fallbacks for older devices.
 *
 * Implements a strict Transient Lockout Window to prevent continuous rumble from cancelling
 * ongoing physical primitive strokes (THUD, CLICK, LOW_TICK) in the Android Vibrator HAL.
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

    @Volatile
    var activeStrength: HapticStrength = HapticStrength.BALANCED

    // Anti-cancellation: Lockout timestamp during which continuous rumble is suppressed
    @Volatile
    private var transientLockoutUntil = 0L

    private var lastKickTimestamp = 0L
    private var lastSnareTimestamp = 0L
    private var lastHiHatTimestamp = 0L
    private var lastResonanceTimestamp = 0L

    fun dispatchFrame(frame: HapticFrame) {
        val motor = vibrator ?: return
        if (activeMode == HapticMode.OFF) return

        val now = System.currentTimeMillis()
        val strengthMult = activeStrength.multiplier

        when (activeMode) {
            HapticMode.FULL_MIX -> {
                // 1. Kick Drum / Beat Drop Transient Priority (PRIMITIVE_THUD)
                if (frame.isKickDrum && (now - lastKickTimestamp > 85L)) {
                    lastKickTimestamp = now
                    transientLockoutUntil = now + 65L // 65ms lockout for physical actuator thud
                    triggerKickTransient(motor, frame.dynamicScale * strengthMult)
                    return
                }

                // 2. Snare / Clap Transient Priority (PRIMITIVE_CLICK)
                if (frame.isSnareHit && (now - lastSnareTimestamp > 75L)) {
                    lastSnareTimestamp = now
                    transientLockoutUntil = now + 40L // 40ms lockout for snare click
                    triggerSnareTransient(motor, frame.dynamicScale * 0.9f * strengthMult)
                    return
                }

                // 3. Crisp Hi-Hat / Percussion Tick (PRIMITIVE_LOW_TICK)
                if (frame.isHiHatTick && (now - lastHiHatTimestamp > 50L)) {
                    lastHiHatTimestamp = now
                    transientLockoutUntil = now + 25L // 25ms lockout for hi-hat tick
                    triggerHiHatTick(motor, frame.dynamicScale * 0.7f * strengthMult)
                    return
                }

                // 4. Continuous Sub-Bass Rumble (Only when no transient is actively executing)
                if (now >= transientLockoutUntil && frame.bassIntensity > 0.38f && (now - lastResonanceTimestamp > 32L)) {
                    lastResonanceTimestamp = now
                    playContinuousResonance(motor, frame.bassIntensity * 0.60f * strengthMult)
                }
            }

            HapticMode.VOCALS_ONLY -> {
                // Smooth melodic vocal phrasing swell
                if (frame.vocalIntensity > 0.15f && (now - lastResonanceTimestamp > 30L)) {
                    lastResonanceTimestamp = now
                    playContinuousResonance(motor, frame.vocalIntensity * strengthMult)
                }
            }

            HapticMode.OFF -> Unit
        }
    }

    fun triggerPreview(mode: HapticMode, strength: HapticStrength = activeStrength) {
        val motor = vibrator ?: return
        val strengthMult = strength.multiplier
        when (mode) {
            HapticMode.FULL_MIX -> {
                transientLockoutUntil = System.currentTimeMillis() + 65L
                triggerKickTransient(motor, 0.95f * strengthMult)
            }
            HapticMode.VOCALS_ONLY -> {
                playContinuousResonance(motor, 0.85f * strengthMult)
            }
            HapticMode.OFF -> Unit
        }
    }

    private fun triggerKickTransient(motor: Vibrator, intensity: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasPrimitiveSupport) {
                val scale = intensity.coerceIn(0.25f, 1.0f)
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, scale)
                    .compose()
                vibrateWithAttributes(motor, effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (intensity * 255).toInt().coerceIn(40, 255)
                motor.vibrate(VibrationEffect.createOneShot(30L, amplitude))
            }
        } catch (_: Throwable) {
            // Guard against device restrictions or hardware permission variations
        }
    }

    private fun triggerSnareTransient(motor: Vibrator, intensity: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasPrimitiveSupport) {
                val scale = intensity.coerceIn(0.25f, 1.0f)
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, scale)
                    .compose()
                vibrateWithAttributes(motor, effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (intensity * 220).toInt().coerceIn(35, 230)
                motor.vibrate(VibrationEffect.createOneShot(18L, amplitude))
            }
        } catch (_: Throwable) {
            // Guard against device restrictions or hardware permission variations
        }
    }

    private fun triggerHiHatTick(motor: Vibrator, intensity: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasPrimitiveSupport) {
                val scale = intensity.coerceIn(0.20f, 1.0f)
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, scale)
                    .compose()
                vibrateWithAttributes(motor, effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (intensity * 180).toInt().coerceIn(25, 180)
                motor.vibrate(VibrationEffect.createOneShot(10L, amplitude))
            }
        } catch (_: Throwable) {
            // Guard against device restrictions or hardware permission variations
        }
    }

    private fun playContinuousResonance(motor: Vibrator, normalizedIntensity: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (normalizedIntensity * 160).toInt().coerceIn(1, 180)
                val effect = VibrationEffect.createOneShot(20L, amplitude)
                vibrateWithAttributes(motor, effect)
            }
        } catch (_: Throwable) {
            // Guard against device restrictions or hardware permission variations
        }
    }

    private fun vibrateWithAttributes(motor: Vibrator, effect: VibrationEffect) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hapticAttributes != null) {
                motor.vibrate(effect, hapticAttributes)
            } else {
                motor.vibrate(effect)
            }
        } catch (_: Throwable) {
            // Guard against device restrictions or hardware permission variations
        }
    }

    fun stop() {
        try {
            transientLockoutUntil = 0L
            vibrator?.cancel()
        } catch (_: Throwable) {
            // Ignore cancel errors
        }
    }
}
