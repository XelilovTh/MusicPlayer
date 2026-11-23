package com.tehmaz.musicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.RepeatMode
import com.tehmaz.musicplayer.data.model.Song
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class PlaybackControlsState(
    val currentSong: Song?,
    val isPlaying: Boolean,
    val isShuffle: Boolean,
    val repeatMode: RepeatMode
)

/**
 * Collect controls that change only when the track or playback mode changes.
 * Position ticks are intentionally excluded to keep the Now Playing screen stable.
 */
@Composable
fun rememberPlaybackControls(
    playbackState: StateFlow<PlaybackState>
): PlaybackControlsState {
    val controlsFlow = remember(playbackState) {
        playbackState.map { state ->
            PlaybackControlsState(
                currentSong = state.currentSong,
                isPlaying = state.isPlaying,
                isShuffle = state.isShuffle,
                repeatMode = state.repeatMode
            )
        }.distinctUntilChanged()
    }
    val current = playbackState.value
    return controlsFlow.collectAsState(
        initial = PlaybackControlsState(
            currentSong = current.currentSong,
            isPlaying = current.isPlaying,
            isShuffle = current.isShuffle,
            repeatMode = current.repeatMode
        )
    ).value
}
