package com.tehmaz.musicplayer

import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.repository.AudioRepository
import com.tehmaz.musicplayer.data.store.FavoritesStore
import com.tehmaz.musicplayer.data.store.UserPlaylistStore
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import com.tehmaz.musicplayer.ui.screens.home.HomeViewModel
import com.tehmaz.musicplayer.ui.screens.library.LibraryTab
import com.tehmaz.musicplayer.ui.screens.library.LibraryViewModel
import com.tehmaz.musicplayer.ui.screens.player.NowPlayingViewModel
import com.tehmaz.musicplayer.ui.screens.playlist.PlaylistViewModel
import com.tehmaz.musicplayer.ui.screens.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val playbackManager = mockk<AudioPlaybackManager>(relaxed = true)
    private val repository = mockk<AudioRepository>(relaxed = true)
    private val playbackStateFlow = MutableStateFlow(PlaybackState())

    private val testSong1 = Song(1L, "Song 1", "Artist 1", "Album 1", 180000L, "uri1")
    private val testSong2 = Song(2L, "Song 2", "Artist 2", "Album 2", 210000L, "uri2")
    private val testSongs = listOf(testSong1, testSong2)
    private val testPlaylist = Playlist(1L, "Test Playlist", 2, "cover1", testSongs)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { playbackManager.playbackState } returns playbackStateFlow
        coEvery { repository.getSongs() } returns testSongs
        coEvery { repository.getRecentSongs() } returns testSongs
        coEvery { repository.getPlaylists() } returns listOf(testPlaylist)
        coEvery { repository.getPlaylistById(1L) } returns testPlaylist
        coEvery { repository.searchSongs(any()) } returns listOf(testSong1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // NowPlayingViewModel Tests
    // =========================================================================

    @Test
    fun testNowPlayingViewModelPlaybackActions() {
        val viewModel = NowPlayingViewModel(playbackManager, FavoritesStore())

        viewModel.playPause()
        verify(exactly = 1) { playbackManager.playPause() }

        viewModel.next()
        verify(exactly = 1) { playbackManager.next() }

        viewModel.previous()
        verify(exactly = 1) { playbackManager.previous() }

        viewModel.seekTo(12000L)
        verify(exactly = 1) { playbackManager.seekTo(12000L) }

        viewModel.toggleShuffle()
        verify(exactly = 1) { playbackManager.toggleShuffle() }

        viewModel.toggleRepeat()
        verify(exactly = 1) { playbackManager.toggleRepeat() }
    }

    @Test
    fun testNowPlayingViewModelFavoriteToggle() {
        playbackStateFlow.value = PlaybackState(currentSong = testSong1)
        val viewModel = NowPlayingViewModel(playbackManager, FavoritesStore())
        assertFalse(testSong1.id in viewModel.favoriteIds.value)

        viewModel.toggleFavorite()
        assertTrue(testSong1.id in viewModel.favoriteIds.value)

        viewModel.toggleFavorite()
        assertFalse(testSong1.id in viewModel.favoriteIds.value)
    }

    // =========================================================================
    // HomeViewModel Tests
    // =========================================================================

    @Test
    fun testHomeViewModelLoadsData() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(repository, playbackManager)
        advanceUntilIdle()

        assertEquals(2, viewModel.recentSongs.value.size)
        assertEquals(1, viewModel.playlists.value.size)
        assertEquals("Test Playlist", viewModel.playlists.value[0].name)
    }

    @Test
    fun testHomeViewModelPlaySongAndHeroCard() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(repository, playbackManager)
        advanceUntilIdle()

        viewModel.playSong(testSong1)
        verify { playbackManager.playSong(testSong1, any()) }

        viewModel.playHeroCard()
        verify { playbackManager.playSong(testSong1, testSongs) }

        viewModel.playPause()
        verify { playbackManager.playPause() }

        viewModel.next()
        verify { playbackManager.next() }
    }

    // =========================================================================
    // LibraryViewModel Tests
    // =========================================================================

    @Test
    fun testLibraryViewModelTabSelection() = runTest(testDispatcher) {
        val viewModel = LibraryViewModel(repository, playbackManager)
        advanceUntilIdle()

        assertEquals(LibraryTab.ALL, viewModel.selectedTab.value)
        assertEquals(2, viewModel.songs.value.size)

        viewModel.selectTab(LibraryTab.ALBUMS)
        assertEquals(LibraryTab.ALBUMS, viewModel.selectedTab.value)

        viewModel.selectTab(LibraryTab.ARTISTS)
        assertEquals(LibraryTab.ARTISTS, viewModel.selectedTab.value)

        // Playlists are exposed as data, not as a LibraryTab.
        assertEquals(1, viewModel.playlists.value.size)
    }

    @Test
    fun testLibraryViewModelPlayback() = runTest(testDispatcher) {
        val viewModel = LibraryViewModel(repository, playbackManager)
        advanceUntilIdle()

        viewModel.playSong(testSong2)
        verify { playbackManager.playSong(testSong2, testSongs) }

        viewModel.shuffleAll()
        verify { playbackManager.playSong(any(), any()) }

        viewModel.playPause()
        verify { playbackManager.playPause() }

        viewModel.next()
        verify { playbackManager.next() }
    }

    // =========================================================================
    // SearchViewModel Tests
    // =========================================================================

    @Test
    fun testSearchViewModelInitialAndQueryChange() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(repository, playbackManager)
        advanceUntilIdle()

        // A blank query shows all songs found on the device.
        assertEquals(2, viewModel.searchResults.value.size)

        // Query change with non-blank
        viewModel.onQueryChange("Song 1")
        advanceUntilIdle()
        coVerify { repository.searchSongs("Song 1") }
        assertEquals(1, viewModel.searchResults.value.size)

        // Query change with blank resets to initial
        viewModel.onQueryChange("")
        advanceUntilIdle()
        assertEquals(2, viewModel.searchResults.value.size)
    }

    @Test
    fun testSearchViewModelQueryAndPlayback() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(repository, playbackManager)
        advanceUntilIdle()

        viewModel.onQueryChange("Song 1")
        advanceUntilIdle()
        assertEquals("Song 1", viewModel.query.value)
        assertEquals(listOf(testSong1), viewModel.searchResults.value)
        coVerify { repository.searchSongs("Song 1") }

        viewModel.playSong(testSong1)
        verify { playbackManager.playSong(testSong1, listOf(testSong1)) }

        viewModel.playPause()
        verify { playbackManager.playPause() }

        viewModel.next()
        verify { playbackManager.next() }
    }

    // =========================================================================
    // PlaylistViewModel Tests
    // =========================================================================

    @Test
    fun testPlaylistViewModelLoadAndPlayback() = runTest(testDispatcher) {
        val viewModel = PlaylistViewModel(repository, playbackManager, UserPlaylistStore())

        viewModel.loadPlaylist(1L)
        advanceUntilIdle()

        assertNotNull(viewModel.playlist.value)
        assertEquals("Test Playlist", viewModel.playlist.value?.name)
        assertEquals(2, viewModel.songs.value.size)

        viewModel.playAll()
        verify { playbackManager.playSong(testSong1, testSongs) }

        viewModel.shuffleAll()
        verify { playbackManager.playSong(any(), any()) }

        viewModel.playSong(testSong2)
        verify { playbackManager.playSong(testSong2, testSongs) }

        viewModel.playPause()
        verify { playbackManager.playPause() }

        viewModel.next()
        verify { playbackManager.next() }
    }
}
