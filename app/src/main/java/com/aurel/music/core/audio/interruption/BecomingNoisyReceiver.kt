package com.aurel.music.core.audio.interruption

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

/**
 * Intercepts audio output changes (e.g. wired headphones unplugged, Bluetooth audio disconnected)
 * and dispatches a callback to immediately pause playback.
 */
class BecomingNoisyReceiver(
    private val context: Context,
    private val onBecomingNoisy: () -> Unit
) : BroadcastReceiver() {

    private var isRegistered = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            onBecomingNoisy()
        }
    }

    fun register() {
        if (!isRegistered) {
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            context.registerReceiver(this, filter)
            isRegistered = true
        }
    }

    fun unregister() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(this)
            } catch (_: IllegalArgumentException) {
                // Receiver was not registered or already unregistered
            } finally {
                isRegistered = false
            }
        }
    }
}

