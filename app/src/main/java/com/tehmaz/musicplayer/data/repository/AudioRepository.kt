package com.tehmaz.musicplayer.data.repository

import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song

/**
 * Interface for querying music tracks and playlists.
 */
interface AudioRepository {
    /**
     * Retrieves all available songs. If local MediaStore has audio files, they are returned;
     * otherwise, rich sample tracks are provided as fallback.
     */
    suspend fun getSongs(): List<Song>

    /**
     * Finds a single song by its ID.
     */
    suspend fun getSongById(id: Long): Song?

    /**
     * Searches songs matching query string across title, artist, or album.
     */
    suspend fun searchSongs(query: String): List<Song>

    /**
     * Retrieves all playlists.
     */
    suspend fun getPlaylists(): List<Playlist>

    /**
     * Retrieves a single playlist by its ID.
     */
    suspend fun getPlaylistById(id: Long): Playlist?

    /**
     * Retrieves recent or trending songs.
     */
    suspend fun getRecentSongs(): List<Song>

    /**
     * Returns true if currently utilizing fallback sample data (e.g. no local audio found).
     */
    fun isUsingFallbackData(): Boolean
}
