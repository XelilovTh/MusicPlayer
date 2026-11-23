package com.tehmaz.musicplayer.data.model

import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Playback repeat modes matching Media3 capabilities.
 */
enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

/**
 * State representing audio playback status and progress.
 *
 * @property currentSong Currently active song, or null if none loaded.
 * @property isPlaying True if audio is actively playing.
 * @property currentPositionMs Current playback position in milliseconds.
 * @property totalDurationMs Total duration of current track in milliseconds.
 * @property isShuffle True if shuffle playback order is active.
 * @property repeatMode Active repeat mode (OFF, ALL, ONE).
 * @property errorMessage One-shot playback error to show to the user, or null.
 */
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val errorMessage: String? = null
) {
    /**
     * Normalized progress value between 0.0f and 1.0f.
     */
    val progress: Float
        get() = if (totalDurationMs > 0) {
            (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    /**
     * Formats current position into mm:ss.
     */
    val formattedPosition: String
        get() = formatTime(currentPositionMs)

    /**
     * Formats total duration into mm:ss.
     */
    val formattedDuration: String
        get() = formatTime(totalDurationMs)

    companion object {
        fun formatTime(ms: Long): String {
            if (ms <= 0) return "0:00"
            val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }
}
