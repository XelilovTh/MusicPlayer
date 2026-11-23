package com.tehmaz.musicplayer.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.data.store.FavoritesStore
import com.tehmaz.musicplayer.data.store.UserPlaylist
import com.tehmaz.musicplayer.data.store.UserPlaylistStore
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.TextMuted
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.koinViewModel

/**
 * Shared actions available on any song: favorites and user playlists.
 */
class SongActionsViewModel(
    private val favorites: FavoritesStore,
    private val userPlaylists: UserPlaylistStore
) : ViewModel() {

    val favoriteIds: StateFlow<Set<Long>> = favorites.favoriteIds
    val playlists: StateFlow<List<UserPlaylist>> = userPlaylists.playlists

    fun toggleFavorite(song: Song) {
        favorites.toggle(song.id)
    }

    /** Returns false if the song is already in the playlist. */
    fun addToPlaylist(playlistId: Long, song: Song): Boolean {
        return userPlaylists.addSong(playlistId, song.id)
    }

    fun createPlaylist(name: String, song: Song): Long {
        return userPlaylists.create(name, song.id)
    }
}

/**
 * Three-dot button with a real options menu: favorite toggle, add to playlist and,
 * when [onRemoveFromList] is given, removal from the current playlist.
 */
@Composable
fun SongOptionsButton(
    song: Song,
    modifier: Modifier = Modifier,
    onRemoveFromList: (() -> Unit)? = null,
    viewModel: SongActionsViewModel = koinViewModel()
) {
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    val isFavorite = song.id in favoriteIds

    Box(modifier = modifier) {
        IconButton(
            onClick = { menuOpen = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            modifier = Modifier.background(Color(0xFF1E140E))
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        color = TextPrimary
                    )
                },
                onClick = {
                    menuOpen = false
                    viewModel.toggleFavorite(song)
                }
            )
            DropdownMenuItem(
                text = { Text(text = "Add to playlist", color = TextPrimary) },
                onClick = {
                    menuOpen = false
                    showPlaylistDialog = true
                }
            )
            if (onRemoveFromList != null) {
                DropdownMenuItem(
                    text = { Text(text = "Remove from this playlist", color = TextPrimary) },
                    onClick = {
                        menuOpen = false
                        onRemoveFromList()
                    }
                )
            }
        }
    }

    if (showPlaylistDialog) {
        AddToPlaylistDialogHost(
            song = song,
            onDismiss = { showPlaylistDialog = false },
            viewModel = viewModel
        )
    }
}

/**
 * Dialog that lets the user pick one of their playlists or create a new one for [song].
 */
@Composable
fun AddToPlaylistDialogHost(
    song: Song,
    onDismiss: () -> Unit,
    viewModel: SongActionsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val playlists by viewModel.playlists.collectAsState()
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E140E),
        title = { Text(text = "Add to playlist", color = TextPrimary) },
        text = {
            Column {
                Column(
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (playlists.isEmpty()) {
                        Text(
                            text = "You have no playlists yet. Create one below.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    playlists.forEach { playlist ->
                        Text(
                            text = "${playlist.name}  \u2022  ${playlist.songIds.size}",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val added = viewModel.addToPlaylist(playlist.id, song)
                                    Toast.makeText(
                                        context,
                                        if (added) "Added to ${playlist.name}" else "Already in ${playlist.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    label = { Text(text = "New playlist name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AmberGoldPrimary,
                        cursorColor = AmberGoldPrimary,
                        focusedLabelColor = AmberGoldPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = newName.isNotBlank(),
                onClick = {
                    val name = newName.trim()
                    viewModel.createPlaylist(name, song)
                    Toast.makeText(context, "Added to $name", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text(text = "Create & add", color = AmberGoldPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextSecondary)
            }
        }
    )
}
