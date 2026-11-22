package com.tehmaz.musicplayer.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.repository.AudioRepository
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val recentTags = listOf("Lofi", "Chill", "Imagine Dragons", "The Weeknd", "Sunset", "Ambient")

    init {
        // Initially show popular songs
        viewModelScope.launch {
            _searchResults.value = repository.getSongs().take(8)
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        viewModelScope.launch {
            if (newQuery.isBlank()) {
                _searchResults.value = repository.getSongs().take(8)
            } else {
                _searchResults.value = repository.searchSongs(newQuery)
            }
        }
    }

    fun selectTag(tag: String) {
        onQueryChange(tag)
    }

    fun playSong(song: Song) {
        playbackManager.playSong(song, _searchResults.value)
    }

    fun playPause() {
        playbackManager.playPause()
    }

    fun next() {
        playbackManager.next()
    }
}
