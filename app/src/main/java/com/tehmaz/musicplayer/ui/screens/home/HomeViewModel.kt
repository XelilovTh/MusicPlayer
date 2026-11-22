package com.tehmaz.musicplayer.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.repository.AudioRepository
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val songs = repository.getSongs()
            _allSongs.value = songs
            _recentSongs.value = repository.getRecentSongs().ifEmpty { songs.take(6) }
            _playlists.value = repository.getPlaylists()
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val targetQueue = if (queue.isNotEmpty()) queue else _allSongs.value
        playbackManager.playSong(song, targetQueue)
    }

    fun playHeroCard() {
        val songs = _allSongs.value
        if (songs.isNotEmpty()) {
            playbackManager.playSong(songs.first(), songs)
        }
    }

    fun playPause() {
        playbackManager.playPause()
    }

    fun next() {
        playbackManager.next()
    }
}
