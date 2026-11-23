package com.tehmaz.musicplayer.data.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Process-wide "library changed" signal. ViewModels observe [changes] and reload their data
 * whenever the media library or the user's playlists change (permission granted, new files,
 * playlist created, ...).
 */
object LibraryEvents {
    private val _changes = MutableStateFlow(0)
    val changes: StateFlow<Int> = _changes.asStateFlow()

    fun notifyChanged() {
        _changes.update { it + 1 }
    }
}
