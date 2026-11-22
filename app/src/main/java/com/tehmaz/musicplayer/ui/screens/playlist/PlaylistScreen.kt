package com.tehmaz.musicplayer.ui.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tehmaz.musicplayer.ui.components.GlassIconButton
import com.tehmaz.musicplayer.ui.components.GlassPillButton
import com.tehmaz.musicplayer.ui.components.GlowBackground
import com.tehmaz.musicplayer.ui.components.GoldenPillButton
import com.tehmaz.musicplayer.ui.components.MiniPlayerBar
import com.tehmaz.musicplayer.ui.screens.library.SongListItem
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.GlassBorderGradient
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlaylistScreen(
    playlistId: Long,
    onBackClick: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    viewModel: PlaylistViewModel = koinViewModel()
) {
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

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
                contentPadding = PaddingValues(top = 44.dp, bottom = 120.dp)
            ) {
                // Header Nav Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(
                            onClick = onBackClick,
                            size = 40.dp
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "Playlist",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        GlassIconButton(
                            onClick = { /* Menu */ },
                            size = 40.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                // Playlist Hero Artwork & Details
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val artShape = RoundedCornerShape(22.dp)
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .aspectRatio(1f)
                                .shadow(
                                    elevation = 14.dp,
                                    shape = artShape,
                                    spotColor = AmberGoldPrimary.copy(alpha = 0.35f)
                                )
                                .clip(artShape)
                                .background(Color(0xFF281B12))
                                .border(1.dp, GlassBorderGradient, artShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!playlist?.coverArtUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = playlist?.coverArtUrl,
                                    contentDescription = playlist?.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.QueueMusic,
                                    contentDescription = null,
                                    tint = AmberGoldPrimary,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = playlist?.name ?: "Chill Vibes",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${songs.size} songs • 2h 34m",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Pill Buttons: Play & Shuffle
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GoldenPillButton(
                                text = "Play",
                                icon = Icons.Rounded.PlayArrow,
                                onClick = { viewModel.playAll() }
                            )

                            GlassPillButton(
                                text = "Shuffle",
                                icon = Icons.Rounded.Shuffle,
                                onClick = { viewModel.shuffleAll() }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(28.dp)) }

                // Track list
                itemsIndexed(songs) { index, song ->
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
