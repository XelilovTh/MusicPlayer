package com.tehmaz.musicplayer

import com.tehmaz.musicplayer.data.store.FavoritesStore
import com.tehmaz.musicplayer.data.store.HistoryStore
import com.tehmaz.musicplayer.data.store.KeyValueStore
import com.tehmaz.musicplayer.data.store.UserPlaylistStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreTest {

    @Test
    fun keyValueStore_withoutContext_behavesLikeEmptyStore() {
        val store = KeyValueStore(null)
        store.putString("a", "b")
        assertNull(store.getString("a"))
        assertTrue(store.getBoolean("flag", true))
        assertEquals(0.5f, store.getFloat("x", 0.5f), 0f)
    }

    @Test
    fun favoritesStore_togglesPerSong() {
        val favorites = FavoritesStore()
        assertFalse(favorites.isFavorite(1L))

        favorites.toggle(1L)
        favorites.toggle(2L)
        assertTrue(favorites.isFavorite(1L))
        assertEquals(setOf(1L, 2L), favorites.favoriteIds.value)

        favorites.toggle(1L)
        assertFalse(favorites.isFavorite(1L))
        assertTrue(favorites.isFavorite(2L))
    }

    @Test
    fun userPlaylistStore_createAddRemoveDelete() {
        val store = UserPlaylistStore()

        val id = store.create("  Gym  ", 10L)
        assertTrue(store.isUserPlaylist(id))
        assertEquals("Gym", store.playlists.value.single().name)
        assertEquals(listOf(10L), store.playlists.value.single().songIds)

        assertTrue(store.addSong(id, 11L))
        assertFalse("duplicates are ignored", store.addSong(id, 11L))
        assertFalse("unknown playlist", store.addSong(42L, 1L))
        assertEquals(listOf(10L, 11L), store.playlists.value.single().songIds)

        store.removeSong(id, 10L)
        assertEquals(listOf(11L), store.playlists.value.single().songIds)

        store.delete(id)
        assertTrue(store.playlists.value.isEmpty())
    }

    @Test
    fun userPlaylistStore_blankNameGetsDefaultAndIdsAreUnique() {
        val store = UserPlaylistStore()
        val first = store.create("   ")
        val second = store.create("Second")

        assertEquals("New playlist", store.playlists.value[0].name)
        assertTrue(second > first)
    }

    @Test
    fun historyStore_keepsMostRecentFirstWithoutDuplicates() {
        val history = HistoryStore()
        history.recordSong(1L)
        history.recordSong(2L)
        history.recordSong(1L)

        assertEquals(listOf(1L, 2L), history.recentSongIds())
    }

    @Test
    fun historyStore_recentSearchesAreDeduplicatedAndLimited() {
        val history = HistoryStore()
        history.recordSearch("a") // too short, ignored
        history.recordSearch("lofi")
        history.recordSearch("jazz")
        history.recordSearch("LOFI")

        assertEquals(listOf("LOFI", "jazz"), history.recentSearches())

        repeat(12) { history.recordSearch("query $it") }
        assertEquals(8, history.recentSearches().size)

        history.clearSearches()
        assertTrue(history.recentSearches().isEmpty())
    }
}
