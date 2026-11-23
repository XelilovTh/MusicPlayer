package com.tehmaz.musicplayer.data.repository

import android.content.Context
import android.util.Log
import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.scanner.AudioScanner
import com.tehmaz.musicplayer.data.store.HistoryStore
import com.tehmaz.musicplayer.data.store.LibraryEvents
import com.tehmaz.musicplayer.data.store.UserPlaylistStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Repository backed by the device's MediaStore. There is no demo/placeholder catalog:
 * without the audio permission (or without music) the library is simply empty.
 */
class AudioRepositoryImpl(
    private val scanner: AudioScanner,
    @Suppress("unused") private val context: Context,
    private val userPlaylists: UserPlaylistStore = UserPlaylistStore(),
    private val history: HistoryStore = HistoryStore()
) : AudioRepository {

    companion object {
        private const val TAG = "AudioRepositoryImpl"
        private const val ALL_SONGS_ID = 1L
        private const val ALBUM_ID_OFFSET = 10L
        private const val RECENT_LIMIT = 8
    }

    private val mutex = Mutex()

    @Volatile
    private var cachedSongs: List<Song>? = null

    override fun hasPermission(): Boolean = scanner.hasPermission()

    override suspend fun getSongs(): List<Song> = withContext(Dispatchers.IO) {
        mutex.withLock {
            cachedSongs?.let { return@withContext it }

            // Without permission nothing is cached, so granting it later takes effect immediately.
            if (!scanner.hasPermission()) {
                return@withContext emptyList()
            }

            val songs = scanner.scanLocalSongs()
            Log.d(TAG, "Loaded ${songs.size} songs from local storage.")
            cachedSongs = songs
            songs
        }
    }

    override suspend fun getSongById(id: Long): Song? = withContext(Dispatchers.IO) {
        getSongs().firstOrNull { it.id == id }
    }

    override suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val normalized = query.trim().lowercase()
        getSongs().filter { song ->
            song.title.lowercase().contains(normalized) ||
                song.artist.lowercase().contains(normalized) ||
                song.album.lowercase().contains(normalized)
        }
    }

    override suspend fun getPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        val songs = getSongs()
        val result = mutableListOf<Playlist>()

        if (songs.isNotEmpty()) {
            result += Playlist(
                id = ALL_SONGS_ID,
                name = "All Songs",
                songCount = songs.size,
                coverArtUrl = songs.firstOrNull { !it.albumArtUri.isNullOrEmpty() }?.albumArtUri,
                songs = songs
            )

            songs.groupBy { it.album }.entries.forEachIndexed { index, (albumName, albumSongs) ->
                result += Playlist(
                    id = index + ALBUM_ID_OFFSET,
                    name = albumName,
                    songCount = albumSongs.size,
                    coverArtUrl = albumSongs.firstOrNull { !it.albumArtUri.isNullOrEmpty() }?.albumArtUri,
                    songs = albumSongs
                )
            }
        }

        userPlaylists.playlists.value.forEach { userPlaylist ->
            val resolved = userPlaylist.songIds.mapNotNull { id -> songs.firstOrNull { it.id == id } }
            result += Playlist(
                id = userPlaylist.id,
                name = userPlaylist.name,
                songCount = resolved.size,
                coverArtUrl = resolved.firstOrNull { !it.albumArtUri.isNullOrEmpty() }?.albumArtUri,
                songs = resolved
            )
        }

        result
    }

    override suspend fun getPlaylistById(id: Long): Playlist? = withContext(Dispatchers.IO) {
        getPlaylists().firstOrNull { it.id == id }
    }

    override suspend fun getRecentSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = getSongs()
        val recent = history.recentSongIds().mapNotNull { id -> songs.firstOrNull { it.id == id } }
        if (recent.isNotEmpty()) recent.take(RECENT_LIMIT) else songs.take(5)
    }

    override suspend fun refresh() {
        mutex.withLock { cachedSongs = null }
        LibraryEvents.notifyChanged()
    }

    override fun recordPlayed(songId: Long) {
        history.recordSong(songId)
    }
}
