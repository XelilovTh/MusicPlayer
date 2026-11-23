package com.tehmaz.musicplayer

import android.content.Context
import com.tehmaz.musicplayer.data.model.RepeatMode
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioPlaybackManagerTest {

    private lateinit var context: Context
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var manager: AudioPlaybackManager

    private val testSong1 = Song(1L, "Song 1", "Artist 1", "Album 1", 100000L, "http://test/1.mp3")
    private val testSong2 = Song(2L, "Song 2", "Artist 2", "Album 2", 120000L, "http://test/2.mp3")
    private val testSong3 = Song(3L, "Song 3", "Artist 3", "Album 3", 150000L, "http://test/3.mp3")

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        manager = AudioPlaybackManager(context, testScope)
    }

    @Test
    fun initialState_isEmpty() {
        val state = manager.playbackState.value
        assertNull(state.currentSong)
        assertFalse(state.isPlaying)
        assertEquals(0L, state.currentPositionMs)
        assertEquals(RepeatMode.OFF, state.repeatMode)
        assertFalse(state.isShuffle)
    }

    @Test
    fun playSong_updatesStateImmediately() = runTest {
        manager.playSong(testSong1, listOf(testSong1, testSong2, testSong3))

        val state = manager.playbackState.value
        assertEquals(testSong1, state.currentSong)
        assertTrue(state.isPlaying)
        assertEquals(100000L, state.totalDurationMs)
        assertEquals(3, manager.currentPlaylist.value.size)
    }

    @Test
    fun playSong_withEmptyPlaylist_createsSingleItemQueue() {
        manager.playSong(testSong1, emptyList())

        val state = manager.playbackState.value
        assertEquals(testSong1, state.currentSong)
        assertEquals(1, manager.currentPlaylist.value.size)
        assertEquals(testSong1, manager.currentPlaylist.value[0])
    }

    @Test
    fun playPause_togglesPlayState() {
        manager.playSong(testSong1)
        assertTrue(manager.playbackState.value.isPlaying)

        manager.playPause()
        assertFalse(manager.playbackState.value.isPlaying)

        manager.playPause()
        assertTrue(manager.playbackState.value.isPlaying)
    }

    @Test
    fun toggleShuffle_togglesState() {
        assertFalse(manager.playbackState.value.isShuffle)
        manager.toggleShuffle()
        assertTrue(manager.playbackState.value.isShuffle)
        manager.toggleShuffle()
        assertFalse(manager.playbackState.value.isShuffle)
    }

    @Test
    fun toggleRepeat_cyclesCorrectly() {
        assertEquals(RepeatMode.OFF, manager.playbackState.value.repeatMode)
        manager.toggleRepeat()
        assertEquals(RepeatMode.ALL, manager.playbackState.value.repeatMode)
        manager.toggleRepeat()
        assertEquals(RepeatMode.ONE, manager.playbackState.value.repeatMode)
        manager.toggleRepeat()
        assertEquals(RepeatMode.OFF, manager.playbackState.value.repeatMode)
    }

    @Test
    fun seekTo_updatesPosition() {
        manager.playSong(testSong1)
        manager.seekTo(45000L)
        assertEquals(45000L, manager.playbackState.value.currentPositionMs)

        // Seeking to negative value clamps to 0L
        manager.seekTo(-5000L)
        assertEquals(0L, manager.playbackState.value.currentPositionMs)
    }

    @Test
    fun next_advancesToNextSongInPlaylist() {
        val queue = listOf(testSong1, testSong2, testSong3)
        manager.playSong(testSong1, queue)

        manager.next()
        assertEquals(testSong2, manager.playbackState.value.currentSong)

        manager.next()
        assertEquals(testSong3, manager.playbackState.value.currentSong)

        // Wraps around to first song
        manager.next()
        assertEquals(testSong1, manager.playbackState.value.currentSong)
    }

    @Test
    fun previous_whenElapsedMoreThan3Seconds_rewindsToStart() {
        manager.playSong(testSong1, listOf(testSong1, testSong2))
        manager.seekTo(5000L) // > 3000ms

        manager.previous()
        assertEquals(0L, manager.playbackState.value.currentPositionMs)
        assertEquals(testSong1, manager.playbackState.value.currentSong)
    }

    @Test
    fun previous_whenElapsedLessThan3Seconds_goesToPreviousSong() {
        val queue = listOf(testSong1, testSong2, testSong3)
        manager.playSong(testSong2, queue)
        manager.seekTo(1000L) // <= 3000ms

        manager.previous()
        assertEquals(testSong1, manager.playbackState.value.currentSong)

        // From first song, wraps around to last song
        manager.seekTo(500L)
        manager.previous()
        assertEquals(testSong3, manager.playbackState.value.currentSong)
    }

    @Test
    fun release_clearsResources() {
        manager.playSong(testSong1)
        manager.release()
        // No exception thrown and can safely call release again
        assertNotNull(manager.playbackState)
    }

    @Test
    fun sleepTimer_countsDownAndPausesPlayback() {
        manager.playSong(testSong1)
        assertTrue(manager.playbackState.value.isPlaying)

        manager.startSleepTimer(1)
        testScope.testScheduler.runCurrent()
        assertEquals(60_000L, manager.sleepTimerRemainingMs.value)

        testScope.testScheduler.advanceTimeBy(30_000L)
        testScope.testScheduler.runCurrent()
        assertEquals(30_000L, manager.sleepTimerRemainingMs.value)
        assertTrue(manager.playbackState.value.isPlaying)

        testScope.testScheduler.advanceTimeBy(31_000L)
        testScope.testScheduler.runCurrent()
        assertNull(manager.sleepTimerRemainingMs.value)
        assertFalse(manager.playbackState.value.isPlaying)
    }

    @Test
    fun sleepTimer_cancelKeepsPlaying() {
        manager.playSong(testSong1)
        manager.startSleepTimer(5)
        testScope.testScheduler.runCurrent()
        assertNotNull(manager.sleepTimerRemainingMs.value)

        manager.cancelSleepTimer()
        testScope.testScheduler.advanceTimeBy(400_000L)
        testScope.testScheduler.runCurrent()

        assertNull(manager.sleepTimerRemainingMs.value)
        assertTrue(manager.playbackState.value.isPlaying)
    }

    @Test
    fun playSong_notifiesListenerAndClearsError() {
        val started = mutableListOf<Long>()
        manager.onSongStarted = { started.add(it.id) }

        manager.playSong(testSong2, listOf(testSong1, testSong2))

        assertEquals(listOf(2L), started)
        assertNull(manager.playbackState.value.errorMessage)
    }

    @Test
    fun playSong_whenQueueMissingSong_prependsIt() {
        manager.playSong(testSong3, listOf(testSong1, testSong2))

        assertEquals(testSong3, manager.currentPlaylist.value.first())
        assertEquals(3, manager.currentPlaylist.value.size)
    }
}
