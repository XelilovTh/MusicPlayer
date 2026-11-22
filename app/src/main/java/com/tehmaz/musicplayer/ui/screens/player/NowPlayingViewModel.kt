package com.tehmaz.musicplayer.ui.screens.player

import androidx.lifecycle.ViewModel
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NowPlayingViewModel(
    private val playbackManager: AudioPlaybackManager
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun playPause() {
        playbackManager.playPause()
    }

    fun next() {
        playbackManager.next()
    }

    fun previous() {
        playbackManager.previous()
    }

    fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playbackManager.toggleRepeat()
    }

    fun toggleFavorite() {
        _isFavorite.update { !it }
    }
}
