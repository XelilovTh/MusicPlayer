package com.tehmaz.musicplayer.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tehmaz.musicplayer.ui.components.DiscoverBadge
import com.tehmaz.musicplayer.ui.components.GlassIconButton
import com.tehmaz.musicplayer.ui.components.GlassmorphicCard
import com.tehmaz.musicplayer.ui.components.GlowBackground
import com.tehmaz.musicplayer.ui.theme.AmberGoldDark
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.TextMuted
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    GlowBackground(
        showTopGlow = true,
        showCenterGlow = false,
        showBottomGlow = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 44.dp, bottom = 40.dp)
        ) {
            // Header Bar
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
                    text = "Settings",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Card: Tehmaz Xelilov
            ProfileCard(
                name = "Tehmaz Xelilov",
                email = "tehmaz.xelilov@example.com",
                badgeText = "Premium Member"
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Audio & Playback Section
            SettingsSectionHeader(title = "Audio & Playback")
            Spacer(modifier = Modifier.height(10.dp))

            SettingsOptionItem(
                icon = Icons.Rounded.HighQuality,
                title = "Streaming Quality",
                subtitle = "High • 320 kbps FLAC / AAC"
            )
            SettingsOptionItem(
                icon = Icons.Rounded.GraphicEq,
                title = "Audio Equalizer",
                subtitle = "Custom presets & bass enhancer"
            )
            SettingsOptionItem(
                icon = Icons.Rounded.Download,
                title = "Download Over Wi-Fi Only",
                subtitle = "Save mobile cellular data"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // General & Preferences Section
            SettingsSectionHeader(title = "General & Preferences")
            Spacer(modifier = Modifier.height(10.dp))

            SettingsSwitchItem(
                icon = Icons.Rounded.Notifications,
                title = "Notifications",
                subtitle = "Playback controls & recommendations",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            SettingsOptionItem(
                icon = Icons.Rounded.DarkMode,
                title = "Appearance",
                subtitle = "Obsidian Dark & Warm Amber Gold"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App & Support Section
            SettingsSectionHeader(title = "Information & Support")
            Spacer(modifier = Modifier.height(10.dp))

            SettingsOptionItem(
                icon = Icons.Rounded.Info,
                title = "About Music Player",
                subtitle = "Version 1.0.0 (Production Build)"
            )
            SettingsOptionItem(
                icon = Icons.Rounded.HelpOutline,
                title = "Help & Feedback",
                subtitle = "FAQs, contact & bug reporting"
            )
        }
    }
}

@Composable
private fun ProfileCard(
    name: String,
    email: String,
    badgeText: String
) {
    val cardShape = RoundedCornerShape(22.dp)

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = cardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with gold border
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFFC067), AmberGoldPrimary, AmberGoldDark)
                        )
                    )
                    .border(2.dp, AmberGoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Color(0xFF140E0A),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = email,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = AmberGoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badgeText,
                        color = AmberGoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = AmberGoldPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
private fun SettingsOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF241810))
                    .border(1.dp, Color(0x33F5B853), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AmberGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF241810))
                    .border(1.dp, Color(0x33F5B853), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AmberGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AmberGoldPrimary,
                    checkedTrackColor = Color(0xFF382518),
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = Color(0xFF1E140E)
                )
            )
        }
    }
}
