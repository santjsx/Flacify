package com.flacify.music.core.audio.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.flacify.music.MainActivity
import com.flacify.music.core.audio.equalizer.EqualizerController
import com.flacify.music.core.audio.interruption.BecomingNoisyReceiver
import com.flacify.music.core.audio.notification.FlacifyNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class FlacifyPlaybackService : MediaSessionService() {

    @Inject
    lateinit var equalizerController: EqualizerController

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private var becomingNoisyReceiver: BecomingNoisyReceiver? = null

    override fun onCreate() {
        super.onCreate()
        FlacifyNotificationHelper.createNotificationChannel(this)

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        exoPlayer = player

        // Bind active audio session to Equalizer
        equalizerController.bindAudioSession(player.audioSessionId)

        val sessionActivityIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityIntent)
            .build()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(FlacifyNotificationHelper.CHANNEL_ID)
            .setNotificationId(FlacifyNotificationHelper.NOTIFICATION_ID)
            .build()

        setMediaNotificationProvider(notificationProvider)

        becomingNoisyReceiver = BecomingNoisyReceiver(this) {
            exoPlayer?.pause()
        }.also { it.register() }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = exoPlayer
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        becomingNoisyReceiver?.unregister()
        becomingNoisyReceiver = null

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        super.onDestroy()
    }
}

