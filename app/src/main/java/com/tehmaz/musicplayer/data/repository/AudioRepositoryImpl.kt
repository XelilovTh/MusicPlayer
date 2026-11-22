package com.tehmaz.musicplayer.data.repository

import android.content.Context
import android.util.Log
import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.scanner.AudioScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Concrete repository implementation that scans local MediaStore audio files
 * and falls back to a curated sample catalog matching the app UI specifications.
 */
class AudioRepositoryImpl(
    private val scanner: AudioScanner,
    @Suppress("unused") private val context: Context
) : AudioRepository {

    companion object {
        private const val TAG = "AudioRepositoryImpl"

        // Curated high quality sample songs matching UI requirements
        val SAMPLE_SONGS = listOf(
            Song(
                id = 101L,
                title = "Sunset Vibes",
                artist = "Chill Playlist",
                album = "Golden Horizons",
                durationMs = 204000L, // 3:24
                contentUri = "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80"
            ),
            Song(
                id = 102L,
                title = "Dreamscape",
                artist = "Lofi Beats",
                album = "Midnight Clouds",
                durationMs = 165000L, // 2:45
                contentUri = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80"
            ),
            Song(
                id = 103L,
                title = "Chill Vibes",
                artist = "Chill Playlist",
                album = "Coffee & Rain",
                durationMs = 192000L, // 3:12
                contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80"
            ),
            Song(
                id = 104L,
                title = "Focus",
                artist = "LoFi",
                album = "Deep Work",
                durationMs = 220000L, // 3:40
                contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=600&auto=format&fit=crop&q=80"
            ),
            Song(
                id = 105L,
                title = "Golden Hour",
                artist = "Ambient",
                album = "Sunburst",
                durationMs = 236000L, // 3:56
                contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1495616811223-4d98c6e9c869?w=600&auto=format&fit=crop&q=80"
            ),
            Song(
                id = 106L,
                title = "Midnight Dreams",
                artist = "Lofi Beats",
                album = "Neon City",
                durationMs = 250000L, // 4:10
                contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80"
            ),
            Song(
                id = 107L,
                title = "Ocean Breeze",
                artist = "Chill Playlist",
                album = "Pacific Waves",
                durationMs = 252000L, // 4:12
                contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=600&auto=format&fit=crop&q=80"
            ),
            Song(
                id = 108L,
                title = "Night Drive",
                artist = "Chill Playlist",
                album = "Synthwave Cruise",
                durationMs = 236000L, // 3:56
                contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600&auto=format&fit=crop&q=80"
            ),
            Song(
                id = 109L,
                title = "Better Days",
                artist = "Chill Playlist",
                album = "Pastel Sky",
                durationMs = 200000L, // 3:20
                contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80"
            ),
            Song(
                id = 110L,
                title = "Lost in Time",
                artist = "Chill Playlist",
                album = "Analog Memories",
                durationMs = 248000L, // 4:08
                contentUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600&auto=format&fit=crop&q=80"
            )
        )

        // Sample playlists matching requirements
        val SAMPLE_PLAYLISTS = listOf(
            Playlist(
                id = 1L,
                name = "Chill Vibes",
                songCount = 5,
                coverArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                songs = listOf(
                    SAMPLE_SONGS[0], // Sunset Vibes
                    SAMPLE_SONGS[2], // Chill Vibes
                    SAMPLE_SONGS[6], // Ocean Breeze
                    SAMPLE_SONGS[8], // Better Days
                    SAMPLE_SONGS[9]  // Lost in Time
                )
            ),
            Playlist(
                id = 2L,
                name = "Workout",
                songCount = 4,
                coverArtUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600&auto=format&fit=crop&q=80",
                songs = listOf(
                    SAMPLE_SONGS[7], // Night Drive
                    SAMPLE_SONGS[3], // Focus
                    SAMPLE_SONGS[4], // Golden Hour
                    SAMPLE_SONGS[5]  // Midnight Dreams
                )
            ),
            Playlist(
                id = 3L,
                name = "Focus",
                songCount = 4,
                coverArtUrl = "https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=600&auto=format&fit=crop&q=80",
                songs = listOf(
                    SAMPLE_SONGS[3], // Focus
                    SAMPLE_SONGS[1], // Dreamscape
                    SAMPLE_SONGS[4], // Golden Hour
                    SAMPLE_SONGS[5]  // Midnight Dreams
                )
            )
        )
    }

    private val mutex = Mutex()
    private var cachedSongs: List<Song>? = null
    private var usingFallback: Boolean = false

    override fun isUsingFallbackData(): Boolean = usingFallback

    override suspend fun getSongs(): List<Song> = withContext(Dispatchers.IO) {
        mutex.withLock {
            cachedSongs?.let { return@withContext it }

            val localSongs = scanner.scanLocalSongs()
            val result = if (localSongs.isNotEmpty()) {
                usingFallback = false
                Log.d(TAG, "Loaded ${localSongs.size} songs from local storage.")
                localSongs
            } else {
                usingFallback = true
                Log.d(TAG, "No local songs found or permission pending; loading rich sample catalog.")
                SAMPLE_SONGS
            }
            cachedSongs = result
            result
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
        if (usingFallback) {
            return@withContext SAMPLE_PLAYLISTS
        }

        // When local songs exist, build dynamic playlists from albums / artists
        val albumPlaylists = songs.groupBy { it.album }.entries.mapIndexed { index, (albumName, albumSongs) ->
            Playlist(
                id = (index + 10).toLong(),
                name = albumName,
                songCount = albumSongs.size,
                coverArtUrl = albumSongs.firstOrNull()?.albumArtUri,
                songs = albumSongs
            )
        }

        // Also include standard playlists populated with local songs if available
        val generalPlaylist = Playlist(
            id = 1L,
            name = "All Songs",
            songCount = songs.size,
            coverArtUrl = songs.firstOrNull()?.albumArtUri,
            songs = songs
        )

        listOf(generalPlaylist) + albumPlaylists
    }

    override suspend fun getPlaylistById(id: Long): Playlist? = withContext(Dispatchers.IO) {
        getPlaylists().firstOrNull { it.id == id }
    }

    override suspend fun getRecentSongs(): List<Song> = withContext(Dispatchers.IO) {
        // Return first 5 songs as recent/trending
        getSongs().take(5)
    }
}
