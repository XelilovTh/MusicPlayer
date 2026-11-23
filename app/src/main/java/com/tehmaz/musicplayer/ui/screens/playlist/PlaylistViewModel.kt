package com.tehmaz.musicplayer.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.repository.AudioRepository
import com.tehmaz.musicplayer.data.store.LibraryEvents
import com.tehmaz.musicplayer.data.store.UserPlaylistStore
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val repository: AudioRepository,
    private val playbackManager: AudioPlaybackManager,
    private val userPlaylists: UserPlaylistStore
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState
    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist: StateFlow<Playlist?> = _playlist.asStateFlow()
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()
    private var currentPlaylistId: Long = 1L

    init {
        viewModelScope.launch {
            LibraryEvents.changes.drop(1).collectLatest { loadPlaylist(currentPlaylistId) }
        }
    }

    fun loadPlaylist(playlistId: Long) {
        currentPlaylistId = playlistId
        viewModelScope.launch {
            val allPlaylists = repository.getPlaylists()
            _playlists.value = allPlaylists.filter { userPlaylists.isUserPlaylist(it.id) }
            val pl = allPlaylists.firstOrNull { it.id == playlistId }
            _playlist.value = pl
            _songs.value = pl?.songs.orEmpty()
        }
    }

    fun playAll() {
        val list = _songs.value
        if (list.isNotEmpty()) playbackManager.playSong(list.first(), list)
    }

    fun shuffleAll() {
        val list = _songs.value
        if (list.isNotEmpty()) {
            val shuffled = list.shuffled()
            playbackManager.playSong(shuffled.first(), shuffled)
        }
    }

    fun isUserPlaylist(): Boolean = userPlaylists.isUserPlaylist(currentPlaylistId)

    fun removeSong(songId: Long) {
        if (!isUserPlaylist()) return
        userPlaylists.removeSong(currentPlaylistId, songId)
        loadPlaylist(currentPlaylistId)
    }

    fun playSong(song: Song) = playbackManager.playSong(song, _songs.value)
    fun playPause() = playbackManager.playPause()
    fun next() = playbackManager.next()
}
