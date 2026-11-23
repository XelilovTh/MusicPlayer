package com.tehmaz.musicplayer.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.RepeatMode
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.service.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages audio playback via Jetpack Media3 MediaController.
 *
 * Exposes reactive StateFlows for the current song, position, duration and playback status.
 * The MediaController connects asynchronously: a play request made before it is ready is
 * remembered and replayed on connection. There is no fake/simulated playback; real player
 * errors are surfaced through [PlaybackState.errorMessage].
 */
class AudioPlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    companion object {
        private const val TAG = "AudioPlaybackManager"
        private const val TICK_INTERVAL_MS = 250L
        private const val SLEEP_TICK_MS = 1000L
    }

    private data class PendingPlayback(val queue: List<Song>, val startIndex: Int)

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()

    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)

    /** Remaining sleep timer time in milliseconds, or null when no timer is running. */
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs.asStateFlow()

    /** Invoked whenever a track starts; used to record listening history. */
    var onSongStarted: ((Song) -> Unit)? = null

    private val songCache = mutableMapOf<String, Song>()

    private var mediaController: MediaController? = null
    private var pendingPlayback: PendingPlayback? = null
    private var progressJob: Job? = null
    private var sleepJob: Job? = null
    private var consecutiveErrors = 0

    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        try {
            val sessionToken = SessionToken(
                context,
                ComponentName(context, PlaybackService::class.java)
            )

            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture.addListener(
                {
                    try {
                        val controller = controllerFuture.get()
                        mediaController = controller
                        attachPlayerListener(controller)
                        applyPendingState(controller)
                        Log.d(TAG, "MediaController connected to PlaybackService.")
                    } catch (e: Exception) {
                        Log.w(TAG, "MediaController connection failed: ${e.message}")
                        _playbackState.update {
                            it.copy(isPlaying = false, errorMessage = "Playback service is unavailable")
                        }
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Unable to create SessionToken: ${e.message}")
        }
    }

    /** Applies shuffle/repeat chosen before connection and replays a request made too early. */
    private fun applyPendingState(controller: MediaController) {
        val state = _playbackState.value
        controller.shuffleModeEnabled = state.isShuffle
        controller.repeatMode = state.repeatMode.toPlayerRepeatMode()

        val pending = pendingPlayback ?: return
        pendingPlayback = null
        try {
            startQueue(controller, pending.queue, pending.startIndex, _playbackState.value.isPlaying)
            if (_playbackState.value.isPlaying) startProgressTicker()
        } catch (e: Exception) {
            Log.w(TAG, "Unable to start pending playback: ${e.message}")
        }
    }

    private fun attachPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) consecutiveErrors = 0
                syncPlayingState(player)
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                syncPlayingState(player)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (mediaItem == null) return
                val song = songCache[mediaItem.mediaId]
                    ?: _currentPlaylist.value.firstOrNull { it.id.toString() == mediaItem.mediaId }
                    ?: return

                notifySongStarted(song)
                _playbackState.update {
                    it.copy(
                        currentSong = song,
                        currentPositionMs = 0L,
                        totalDurationMs = if (song.durationMs > 0) song.durationMs else 0L
                    )
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        consecutiveErrors = 0
                        val duration = player.duration
                        if (duration > 0) {
                            _playbackState.update { it.copy(totalDurationMs = duration) }
                        }
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.update { it.copy(currentPositionMs = it.totalDurationMs) }
                    }
                    else -> Unit
                }
                syncPlayingState(player)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.update { it.copy(isShuffle = shuffleModeEnabled) }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                _playbackState.update { it.copy(repeatMode = mode) }
            }

            override fun onPlayerError(error: PlaybackException) {
                consecutiveErrors++
                Log.w(TAG, "Player error (${error.errorCode}): ${error.message}")
                val message = describeError(error)

                // Skip an unplayable track, but never loop forever over a fully broken queue.
                val canSkip = player.hasNextMediaItem() && consecutiveErrors < player.mediaItemCount
                if (canSkip) {
                    _playbackState.update { it.copy(errorMessage = message) }
                    player.seekToNextMediaItem()
                    player.prepare()
                    player.play()
                } else {
                    _playbackState.update { it.copy(isPlaying = false, errorMessage = message) }
                    stopProgressTicker()
                }
            }
        })
    }

    /** The UI shows "playing" while playback is wanted, including short buffering phases. */
    private fun syncPlayingState(player: Player) {
        val state = player.playbackState
        val shouldShowPlaying = player.playWhenReady &&
            state != Player.STATE_ENDED &&
            state != Player.STATE_IDLE
        _playbackState.update { it.copy(isPlaying = shouldShowPlaying) }
        if (shouldShowPlaying) startProgressTicker() else stopProgressTicker()
    }

    private fun describeError(error: PlaybackException): String {
        return when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "No internet connection"
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "This file could not be found"
            else -> "Couldn't play this track"
        }
    }

    /**
     * Plays the specified song and optionally sets the upcoming playlist queue.
     */
    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        val baseQueue = if (playlist.isNotEmpty()) playlist else listOf(song)
        val queue = if (baseQueue.any { it.id == song.id }) baseQueue else listOf(song) + baseQueue
        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        _currentPlaylist.value = queue
        queue.forEach { songCache[it.id.toString()] = it }
        consecutiveErrors = 0
        notifySongStarted(song)

        _playbackState.update {
            it.copy(
                currentSong = song,
                isPlaying = true,
                currentPositionMs = 0L,
                totalDurationMs = song.durationMs,
                errorMessage = null
            )
        }

        val controller = mediaController
        if (controller == null) {
            // The controller connects asynchronously; replay this request once it is ready.
            pendingPlayback = PendingPlayback(queue, startIndex)
            return
        }

        try {
            startQueue(controller, queue, startIndex, true)
            startProgressTicker()
        } catch (e: Exception) {
            Log.w(TAG, "Error playing song: ${e.message}")
            _playbackState.update {
                it.copy(isPlaying = false, errorMessage = "Couldn't start playback")
            }
        }
    }

    private fun startQueue(
        controller: MediaController,
        queue: List<Song>,
        startIndex: Int,
        playWhenReady: Boolean
    ) {
        controller.setMediaItems(queue.map { it.toMediaItem() }, startIndex, 0L)
        controller.prepare()
        if (playWhenReady) controller.play() else controller.pause()
    }

    /**
     * Toggles between play and pause. Also restarts a finished queue and recovers from errors.
     */
    fun playPause() {
        val controller = mediaController
        val wantsPlaying = _playbackState.value.isPlaying

        if (controller == null) {
            // Not connected yet: remember the intent, it is applied on connection.
            _playbackState.update { it.copy(isPlaying = !wantsPlaying) }
            return
        }

        if (wantsPlaying) {
            controller.pause()
            return
        }

        when (controller.playbackState) {
            Player.STATE_ENDED -> {
                controller.seekToDefaultPosition(0)
                controller.play()
            }
            Player.STATE_IDLE -> {
                controller.prepare()
                controller.play()
            }
            else -> controller.play()
        }
        _playbackState.update { it.copy(isPlaying = true, errorMessage = null) }
        startProgressTicker()
    }

    /**
     * Skips to the next song in the playlist.
     */
    fun next() {
        val controller = mediaController
        if (controller != null && controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
            return
        }

        // End of queue (or not connected): continue manually, wrapping to the first song.
        val playlist = _currentPlaylist.value
        val current = _playbackState.value.currentSong
        if (playlist.isEmpty()) return

        val currentIndex = playlist.indexOfFirst { it.id == current?.id }
        val nextIndex = if (_playbackState.value.isShuffle) {
            playlist.indices.filter { it != currentIndex }.randomOrNull() ?: 0
        } else {
            (currentIndex + 1) % playlist.size
        }

        val nextSong = playlist.getOrNull(nextIndex) ?: playlist.first()
        playSong(nextSong, playlist)
    }

    /**
     * Skips to previous song or rewinds current track if > 3 seconds elapsed.
     */
    fun previous() {
        val controller = mediaController
        if (_playbackState.value.currentPositionMs > 3000L) {
            seekTo(0L)
            return
        }

        if (controller != null && controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
            return
        }

        val playlist = _currentPlaylist.value
        val current = _playbackState.value.currentSong
        if (playlist.isEmpty()) return

        val currentIndex = playlist.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
        val prevSong = playlist.getOrNull(prevIndex) ?: playlist.first()
        playSong(prevSong, playlist)
    }

    /**
     * Seeks to the specified position in milliseconds.
     */
    fun seekTo(positionMs: Long) {
        val safePos = positionMs.coerceAtLeast(0L)
        mediaController?.seekTo(safePos)
        _playbackState.update { it.copy(currentPositionMs = safePos) }
    }

    /**
     * Toggles shuffle mode between true and false.
     */
    fun toggleShuffle() {
        val nextShuffle = !_playbackState.value.isShuffle
        mediaController?.shuffleModeEnabled = nextShuffle
        _playbackState.update { it.copy(isShuffle = nextShuffle) }
    }

    /**
     * Cycles repeat mode: OFF -> ALL -> ONE -> OFF.
     */
    fun toggleRepeat() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        mediaController?.repeatMode = nextMode.toPlayerRepeatMode()
        _playbackState.update { it.copy(repeatMode = nextMode) }
    }

    /** Clears the one-shot error after the UI has shown it. */
    fun clearError() {
        _playbackState.update { it.copy(errorMessage = null) }
    }

    /**
     * Starts (or restarts) the sleep timer. Playback is paused when it reaches zero.
     */
    fun startSleepTimer(minutes: Int) {
        if (minutes <= 0) {
            cancelSleepTimer()
            return
        }
        sleepJob?.cancel()
        _sleepTimerRemainingMs.value = minutes * 60_000L
        sleepJob = scope.launch {
            var remaining = minutes * 60_000L
            while (remaining > 0L && isActive) {
                _sleepTimerRemainingMs.value = remaining
                delay(SLEEP_TICK_MS)
                remaining -= SLEEP_TICK_MS
            }
            if (isActive) {
                _sleepTimerRemainingMs.value = null
                pauseFromSleepTimer()
            }
        }
    }

    /** Cancels a running sleep timer. */
    fun cancelSleepTimer() {
        sleepJob?.cancel()
        sleepJob = null
        _sleepTimerRemainingMs.value = null
    }

    private fun pauseFromSleepTimer() {
        mediaController?.pause()
        _playbackState.update { it.copy(isPlaying = false) }
        stopProgressTicker()
    }

    /**
     * Releases controller and background coroutine jobs.
     */
    fun release() {
        stopProgressTicker()
        cancelSleepTimer()
        mediaController?.release()
        mediaController = null
    }

    private fun startProgressTicker() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                delay(TICK_INTERVAL_MS)
                val controller = mediaController ?: continue
                val position = controller.currentPosition.coerceAtLeast(0L)
                val duration = controller.duration.let {
                    if (it > 0) it else _playbackState.value.totalDurationMs
                }
                _playbackState.update {
                    it.copy(currentPositionMs = position, totalDurationMs = duration)
                }
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun notifySongStarted(song: Song) {
        try {
            onSongStarted?.invoke(song)
        } catch (e: Exception) {
            Log.w(TAG, "onSongStarted failed: ${e.message}")
        }
    }

    private fun RepeatMode.toPlayerRepeatMode(): Int = when (this) {
        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
    }

    private fun Song.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(Uri.parse(contentUri))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
    }
}
