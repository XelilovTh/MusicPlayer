package com.tehmaz.musicplayer.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.repository.AudioRepository
import com.tehmaz.musicplayer.data.store.LibraryEvents
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: AudioRepository,
    private val playbackManager: AudioPlaybackManager
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    private var searchJob: Job? = null
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
            _searchResults.value = if (_query.value.isBlank()) songs else repository.searchSongs(_query.value)
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchResults.value = if (newQuery.isBlank()) repository.getSongs()
                else repository.searchSongs(newQuery)
        }
    }

    fun playSong(song: Song) = playbackManager.playSong(song, _searchResults.value)
    fun playPause() = playbackManager.playPause()
    fun next() = playbackManager.next()
}
