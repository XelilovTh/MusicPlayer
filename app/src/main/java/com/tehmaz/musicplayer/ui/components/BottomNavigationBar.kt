package com.tehmaz.musicplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.GlassBorderGradient
import com.tehmaz.musicplayer.ui.theme.GoldenAmberGradient
import com.tehmaz.musicplayer.ui.theme.TextMuted

enum class NavTab(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Rounded.Home),
    PLAYLIST("playlists", "Playlist", Icons.Rounded.QueueMusic),
    SEARCH("search", "Discover", Icons.Rounded.Explore),
    LIBRARY("library", "Library", Icons.Rounded.LibraryMusic)
}

/**
 * Floating glassmorphic bottom navigation bar with active glowing amber pill.
 */
@Composable
fun FloatingBottomBar(
    currentRoute: String,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .shadow(
                elevation = 16.dp,
                shape = barShape,
                spotColor = AmberGoldPrimary.copy(alpha = 0.25f),
                ambientColor = Color.Black.copy(alpha = 0.7f)
            )
            .clip(barShape)
            .background(Color(0xF018110B))
            .border(1.dp, GlassBorderGradient, barShape)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.values().forEach { tab ->
                val isSelected = currentRoute.startsWith(tab.route)

                NavTabItem(
                    tab = tab,
                    isSelected = isSelected,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    tab: NavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF140E0A) else TextMuted,
        animationSpec = tween(durationMillis = 200),
        label = "nav_icon_color"
    )

    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            spotColor = AmberGoldPrimary.copy(alpha = 0.4f)
                        )
                        .background(GoldenAmberGradient)
                } else {
                    Modifier.background(Color.Transparent)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = AmberGoldPrimary),
                onClick = onClick
            )
            .padding(horizontal = if (isSelected) 16.dp else 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150))
            ) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.title,
                        color = Color(0xFF140E0A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
