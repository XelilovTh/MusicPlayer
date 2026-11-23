package com.tehmaz.musicplayer.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.ui.components.CategoryPill
import com.tehmaz.musicplayer.ui.components.EmptyLibraryCard
import com.tehmaz.musicplayer.ui.components.SongOptionsButton
import com.tehmaz.musicplayer.ui.components.rememberPermissionRequester
import com.tehmaz.musicplayer.ui.components.GlassPillButton
import com.tehmaz.musicplayer.ui.components.GlassmorphicCard
import com.tehmaz.musicplayer.ui.components.GlowBackground
import com.tehmaz.musicplayer.ui.components.MiniPlayerBar
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.TextMuted
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@Composable
fun LibraryScreen(
    onNavigateToNowPlaying: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    var hasPermission by remember { mutableStateOf(viewModel.hasPermission()) }
    val requestPermission = rememberPermissionRequester {
        hasPermission = viewModel.hasPermission()
        viewModel.rescan()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = viewModel.hasPermission()
                viewModel.rescan()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val visibleSongs = when (selectedTab) {
        LibraryTab.ALL -> songs
        LibraryTab.ARTISTS -> songs.sortedBy { it.artist.lowercase() + it.title.lowercase() }
        LibraryTab.ALBUMS -> songs.sortedBy { it.album.lowercase() + it.title.lowercase() }
    }

    GlowBackground(
        showTopGlow = true,
        showCenterGlow = false,
        showBottomGlow = true
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp)
            ) {
                // Header with title and shuffle all
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Your Library",
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${songs.size} tracks available",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        GlassPillButton(
                            text = "Shuffle",
                            icon = Icons.Rounded.Shuffle,
                            onClick = { viewModel.shuffleAll() }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }

                if (songs.isEmpty()) {
                    item { EmptyLibraryCard(hasPermission, requestPermission, viewModel::rescan) }
                } else {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(LibraryTab.values()) { tab ->
                                CategoryPill(
                                    text = tab.title,
                                    isSelected = tab == selectedTab,
                                    onClick = { viewModel.selectTab(tab) }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }

                // Real local tracks, grouped by artist or album when those tabs are selected.
                if (selectedTab == LibraryTab.ALL) {
                    itemsIndexed(visibleSongs) { index, song ->
                        val isCurrent = playbackState.currentSong?.id == song.id
                        SongListItem(
                            index = index + 1,
                            song = song,
                            isPlaying = isCurrent && playbackState.isPlaying,
                            onClick = { viewModel.playSong(song) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    val groups = visibleSongs.groupBy {
                        if (selectedTab == LibraryTab.ARTISTS) it.artist else it.album
                    }.toSortedMap(String.CASE_INSENSITIVE_ORDER)
                    items(groups.entries.toList()) { group ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = group.key,
                                color = AmberGoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                            )
                            group.value.forEachIndexed { index, song ->
                                val isCurrent = playbackState.currentSong?.id == song.id
                                SongListItem(
                                    index = index + 1,
                                    song = song,
                                    isPlaying = isCurrent && playbackState.isPlaying,
                                    onClick = { viewModel.playSong(song) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Docked MiniPlayer
            if (playbackState.currentSong != null) {
                MiniPlayerBar(
                    currentSong = playbackState.currentSong,
                    isPlaying = playbackState.isPlaying,
                    progress = playbackState.progress,
                    onPlayPauseClick = { viewModel.playPause() },
                    onNextClick = { viewModel.next() },
                    onBarClick = onNavigateToNowPlaying
                )
            }
        }
    }
}

@Composable
fun SongListItem(
    index: Int,
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemoveFromList: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = shape,
        backgroundColor = if (isPlaying) Color(0xFF261A12) else Color(0xFF1E140E).copy(alpha = 0.6f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index number or Equalizer icon if playing
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Playing",
                        tint = AmberGoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "$index",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Album Artwork
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF291B12))
                    .border(1.dp, Color(0x33F5B853), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!song.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = AmberGoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Song Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = if (isPlaying) AmberGoldPrimary else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${song.artist} • ${song.album}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Duration
            Text(
                text = song.formattedDuration,
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            SongOptionsButton(song, modifier = Modifier.size(32.dp), onRemoveFromList = onRemoveFromList)
        }
    }
}
