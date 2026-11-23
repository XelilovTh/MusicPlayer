package com.tehmaz.musicplayer.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tehmaz.musicplayer.MainActivity
import com.tehmaz.musicplayer.audio.EqualizerController
import org.koin.core.context.GlobalContext

/**
 * Android Jetpack Media3 background playback service.
 * Manages ExoPlayer instance, MediaSession, and system media notifications.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var equalizerController: EqualizerController? = null

    override fun onCreate() {
        super.onCreate()

        // Configure audio attributes for automatic focus management
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        // Attach the real equalizer / bass boost / virtualizer to this player's audio session.
        equalizerController = try {
            GlobalContext.get().get<EqualizerController>()
        } catch (_: Throwable) {
            null
        }
        equalizerController?.attach(player.audioSessionId)
        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                equalizerController?.attach(audioSessionId)
            }
        })

        // PendingIntent to launch MainActivity from lock screen / notification
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val callback = object : MediaSession.Callback {
            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {
                return Futures.immediateFuture(mediaItems)
            }

            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val connectionResult = super.onConnect(session, controller)
                val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
                val availablePlayerCommands = connectionResult.availablePlayerCommands.buildUpon()

                return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                    .setAvailableSessionCommands(availableSessionCommands.build())
                    .setAvailablePlayerCommands(availablePlayerCommands.build())
                    .build()
            }
        }

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(callback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        equalizerController?.release()
        equalizerController = null
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
