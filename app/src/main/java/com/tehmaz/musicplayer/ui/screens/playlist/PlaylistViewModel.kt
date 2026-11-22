package com.tehmaz.musicplayer.ui.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.repository.AudioRepository
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val repository: AudioRepository,
    private val playbackManager: AudioPlaybackManager
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState

    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist: StateFlow<Playlist?> = _playlist.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val pl = repository.getPlaylistById(playlistId) ?: repository.getPlaylists().firstOrNull()
            _playlist.value = pl
            _songs.value = pl?.songs ?: repository.getSongs()
        }
    }

    fun playAll() {
        val list = _songs.value
        if (list.isNotEmpty()) {
            playbackManager.playSong(list.first(), list)
        }
    }

    fun shuffleAll() {
        val list = _songs.value
        if (list.isNotEmpty()) {
            val shuffled = list.shuffled()
            playbackManager.playSong(shuffled.first(), shuffled)
        }
    }

    fun playSong(song: Song) {
        playbackManager.playSong(song, _songs.value)
    }

    fun playPause() {
        playbackManager.playPause()
    }

    fun next() {
        playbackManager.next()
    }
}
