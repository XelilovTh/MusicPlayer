package com.tehmaz.musicplayer

import android.content.Context
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.repository.AudioRepositoryImpl
import com.tehmaz.musicplayer.data.scanner.AudioScanner
import com.tehmaz.musicplayer.data.store.HistoryStore
import com.tehmaz.musicplayer.data.store.UserPlaylistStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AudioRepositoryTest {

    private lateinit var scanner: AudioScanner
    private lateinit var context: Context
    private lateinit var userPlaylists: UserPlaylistStore
    private lateinit var history: HistoryStore
    private lateinit var repository: AudioRepositoryImpl

    private val songA1 = Song(1L, "Track 1", "Artist A", "Album Alpha", 180000L, "uri1")
    private val songA2 = Song(2L, "Track 2", "Artist A", "Album Alpha", 200000L, "uri2")
    private val songB1 = Song(3L, "Sunset Drive", "Artist B", "Album Beta", 210000L, "uri3")
    private val localSongs = listOf(songA1, songA2, songB1)

    @Before
    fun setup() {
        scanner = mockk(relaxed = true)
        context = mockk(relaxed = true)
        userPlaylists = UserPlaylistStore()
        history = HistoryStore()
        every { scanner.hasPermission() } returns true
        coEvery { scanner.scanLocalSongs() } returns localSongs
        repository = AudioRepositoryImpl(scanner, context, userPlaylists, history)
    }

    @Test
    fun getSongs_withoutPermission_returnsEmptyAndNeverScans() = runTest {
        every { scanner.hasPermission() } returns false

        val songs = repository.getSongs()

        assertTrue(songs.isEmpty())
        coVerify(exactly = 0) { scanner.scanLocalSongs() }
    }

    @Test
    fun getSongs_afterPermissionGranted_returnsLocalSongs() = runTest {
        every { scanner.hasPermission() } returns false
        assertTrue(repository.getSongs().isEmpty())

        // The user grants the permission: no restart / manual refresh needed.
        every { scanner.hasPermission() } returns true
        val songs = repository.getSongs()

        assertEquals(3, songs.size)
        assertEquals("Track 1", songs[0].title)
    }

    @Test
    fun getSongs_isCachedUntilRefresh() = runTest {
        repository.getSongs()
        repository.getSongs()
        coVerify(exactly = 1) { scanner.scanLocalSongs() }

        repository.refresh()
        repository.getSongs()
        coVerify(exactly = 2) { scanner.scanLocalSongs() }
    }

    @Test
    fun getSongs_whenDeviceHasNoMusic_isEmpty() = runTest {
        coEvery { scanner.scanLocalSongs() } returns emptyList()

        assertTrue(repository.getSongs().isEmpty())
        assertTrue(repository.getPlaylists().isEmpty())
        assertTrue(repository.getRecentSongs().isEmpty())
    }

    @Test
    fun getSongById_returnsCorrectSong() = runTest {
        assertEquals("Track 2", repository.getSongById(2L)?.title)
        assertNull(repository.getSongById(9999L))
    }

    @Test
    fun searchSongs_matchesQueryAcrossTitleArtistAlbum() = runTest {
        assertEquals(1, repository.searchSongs("Sunset").size)
        assertEquals(2, repository.searchSongs("artist a").size)
        assertEquals(1, repository.searchSongs("Album Beta").size)
        assertTrue(repository.searchSongs("   SUNSET   ").isNotEmpty())
        assertTrue(repository.searchSongs("   ").isEmpty())
        assertTrue(repository.searchSongs("UnknownNonExistentTrack404").isEmpty())
    }

    @Test
    fun getPlaylists_groupsByAlbumAfterAllSongs() = runTest {
        val playlists = repository.getPlaylists()

        assertEquals(3, playlists.size)
        assertEquals("All Songs", playlists[0].name)
        assertEquals(3, playlists[0].songCount)
        assertEquals("Album Alpha", playlists[1].name)
        assertEquals(2, playlists[1].songCount)
        assertEquals("Album Beta", playlists[2].name)
        assertEquals(1, playlists[2].songCount)
    }

    @Test
    fun getPlaylists_includesUserPlaylistsWithResolvedSongs() = runTest {
        val id = userPlaylists.create("Road trip", songA1.id)
        userPlaylists.addSong(id, songB1.id)
        userPlaylists.addSong(id, 9999L) // song no longer on the device

        val playlist = repository.getPlaylistById(id)

        assertNotNull(playlist)
        assertEquals("Road trip", playlist?.name)
        assertEquals(listOf(songA1, songB1), playlist?.songs)
        assertEquals(2, playlist?.songCount)
    }

    @Test
    fun getPlaylistById_returnsCorrectPlaylist() = runTest {
        assertEquals("All Songs", repository.getPlaylistById(1L)?.name)
        assertNull(repository.getPlaylistById(999L))
    }

    @Test
    fun getRecentSongs_withoutHistory_returnsEmptyInsteadOfInventingRecentPlays() = runTest {
        assertTrue(repository.getRecentSongs().isEmpty())
    }

    @Test
    fun getRecentSongs_usesListeningHistoryMostRecentFirst() = runTest {
        repository.recordPlayed(1L)
        repository.recordPlayed(3L)
        repository.recordPlayed(1L)

        val recent = repository.getRecentSongs()

        assertEquals(listOf(1L, 3L), recent.map { it.id })
        assertFalse(recent.any { it.id == 2L })
    }
}
