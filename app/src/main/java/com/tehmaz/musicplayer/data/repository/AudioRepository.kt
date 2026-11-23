package com.tehmaz.musicplayer.data.repository

import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song

/**
 * Interface for querying the user's local music and playlists.
 */
interface AudioRepository {
    /**
     * Retrieves all music found on the device. Empty if the audio permission is missing
     * or the device has no music.
     */
    suspend fun getSongs(): List<Song>

    /** Finds a single song by its ID. */
    suspend fun getSongById(id: Long): Song?

    /** Searches songs matching query string across title, artist, or album. */
    suspend fun searchSongs(query: String): List<Song>

    /** Retrieves all playlists (All Songs, albums and user-created playlists). */
    suspend fun getPlaylists(): List<Playlist>

    /** Retrieves a single playlist by its ID. */
    suspend fun getPlaylistById(id: Long): Playlist?

    /** Retrieves the recently played songs (falls back to the first songs when no history). */
    suspend fun getRecentSongs(): List<Song>

    /** Drops the cached scan so the next read queries MediaStore again, then notifies listeners. */
    suspend fun refresh()

    /** Records that a song started playing (listening history). */
    fun recordPlayed(songId: Long)

    /** True if the app is allowed to read audio files from the device. */
    fun hasPermission(): Boolean
}
