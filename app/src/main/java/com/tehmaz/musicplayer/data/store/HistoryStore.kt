package com.tehmaz.musicplayer.data.store

/**
 * Persistent listening history (recently played songs) and recent search queries.
 * Works purely in memory when [store] is null.
 */
class HistoryStore(private val store: KeyValueStore? = null) {

    companion object {
        private const val KEY_SONGS = "recent_song_ids"
        private const val KEY_SEARCHES = "recent_searches"
        private const val MAX_SONGS = 20
        private const val MAX_SEARCHES = 8
    }

    private var songIds: List<Long> = loadSongIds()
    private var searches: List<String> = loadSearches()

    /** Most recently played first. */
    @Synchronized
    fun recentSongIds(): List<Long> = songIds

    @Synchronized
    fun recordSong(songId: Long) {
        songIds = (listOf(songId) + songIds.filter { it != songId }).take(MAX_SONGS)
        store?.putString(KEY_SONGS, songIds.joinToString(","))
    }

    /** Most recent first. */
    @Synchronized
    fun recentSearches(): List<String> = searches

    @Synchronized
    fun recordSearch(query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        searches = (listOf(clean) + searches.filterNot { it.equals(clean, ignoreCase = true) })
            .take(MAX_SEARCHES)
        store?.putString(KEY_SEARCHES, searches.joinToString("\n"))
    }

    @Synchronized
    fun clearSearches() {
        searches = emptyList()
        store?.putString(KEY_SEARCHES, null)
    }

    private fun loadSongIds(): List<Long> {
        val raw = store?.getString(KEY_SONGS) ?: return emptyList()
        return raw.split(',').mapNotNull { it.trim().toLongOrNull() }
    }

    private fun loadSearches(): List<String> {
        val raw = store?.getString(KEY_SEARCHES) ?: return emptyList()
        return raw.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
    }
}
