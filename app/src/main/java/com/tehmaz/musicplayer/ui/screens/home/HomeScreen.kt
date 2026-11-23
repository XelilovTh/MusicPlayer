package com.tehmaz.musicplayer.ui.screens.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tehmaz.musicplayer.data.model.Playlist
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.ui.components.DiscoverBadge
import com.tehmaz.musicplayer.ui.components.EmptyLibraryCard
import com.tehmaz.musicplayer.ui.components.rememberPermissionRequester
import com.tehmaz.musicplayer.ui.components.GlassIconButton
import com.tehmaz.musicplayer.ui.components.GlassmorphicCard
import com.tehmaz.musicplayer.ui.components.GlowBackground
import com.tehmaz.musicplayer.ui.components.rememberPlaybackSummary
import com.tehmaz.musicplayer.ui.components.GoldenPillButton
import com.tehmaz.musicplayer.ui.components.VinylRecordView
import com.tehmaz.musicplayer.ui.theme.AmberGoldDark
import com.tehmaz.musicplayer.ui.theme.AmberGoldLight
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.GlassBorderGradient
import com.tehmaz.musicplayer.ui.theme.TextMuted
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToLibrary: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val playbackSummary = rememberPlaybackSummary(viewModel.playbackState)
    val recentSongs by viewModel.recentSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    var hasPermission by remember { mutableStateOf(viewModel.hasPermission()) }
    val requestPermission = rememberPermissionRequester {
        hasPermission = viewModel.hasPermission()
        viewModel.rescan()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val permissionNowGranted = viewModel.hasPermission()
                if (!hasPermission && permissionNowGranted) viewModel.rescan()
                else viewModel.refresh()
                hasPermission = permissionNowGranted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    GlowBackground(
        showTopGlow = true,
        showCenterGlow = false,
        showBottomGlow = true
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 48.dp, bottom = if (playbackSummary.first != null) 180.dp else 100.dp)
            ) {
                // 1. Top Header with User Greeting
                item {
                    HomeTopHeader(
                        title = "Your Music",
                        greeting = "Listen offline, anytime",
                        onSearchClick = onNavigateToSearch,
                        onSettingsClick = onNavigateToSettings
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                if (allSongs.isEmpty()) {
                    item { EmptyLibraryCard(hasPermission, requestPermission, viewModel::rescan) }
                } else {
                    item {
                        HeroVinylCard(
                            isPlaying = playbackSummary.second,
                            onPlayClick = { viewModel.playHeroCard() }
                        )
                    }
                }

                if (allSongs.isNotEmpty()) item { Spacer(modifier = Modifier.height(28.dp)) }

                if (recentSongs.isNotEmpty()) item { // Recently played section
                    SectionHeader(
                        title = "Recently Played",
                        actionText = "See All",
                        onActionClick = onNavigateToLibrary
                    )
                }

                if (allSongs.isNotEmpty()) {
                    if (recentSongs.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(14.dp)) }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(recentSongs, key = { it.id }) { song ->
                                    RecentlyPlayedCard(
                                        song = song,
                                        isCurrentPlaying = playbackSummary.first == song.id && playbackSummary.second,
                                        onClick = { viewModel.playSong(song, recentSongs) }
                                    )
                                }
                            }
                        }
                    }
                    if (playlists.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(30.dp)) }
                        item {
                            SectionHeader(
                                title = "Your Playlists",
                                actionText = "See All",
                                onActionClick = onNavigateToLibrary
                            )
                        }
                        item { Spacer(modifier = Modifier.height(14.dp)) }
                        items(playlists, key = { it.id }) { playlist ->
                            PlaylistItemCard(
                                playlist = playlist,
                                onClick = { onNavigateToPlaylist(playlist.id) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top greeting header with glowing music note badge and circular glass buttons.
 */
@Composable
private fun HomeTopHeader(
    title: String,
    greeting: String,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing frosted square with musical note icon
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = AmberGoldPrimary.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2E1C12), Color(0xFF1B110A))
                    )
                )
                .border(1.dp, Color(0x66F5B853), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = AmberGoldPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // App title and short offline-first description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Search circular glass button
        GlassIconButton(
            onClick = onSearchClick,
            size = 42.dp
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Settings circular glass button
        GlassIconButton(
            onClick = onSettingsClick,
            size = 42.dp
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Hero Vinyl Card matching reference image 1:
 * Discover Music badge, "Feel the Music", "Play Now" golden pill, and rotating vinyl record.
 */
@Composable
private fun HeroVinylCard(
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 14.dp,
                shape = cardShape,
                spotColor = AmberGoldPrimary.copy(alpha = 0.3f),
                ambientColor = Color.Black.copy(alpha = 0.8f)
            )
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2B180E),
                        Color(0xFF1E110A),
                        Color(0xFF140B06)
                    )
                )
            )
            .border(1.dp, GlassBorderGradient, cardShape)
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Content
            Column(
                modifier = Modifier.weight(1.3f)
            ) {
                DiscoverBadge(text = "✨ Discover Music")

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Feel the Music",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your favorite songs, always with you.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                GoldenPillButton(
                    text = "▶ Play Now",
                    onClick = onPlayClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Carousel Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(AmberGoldPrimary)
                    )
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0x40FFFFFF))
                        )
                    }
                }
            }

            // Right Vinyl Graphic
            Box(
                modifier = Modifier.weight(0.9f),
                contentAlignment = Alignment.CenterEnd
            ) {
                VinylRecordView(
                    size = 140.dp,
                    isPlaying = isPlaying
                )
            }
        }
    }
}

/**
 * Standard section title with gold "See All >" link button.
 */
@Composable
private fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(color = AmberGoldPrimary),
                    onClick = onActionClick
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = actionText,
                color = AmberGoldPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = AmberGoldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Album art card with rounded corners, play overlay, and song details.
 */
@Composable
private fun RecentlyPlayedCard(
    song: Song,
    isCurrentPlaying: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .width(135.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = AmberGoldPrimary.copy(alpha = 0.2f)),
                onClick = onClick
            )
    ) {
        // Artwork Box
        Box(
            modifier = Modifier
                .size(135.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = shape,
                    spotColor = if (isCurrentPlaying) AmberGoldPrimary.copy(alpha = 0.5f) else Color.Black
                )
                .clip(shape)
                .background(Color(0xFF261810))
                .border(
                    width = 1.dp,
                    color = if (isCurrentPlaying) AmberGoldPrimary else Color(0x33F5B853),
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!song.albumArtUri.isNullOrEmpty()) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AmberGoldPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Dark vignette overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x990C0907))
                        )
                    )
            )

            // Circular golden glowing play button at bottom-right or center
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AmberGoldLight, AmberGoldDark)
                        )
                    )
                    .shadow(6.dp, CircleShape, spotColor = AmberGoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCurrentPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFF140E0A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = song.artist,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Playlist vertical item card matching reference layout:
 * Frosted glass card, playlist icon / cover, title, track count, 3-dot overflow.
 */
@Composable
private fun PlaylistItemCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = shape,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playlist Art or Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF351F14), Color(0xFF1E110A))
                        )
                    )
                    .border(1.dp, Color(0x40F5B853), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!playlist.coverArtUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = playlist.coverArtUrl,
                        contentDescription = playlist.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = AmberGoldPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Playlist Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${playlist.songCount} songs",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // 3-dot Menu Icon
            IconButton(
                onClick = { /* Menu options */ },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
