package com.tehmaz.musicplayer.data.model

import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Domain model representing a music track.
 *
 * @property id Unique identifier (MediaStore audio ID).
 * @property title Name of the audio track.
 * @property artist Artist or creator name.
 * @property album Album name.
 * @property durationMs Track duration in milliseconds.
 * @property contentUri Content URI string or streamable URL.
 * @property albumArtUri Album artwork URI string or null.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val albumArtUri: String? = null
) {
    /**
     * Formats duration into a human-readable mm:ss or hh:mm:ss format.
     */
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "0:00"
            val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
            }
        }
}
