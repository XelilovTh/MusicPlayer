package com.tehmaz.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.tehmaz.musicplayer.data.model.Song
import com.tehmaz.musicplayer.ui.theme.AmberGoldDark
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.GlassBorderGradient
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary

/**
 * Docked frosted glass MiniPlayer positioned directly above the bottom navigation bar.
 */
@Composable
fun MiniPlayerBar(
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentSong == null) return

    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = 12.dp,
                shape = shape,
                spotColor = AmberGoldPrimary.copy(alpha = 0.25f),
                ambientColor = Color.Black.copy(alpha = 0.8f)
            )
            .clip(shape)
            .background(Color(0xE61E140E))
            .border(1.dp, GlassBorderGradient, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = AmberGoldPrimary.copy(alpha = 0.2f)),
                onClick = onBarClick
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Track playback progress line
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = AmberGoldPrimary,
                trackColor = Color(0x33F5B853),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art Thumbnail
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E1C12))
                        .border(1.dp, Color(0x40F5B853), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentSong.albumArtUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = currentSong.albumArtUri,
                            contentDescription = currentSong.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AmberGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Track Title & Artist
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentSong.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentSong.artist,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play / Pause glowing circle button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AmberGoldPrimary, AmberGoldDark)
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true, color = Color.White),
                            onClick = onPlayPauseClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color(0xFF140E0A),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Next Button
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next Track",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
