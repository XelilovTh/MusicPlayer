package com.tehmaz.musicplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AmberGoldPrimary,
    onPrimary = Color(0xFF140E0A),
    primaryContainer = AmberGoldDark,
    onPrimaryContainer = Color(0xFFFFF1DC),
    secondary = AmberGoldSecondary,
    onSecondary = Color(0xFF140E0A),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = AmberGoldDark.copy(alpha = 0.4f),
    outlineVariant = Color(0x26F5B853)
)

@Composable
fun MusicPlayerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
