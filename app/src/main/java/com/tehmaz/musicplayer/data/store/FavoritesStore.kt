package com.tehmaz.musicplayer.data.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Persistent set of favorite song ids. Works purely in memory when [store] is null.
 */
class FavoritesStore(private val store: KeyValueStore? = null) {

    companion object {
        private const val KEY = "favorite_song_ids"
    }

    private val _favoriteIds = MutableStateFlow(load())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    fun isFavorite(songId: Long): Boolean = songId in _favoriteIds.value

    fun toggle(songId: Long) {
        _favoriteIds.update { current ->
            if (songId in current) current - songId else current + songId
        }
        store?.putString(KEY, _favoriteIds.value.joinToString(","))
    }

    private fun load(): Set<Long> {
        val raw = store?.getString(KEY) ?: return emptySet()
        return raw.split(',').mapNotNull { it.trim().toLongOrNull() }.toSet()
    }
}
