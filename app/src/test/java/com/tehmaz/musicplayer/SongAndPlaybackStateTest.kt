package com.tehmaz.musicplayer

import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.RepeatMode
import com.tehmaz.musicplayer.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SongAndPlaybackStateTest {

    @Test
    fun testSongFormattedDuration() {
        val song1 = Song(
            id = 1,
            title = "Sunset Vibes",
            artist = "Chill Playlist",
            album = "Chill",
            durationMs = 204000L, // 3 mins 24 secs
            contentUri = "content://media/test/1"
        )
        assertEquals("3:24", song1.formattedDuration)

        val songZero = Song(
            id = 2,
            title = "Zero",
            artist = "Artist",
            album = "Album",
            durationMs = 0L,
            contentUri = "content://media/test/2"
        )
        assertEquals("0:00", songZero.formattedDuration)

        val songNegative = Song(
            id = 3,
            title = "Negative",
            artist = "Artist",
            album = "Album",
            durationMs = -5000L,
            contentUri = "content://media/test/3"
        )
        assertEquals("0:00", songNegative.formattedDuration)

        val songUnderOneMinute = Song(
            id = 4,
            title = "Short",
            artist = "Artist",
            album = "Album",
            durationMs = 45000L, // 45 secs
            contentUri = "content://media/test/4"
        )
        assertEquals("0:45", songUnderOneMinute.formattedDuration)

        val songOverOneHour = Song(
            id = 5,
            title = "Long Track",
            artist = "Artist",
            album = "Album",
            durationMs = 3665000L, // 1 hour 1 min 5 secs
            contentUri = "content://media/test/5"
        )
        assertEquals("1:01:05", songOverOneHour.formattedDuration)
    }

    @Test
    fun testPlaybackStateProgress() {
        val stateHalf = PlaybackState(
            currentPositionMs = 50000L,
            totalDurationMs = 100000L,
            isPlaying = true
        )
        assertEquals(0.5f, stateHalf.progress, 0.001f)
        assertEquals("0:50", stateHalf.formattedPosition)
        assertEquals("1:40", stateHalf.formattedDuration)

        // Progress capped at 1.0f even if position exceeds duration
        val stateOverflow = PlaybackState(
            currentPositionMs = 120000L,
            totalDurationMs = 100000L
        )
        assertEquals(1.0f, stateOverflow.progress, 0.001f)

        // Zero total duration returns 0f progress
        val stateZero = PlaybackState(
            currentPositionMs = 5000L,
            totalDurationMs = 0L
        )
        assertEquals(0.0f, stateZero.progress, 0.001f)
        assertEquals("0:05", stateZero.formattedPosition)
        assertEquals("0:00", stateZero.formattedDuration)

        // Negative values
        val stateNegative = PlaybackState(
            currentPositionMs = -100L,
            totalDurationMs = -500L
        )
        assertEquals(0.0f, stateNegative.progress, 0.001f)
        assertEquals("0:00", stateNegative.formattedPosition)
        assertEquals("0:00", stateNegative.formattedDuration)
    }

    @Test
    fun testPlaybackStateFormatTime() {
        assertEquals("0:00", PlaybackState.formatTime(0L))
        assertEquals("0:00", PlaybackState.formatTime(-1000L))
        assertEquals("0:09", PlaybackState.formatTime(9000L))
        assertEquals("0:59", PlaybackState.formatTime(59000L))
        assertEquals("1:00", PlaybackState.formatTime(60000L))
        assertEquals("10:35", PlaybackState.formatTime(635000L))
    }

    @Test
    fun testRepeatModeValues() {
        val modes = RepeatMode.values()
        assertEquals(3, modes.size)
        assertEquals(RepeatMode.OFF, modes[0])
        assertEquals(RepeatMode.ALL, modes[1])
        assertEquals(RepeatMode.ONE, modes[2])
    }

    @Test
    fun testPlaylistModel() {
        val song = Song(1L, "Song", "Artist", "Album", 1000L, "uri")
        val playlist = Playlist(
            id = 10L,
            name = "Test Playlist",
            songCount = 1,
            coverArtUrl = "http://art.jpg",
            songs = listOf(song)
        )
        assertEquals(10L, playlist.id)
        assertEquals("Test Playlist", playlist.name)
        assertEquals(1, playlist.songCount)
        assertEquals("http://art.jpg", playlist.coverArtUrl)
        assertEquals(1, playlist.songs.size)
        assertEquals("Song", playlist.songs[0].title)
    }
}
