package com.tehmaz.musicplayer.ui.screens.player

import androidx.lifecycle.ViewModel
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.store.FavoritesStore
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
import kotlinx.coroutines.flow.StateFlow

class NowPlayingViewModel(
    private val playbackManager: AudioPlaybackManager,
    private val favorites: FavoritesStore
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState
    val favoriteIds: StateFlow<Set<Long>> = favorites.favoriteIds

    fun playPause() = playbackManager.playPause()
    fun next() = playbackManager.next()
    fun previous() = playbackManager.previous()
    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)
    fun toggleShuffle() = playbackManager.toggleShuffle()
    fun toggleRepeat() = playbackManager.toggleRepeat()

    fun toggleFavorite() {
        playbackState.value.currentSong?.let { favorites.toggle(it.id) }
    }
}
