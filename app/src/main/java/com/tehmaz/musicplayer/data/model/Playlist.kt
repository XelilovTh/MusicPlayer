package com.tehmaz.musicplayer.data.model

/**
 * Domain model representing a playlist or album collection.
 *
 * @property id Unique playlist identifier.
 * @property name Playlist title.
 * @property songCount Number of songs in the playlist.
 * @property coverArtUrl Cover image URL or URI string.
 * @property songs List of songs belonging to this playlist.
 */
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val coverArtUrl: String? = null,
    val songs: List<Song> = emptyList()
)
