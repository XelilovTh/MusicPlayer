package com.tehmaz.musicplayer

import android.content.Context
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.repository.AudioRepositoryImpl
import com.tehmaz.musicplayer.data.scanner.AudioScanner
import io.mockk.coEvery
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
    private lateinit var repository: AudioRepositoryImpl

    @Before
    fun setup() {
        scanner = mockk(relaxed = true)
        context = mockk(relaxed = true)
        repository = AudioRepositoryImpl(scanner, context)
    }

    @Test
    fun getSongs_whenNoLocalSongs_returnsSampleSongs() = runTest {
        coEvery { scanner.scanLocalSongs() } returns emptyList()

        val songs = repository.getSongs()

        assertTrue(repository.isUsingFallbackData())
        assertEquals(10, songs.size)
        assertEquals("Sunset Vibes", songs[0].title)
        assertEquals("Dreamscape", songs[1].title)
        assertEquals("Chill Vibes", songs[2].title)
        assertEquals("Focus", songs[3].title)
        assertEquals("Golden Hour", songs[4].title)
        assertEquals("Midnight Dreams", songs[5].title)
        assertEquals("Ocean Breeze", songs[6].title)
        assertEquals("Night Drive", songs[7].title)
        assertEquals("Better Days", songs[8].title)
        assertEquals("Lost in Time", songs[9].title)
    }

    @Test
    fun getSongs_whenLocalSongsExist_returnsLocalSongs() = runTest {
        val mockSongs = listOf(
            Song(1L, "Local Song 1", "Local Artist", "Local Album", 180000L, "content://media/1"),
            Song(2L, "Local Song 2", "Local Artist", "Local Album", 210000L, "content://media/2")
        )
        coEvery { scanner.scanLocalSongs() } returns mockSongs

        val songs = repository.getSongs()

        assertFalse(repository.isUsingFallbackData())
        assertEquals(2, songs.size)
        assertEquals("Local Song 1", songs[0].title)
    }

    @Test
    fun getSongById_returnsCorrectSong() = runTest {
        coEvery { scanner.scanLocalSongs() } returns emptyList()

        val song = repository.getSongById(101L)
        assertNotNull(song)
        assertEquals("Sunset Vibes", song?.title)

        val nonExistent = repository.getSongById(9999L)
        assertNull(nonExistent)
    }

    @Test
    fun searchSongs_matchesQueryAcrossTitleArtistAlbum() = runTest {
        coEvery { scanner.scanLocalSongs() } returns emptyList()

        // Match title
        val resultTitle = repository.searchSongs("Sunset")
        assertEquals(1, resultTitle.size)
        assertEquals("Sunset Vibes", resultTitle[0].title)

        // Match artist
        val resultArtist = repository.searchSongs("lofi beats")
        assertEquals(2, resultArtist.size)

        // Match album
        val resultAlbum = repository.searchSongs("Golden Horizons")
        assertEquals(1, resultAlbum.size)
        assertEquals(101L, resultAlbum[0].id)

        // Case insensitivity and whitespace trimming
        val resultTrim = repository.searchSongs("   CHILL   ")
        assertTrue(resultTrim.isNotEmpty())

        // Blank query returns empty list
        val emptyQuery = repository.searchSongs("   ")
        assertTrue(emptyQuery.isEmpty())

        // Non-existent search
        val nonExistent = repository.searchSongs("UnknownNonExistentTrack404")
        assertTrue(nonExistent.isEmpty())
    }

    @Test
    fun getPlaylists_whenFallback_returnsCuratedPlaylists() = runTest {
        coEvery { scanner.scanLocalSongs() } returns emptyList()

        val playlists = repository.getPlaylists()
        assertEquals(3, playlists.size)
        assertEquals("Chill Vibes", playlists[0].name)
        assertEquals("Workout", playlists[1].name)
        assertEquals("Focus", playlists[2].name)
        assertEquals(5, playlists[0].songs.size)
    }

    @Test
    fun getPlaylists_whenLocalSongsExist_groupsPlaylistsByAlbum() = runTest {
        val mockSongs = listOf(
            Song(1L, "Track 1", "Artist A", "Album Alpha", 180000L, "uri1"),
            Song(2L, "Track 2", "Artist A", "Album Alpha", 200000L, "uri2"),
            Song(3L, "Track 3", "Artist B", "Album Beta", 210000L, "uri3")
        )
        coEvery { scanner.scanLocalSongs() } returns mockSongs

        val playlists = repository.getPlaylists()
        assertFalse(repository.isUsingFallbackData())
        // Should have "All Songs" + 2 albums ("Album Alpha", "Album Beta")
        assertEquals(3, playlists.size)
        assertEquals("All Songs", playlists[0].name)
        assertEquals(3, playlists[0].songCount)
        assertEquals("Album Alpha", playlists[1].name)
        assertEquals(2, playlists[1].songCount)
        assertEquals("Album Beta", playlists[2].name)
        assertEquals(1, playlists[2].songCount)
    }

    @Test
    fun getPlaylistById_returnsCorrectPlaylist() = runTest {
        coEvery { scanner.scanLocalSongs() } returns emptyList()

        val playlist1 = repository.getPlaylistById(1L)
        assertNotNull(playlist1)
        assertEquals("Chill Vibes", playlist1?.name)

        val nonExistent = repository.getPlaylistById(999L)
        assertNull(nonExistent)
    }

    @Test
    fun getRecentSongs_returnsMaxFiveSongs() = runTest {
        coEvery { scanner.scanLocalSongs() } returns emptyList()

        val recent = repository.getRecentSongs()
        assertEquals(5, recent.size)
        assertEquals(101L, recent[0].id)
        assertEquals(105L, recent[4].id)
    }
}
