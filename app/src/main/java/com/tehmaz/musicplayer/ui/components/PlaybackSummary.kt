package com.tehmaz.musicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.tehmaz.musicplayer.data.model.PlaybackState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Collect only fields used by song lists. Playback position updates should not redraw
 * and re-sort the entire library every few hundred milliseconds.
 */
@Composable
fun rememberPlaybackSummary(
    playbackState: StateFlow<PlaybackState>
): Pair<Long?, Boolean> {
    val summaryFlow = remember(playbackState) {
        playbackState
            .map { state -> state.currentSong?.id to state.isPlaying }
            .distinctUntilChanged()
    }
    val current = playbackState.value
    return summaryFlow.collectAsState(
        initial = current.currentSong?.id to current.isPlaying
    ).value
}
