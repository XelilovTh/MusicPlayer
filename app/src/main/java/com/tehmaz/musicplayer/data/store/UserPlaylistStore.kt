package com.tehmaz.musicplayer.data.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A playlist created by the user. Only song ids are stored; songs are resolved at read time.
 */
data class UserPlaylist(
    val id: Long,
    val name: String,
    val songIds: List<Long>
)

/**
 * Persistent store for user-created playlists. Works purely in memory when [store] is null.
 */
class UserPlaylistStore(private val store: KeyValueStore? = null) {

    companion object {
        /** User playlist ids start above this value so they never collide with built-in ones. */
        const val ID_BASE = 1_000_000L
        private const val KEY = "user_playlists"
    }

    private val _playlists = MutableStateFlow(load())
    val playlists: StateFlow<List<UserPlaylist>> = _playlists.asStateFlow()

    /** Creates a playlist and returns its id. */
    fun create(name: String, firstSongId: Long? = null): Long {
        val cleanName = name.replace('\t', ' ').replace('\n', ' ').trim().ifEmpty { "New playlist" }
        val newId = (_playlists.value.maxOfOrNull { it.id } ?: ID_BASE) + 1
        val songs = if (firstSongId != null) listOf(firstSongId) else emptyList()
        commit(_playlists.value + UserPlaylist(newId, cleanName, songs))
        return newId
    }

    /** Adds a song to a playlist. Returns false if the playlist is unknown or already has the song. */
    fun addSong(playlistId: Long, songId: Long): Boolean {
        val target = _playlists.value.firstOrNull { it.id == playlistId } ?: return false
        if (songId in target.songIds) return false
        commit(_playlists.value.map {
            if (it.id == playlistId) it.copy(songIds = it.songIds + songId) else it
        })
        return true
    }

    fun removeSong(playlistId: Long, songId: Long) {
        commit(_playlists.value.map {
            if (it.id == playlistId) it.copy(songIds = it.songIds.filter { id -> id != songId }) else it
        })
    }

    fun delete(playlistId: Long) {
        commit(_playlists.value.filter { it.id != playlistId })
    }

    fun isUserPlaylist(playlistId: Long): Boolean = playlistId > ID_BASE

    private fun commit(updated: List<UserPlaylist>) {
        _playlists.value = updated
        val serialized = updated.joinToString("\n") { playlist ->
            "${playlist.id}\t${playlist.name}\t${playlist.songIds.joinToString(",")}"
        }
        store?.putString(KEY, serialized)
        LibraryEvents.notifyChanged()
    }

    private fun load(): List<UserPlaylist> {
        val raw = store?.getString(KEY) ?: return emptyList()
        return raw.split('\n').mapNotNull { line ->
            val parts = line.split('\t')
            if (parts.size < 2) return@mapNotNull null
            val id = parts[0].toLongOrNull() ?: return@mapNotNull null
            val ids = parts.getOrNull(2)
                ?.split(',')
                ?.mapNotNull { it.trim().toLongOrNull() }
                ?: emptyList()
            UserPlaylist(id, parts[1], ids)
        }
    }
}
