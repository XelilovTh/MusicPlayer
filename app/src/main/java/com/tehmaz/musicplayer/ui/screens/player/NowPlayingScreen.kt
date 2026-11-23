package com.tehmaz.musicplayer.ui.screens.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tehmaz.musicplayer.data.model.PlaybackState
import com.tehmaz.musicplayer.data.model.RepeatMode
import com.tehmaz.musicplayer.ui.components.GlassIconButton
import com.tehmaz.musicplayer.ui.components.rememberPlaybackControls
import com.tehmaz.musicplayer.ui.components.AddToPlaylistDialogHost
import com.tehmaz.musicplayer.ui.components.GlowBackground
import com.tehmaz.musicplayer.ui.theme.AmberGoldDark
import com.tehmaz.musicplayer.ui.theme.AmberGoldLight
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.GlassBorderGradient
import com.tehmaz.musicplayer.ui.theme.GoldenAmberGradient
import com.tehmaz.musicplayer.ui.theme.TextMuted
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onCollapse: () -> Unit,
    onNavigateToEQ: () -> Unit,
    onNavigateToSleepTimer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: NowPlayingViewModel = koinViewModel()
) {
    val playbackControls = rememberPlaybackControls(viewModel.playbackState)
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    val song = playbackControls.currentSong
    val isFavorite = song?.let { it.id in favoriteIds } == true
    var showAddToPlaylist by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isUserDragging by remember { androidx.compose.runtime.mutableStateOf(false) }

    GlowBackground(
        showTopGlow = true,
        showCenterGlow = true,
        showBottomGlow = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 44.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Header: Collapse Chevron, Title, 3-dot Menu
            NowPlayingHeader(
                onCollapse = onCollapse,
                title = song?.album ?: "Now Playing"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Large Album Art with warm amber radial halo behind it
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                // Amber Halo Glow Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AmberGoldPrimary.copy(alpha = 0.35f),
                                AmberGoldDark.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width * 0.65f
                        ),
                        radius = size.width * 0.65f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }

                // Rounded Square Album Art
                val artShape = RoundedCornerShape(26.dp)
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.92f)
                        .aspectRatio(1f)
                        .shadow(
                            elevation = 20.dp,
                            shape = artShape,
                            spotColor = AmberGoldPrimary.copy(alpha = 0.4f),
                            ambientColor = Color.Black
                        )
                        .clip(artShape)
                        .background(Color(0xFF261910))
                        .border(1.5.dp, GlassBorderGradient, artShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (song != null && !song.albumArtUri.isNullOrEmpty()) {
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
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Track Title, Artist, Favorite toggle & Add to playlist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song?.title ?: "No Track Selected",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song?.artist ?: "Unknown Artist",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Add to playlist icon
                IconButton(
                    onClick = { if (song != null) showAddToPlaylist = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlaylistAdd,
                        contentDescription = "Add to playlist",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Favorite / Heart Toggle Button
                val heartTint by animateColorAsState(
                    targetValue = if (isFavorite) AmberGoldPrimary else TextSecondary,
                    label = "heart_color"
                )

                IconButton(
                    enabled = song != null,
                    onClick = { viewModel.toggleFavorite() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = heartTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (showAddToPlaylist && song != null) {
                AddToPlaylistDialogHost(song = song, onDismiss = { showAddToPlaylist = false })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Position updates are isolated so the artwork and transport controls do not recompose.
            PlaybackProgressControls(
                playbackState = viewModel.playbackState,
                onSeek = viewModel::seekTo
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Playback Controls Row: Shuffle, Previous, Large Glowing Play/Pause, Next, Repeat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackControls.isShuffle) AmberGoldPrimary else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous Track
                IconButton(
                    onClick = { viewModel.previous() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Large Glowing Golden Play / Pause Button (68.dp)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            spotColor = AmberGoldPrimary.copy(alpha = 0.6f),
                            ambientColor = AmberGoldPrimary.copy(alpha = 0.4f)
                        )
                        .clip(CircleShape)
                        .background(GoldenAmberGradient)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(color = Color.White),
                            onClick = { viewModel.playPause() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playbackControls.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playbackControls.isPlaying) "Pause" else "Play",
                        tint = Color(0xFF140E0A),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next Track
                IconButton(
                    onClick = { viewModel.next() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next Track",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat Mode Toggle
                IconButton(
                    onClick = { viewModel.toggleRepeat() },
                    modifier = Modifier.size(44.dp)
                ) {
                    val repeatIcon = if (playbackControls.repeatMode == RepeatMode.ONE) {
                        Icons.Rounded.RepeatOne
                    } else {
                        Icons.Rounded.Repeat
                    }
                    val repeatTint = if (playbackControls.repeatMode != RepeatMode.OFF) {
                        AmberGoldPrimary
                    } else {
                        TextMuted
                    }
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = repeatTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 6. Bottom Frosted Glass Action Dock: Lyrics, EQ, Sleep Timer, Settings
            BottomGlassActionDock(
                onEQClick = onNavigateToEQ,
                onSleepTimerClick = onNavigateToSleepTimer,
                onSettingsClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun PlaybackProgressControls(
    playbackState: StateFlow<PlaybackState>,
    onSeek: (Long) -> Unit
) {
    val positionFlow = remember(playbackState) {
        playbackState
            .map { it.currentPositionMs to it.totalDurationMs }
            .distinctUntilChanged()
    }
    val initial = playbackState.value
    val (positionMs, durationMs) = positionFlow.collectAsState(
        initial = initial.currentPositionMs to initial.totalDurationMs
    ).value

    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isUserDragging by remember { mutableStateOf(false) }
    val progress = if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val currentProgress = if (isUserDragging) sliderPosition else progress

    Spacer(modifier = Modifier.height(20.dp))
    Slider(
        value = currentProgress,
        onValueChange = { value ->
            isUserDragging = true
            sliderPosition = value
        },
        onValueChangeFinished = {
            isUserDragging = false
            onSeek((sliderPosition * durationMs).toLong())
        },
        colors = SliderDefaults.colors(
            thumbColor = AmberGoldLight,
            activeTrackColor = AmberGoldPrimary,
            inactiveTrackColor = Color(0x33F5B853)
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isUserDragging) {
                PlaybackState.formatTime((sliderPosition * durationMs).toLong())
            } else PlaybackState.formatTime(positionMs),
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = PlaybackState.formatTime(durationMs),
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Top collapse header.
 */
@Composable
private fun NowPlayingHeader(
    onCollapse: () -> Unit,
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            onClick = onCollapse,
            size = 40.dp
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PLAYING FROM",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.size(40.dp))
    }
}

/**
 * Frosted Glass Action Dock matching reference styling:
 * Translucent dark capsule containing 4 buttons (Lyrics, EQ, Sleep Timer, Settings).
 */
@Composable
private fun BottomGlassActionDock(
    onEQClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val dockShape = RoundedCornerShape(26.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = dockShape,
                spotColor = AmberGoldPrimary.copy(alpha = 0.2f),
                ambientColor = Color.Black.copy(alpha = 0.7f)
            )
            .clip(dockShape)
            .background(Color(0xE61E140E))
            .border(1.dp, GlassBorderGradient, dockShape)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockActionButton(
                icon = Icons.Rounded.GraphicEq,
                label = "EQ",
                onClick = onEQClick
            )
            DockActionButton(
                icon = Icons.Rounded.Bedtime,
                label = "Sleep Timer",
                onClick = onSleepTimerClick
            )
            DockActionButton(
                icon = Icons.Rounded.Settings,
                label = "Settings",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun DockActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = AmberGoldPrimary.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AmberGoldPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
