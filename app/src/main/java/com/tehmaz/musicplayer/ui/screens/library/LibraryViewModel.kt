package com.tehmaz.musicplayer.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.repository.AudioRepository
import com.tehmaz.musicplayer.data.store.LibraryEvents
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

enum class LibraryTab(val title: String) {
    ALL("All"),
    ARTISTS("Artists"),
    ALBUMS("Albums")
}

class LibraryViewModel(
    private val repository: AudioRepository,
    private val playbackManager: AudioPlaybackManager
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState
    private val _selectedTab = MutableStateFlow(LibraryTab.ALL)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            LibraryEvents.changes.drop(1).collectLatest { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _songs.value = repository.getSongs()
            _playlists.value = repository.getPlaylists()
        }
    }

    fun hasPermission(): Boolean = repository.hasPermission()

    fun rescan() {
        viewModelScope.launch {
            repository.refresh()
            refresh()
        }
    }
    fun selectTab(tab: LibraryTab) { _selectedTab.value = tab }
    fun playSong(song: Song) = playbackManager.playSong(song, _songs.value)

    fun shuffleAll() {
        val list = _songs.value
        if (list.isNotEmpty()) playbackManager.playSong(list.random(), list.shuffled())
    }

    fun playPause() = playbackManager.playPause()
    fun next() = playbackManager.next()
}
