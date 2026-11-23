package com.tehmaz.musicplayer.ui.screens.home

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

class HomeViewModel(
    private val repository: AudioRepository,
    private val playbackManager: AudioPlaybackManager
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState
    private val _recentSongs = MutableStateFlow<List<Song>>(emptyList())
    val recentSongs: StateFlow<List<Song>> = _recentSongs.asStateFlow()
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            LibraryEvents.changes.drop(1).collectLatest { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val songs = repository.getSongs()
            _allSongs.value = songs
            _recentSongs.value = repository.getRecentSongs()
            _playlists.value = repository.getPlaylists()
        }
    }

    fun hasPermission(): Boolean = repository.hasPermission()

    fun rescan() {
        // repository.refresh() emits LibraryEvents; the observer performs one reload.
        viewModelScope.launch { repository.refresh() }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        playbackManager.playSong(song, if (queue.isNotEmpty()) queue else _allSongs.value)
    }

    fun playHeroCard() {
        _allSongs.value.firstOrNull()?.let { playbackManager.playSong(it, _allSongs.value) }
    }

    fun playPause() = playbackManager.playPause()
    fun next() = playbackManager.next()
}
