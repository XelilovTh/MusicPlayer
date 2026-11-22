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
 * Exposes reactive StateFlow for the current song, position, duration, and playback status.
 * Provides resilient fallback for simulated playback when network or offline audio is unavailable.
 */
class AudioPlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    companion object {
        private const val TAG = "AudioPlaybackManager"
        private const val TICK_INTERVAL_MS = 250L
    }

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()

    private val songCache = mutableMapOf<String, Song>()

    private var mediaController: MediaController? = null
    private var progressJob: Job? = null
    private var isSimulatingOffline = false

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
                        Log.d(TAG, "MediaController successfully connected to PlaybackService.")
                    } catch (e: Exception) {
                        Log.w(TAG, "MediaController connection failed, using fallback controller mode: ${e.message}")
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Unable to create SessionToken: ${e.message}")
        }
    }

    private fun attachPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isSimulatingOffline = false
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTicker()
                } else {
                    stopProgressTicker()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (mediaItem == null) return
                val song = songCache[mediaItem.mediaId]
                    ?: _currentPlaylist.value.firstOrNull { it.id.toString() == mediaItem.mediaId }
                
                if (song != null) {
                    _playbackState.update {
                        it.copy(
                            currentSong = song,
                            currentPositionMs = 0L,
                            totalDurationMs = if (song.durationMs > 0) song.durationMs else 0L
                        )
                    }
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        val duration = player.duration
                        if (duration > 0) {
                            _playbackState.update { it.copy(totalDurationMs = duration) }
                        }
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.update { it.copy(isPlaying = false, currentPositionMs = it.totalDurationMs) }
                        stopProgressTicker()
                    }
                    else -> Unit
                }
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
                Log.w(TAG, "ExoPlayer error (${error.errorCode}): ${error.message}. Activating simulated playback.")
                // Seamlessly fall back to simulated ticker so UI animations & play state remain responsive
                isSimulatingOffline = true
                _playbackState.update { it.copy(isPlaying = true) }
                startProgressTicker()
            }
        })
    }

    /**
     * Plays the specified song and optionally sets the upcoming playlist queue.
     */
    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        val queue = if (playlist.isNotEmpty()) playlist else listOf(song)
        _currentPlaylist.value = queue
        queue.forEach { songCache[it.id.toString()] = it }

        _playbackState.update {
            it.copy(
                currentSong = song,
                isPlaying = true,
                currentPositionMs = 0L,
                totalDurationMs = song.durationMs
            )
        }

        val controller = mediaController
        if (controller != null) {
            try {
                val mediaItems = queue.map { it.toMediaItem() }
                val initialIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                controller.setMediaItems(mediaItems, initialIndex, 0L)
                controller.prepare()
                controller.play()
                startProgressTicker()
                return
            } catch (e: Exception) {
                Log.w(TAG, "Error playing song via MediaController: ${e.message}. Falling back to simulation.")
            }
        }

        // Fallback simulation when media controller is unavailable
        isSimulatingOffline = true
        startProgressTicker()
    }

    /**
     * Toggles between play and pause.
     */
    fun playPause() {
        val controller = mediaController
        val currentPlaying = _playbackState.value.isPlaying

        if (controller != null && !isSimulatingOffline) {
            if (currentPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        } else {
            // Simulated toggle
            val nextPlaying = !currentPlaying
            _playbackState.update { it.copy(isPlaying = nextPlaying) }
            if (nextPlaying) {
                startProgressTicker()
            } else {
                stopProgressTicker()
            }
        }
    }

    /**
     * Skips to the next song in the playlist.
     */
    fun next() {
        val controller = mediaController
        if (controller != null && controller.hasNextMediaItem() && !isSimulatingOffline) {
            controller.seekToNextMediaItem()
            return
        }

        // Manual playlist iteration
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

        if (controller != null && controller.hasPreviousMediaItem() && !isSimulatingOffline) {
            controller.seekToPreviousMediaItem()
            return
        }

        // Manual playlist iteration
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
        val controller = mediaController
        if (controller != null && !isSimulatingOffline) {
            controller.seekTo(safePos)
        }
        _playbackState.update { it.copy(currentPositionMs = safePos) }
    }

    /**
     * Toggles shuffle mode between true and false.
     */
    fun toggleShuffle() {
        val nextShuffle = !_playbackState.value.isShuffle
        val controller = mediaController
        if (controller != null) {
            controller.shuffleModeEnabled = nextShuffle
        }
        _playbackState.update { it.copy(isShuffle = nextShuffle) }
    }

    /**
     * Cycles repeat mode: OFF -> ALL -> ONE -> OFF.
     */
    fun toggleRepeat() {
        val currentMode = _playbackState.value.repeatMode
        val (nextMode, exoRepeatMode) = when (currentMode) {
            RepeatMode.OFF -> RepeatMode.ALL to Player.REPEAT_MODE_ALL
            RepeatMode.ALL -> RepeatMode.ONE to Player.REPEAT_MODE_ONE
            RepeatMode.ONE -> RepeatMode.OFF to Player.REPEAT_MODE_OFF
        }

        val controller = mediaController
        if (controller != null) {
            controller.repeatMode = exoRepeatMode
        }
        _playbackState.update { it.copy(repeatMode = nextMode) }
    }

    /**
     * Releases controller and background coroutine jobs.
     */
    fun release() {
        stopProgressTicker()
        mediaController?.release()
        mediaController = null
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                delay(TICK_INTERVAL_MS)
                val controller = mediaController
                if (controller != null && !isSimulatingOffline) {
                    val pos = controller.currentPosition.coerceAtLeast(0L)
                    val dur = controller.duration.let { if (it > 0) it else _playbackState.value.totalDurationMs }
                    _playbackState.update { it.copy(currentPositionMs = pos, totalDurationMs = dur) }
                } else {
                    // Simulated progress tick
                    val cur = _playbackState.value.currentPositionMs
                    val total = _playbackState.value.totalDurationMs
                    val next = cur + TICK_INTERVAL_MS
                    if (total > 0 && next >= total) {
                        handleTrackCompletion()
                    } else {
                        _playbackState.update { it.copy(currentPositionMs = next) }
                    }
                }
            }
        }
    }

    private fun handleTrackCompletion() {
        when (_playbackState.value.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0L)
            }
            RepeatMode.ALL -> {
                next()
            }
            RepeatMode.OFF -> {
                val playlist = _currentPlaylist.value
                val current = _playbackState.value.currentSong
                val currentIndex = playlist.indexOfFirst { it.id == current?.id }
                if (currentIndex >= 0 && currentIndex < playlist.size - 1) {
                    next()
                } else {
                    _playbackState.update { it.copy(isPlaying = false, currentPositionMs = it.totalDurationMs) }
                    stopProgressTicker()
                }
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
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
