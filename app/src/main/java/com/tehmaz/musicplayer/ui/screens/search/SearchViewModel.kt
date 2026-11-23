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
import kotlinx.coroutines.delay
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
            LibraryEvents.changes.drop(1).collectLatest { loadData() }
        }
    }

    fun refresh() {
        viewModelScope.launch { loadData() }
    }

    private suspend fun loadData() {
        val songs = repository.getSongs()
        _allSongs.value = songs
        val query = _query.value
        _searchResults.value = if (query.isBlank()) songs else repository.searchSongs(query)
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Wait briefly for typing to settle; cancelled jobs never scan/filter for stale text.
            delay(180L)
            _searchResults.value = if (newQuery.isBlank()) _allSongs.value
                else repository.searchSongs(newQuery)
        }
    }

    fun playSong(song: Song) = playbackManager.playSong(song, _searchResults.value)
    fun playPause() = playbackManager.playPause()
    fun next() = playbackManager.next()
}
