package com.tehmaz.musicplayer.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Obsidian & Espresso Black Palette
val DarkBackground = Color(0xFF0C0907)
val DarkSurface = Color(0xFF140E0A)
val DarkCardSurface = Color(0xFF1C130D)
val DarkElevatedSurface = Color(0xFF251A12)

// Translucent Frosted Glass Surfaces
val GlassSurface = Color(0xFF211711).copy(alpha = 0.70f)
val GlassSurfaceLight = Color(0xFF2C1F17).copy(alpha = 0.55f)
val GlassSurfaceSubtle = Color(0xFF18100B).copy(alpha = 0.85f)

// Warm Amber & Gold Accents
val AmberGoldPrimary = Color(0xFFF5B853)
val AmberGoldSecondary = Color(0xFFE69A35)
val AmberGoldDark = Color(0xFFD49B4E)
val AmberGoldLight = Color(0xFFFFC067)
val AmberGoldGlow = Color(0x66F5B853)
val AmberGlowAmbient = Color(0x26F5B853)
val AmberGlowStrong = Color(0x99F5B853)

// Text Colors
val TextPrimary = Color(0xFFFDFBF7)
val TextSecondary = Color(0xFFB8AFA6)
val TextMuted = Color(0xFF7D7268)
val TextGold = Color(0xFFFFC671)

// Neutral & Status
val DividerColor = Color(0x1FFFFFFF)
val ErrorRed = Color(0xFFE55353)
val SuccessGreen = Color(0xFF4EBE7B)

// Luxury Gradients
val GoldenAmberGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFFFC067),
        Color(0xFFF5B853),
        Color(0xFFD49B4E)
    )
)

val GoldenAmberVerticalGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFC671),
        Color(0xFFF5B853),
        Color(0xFFD49B4E)
    )
)

val HeroVinylGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2E1C12),
        Color(0xFF1F130B),
        Color(0xFF140D07)
    )
)

val GlassBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x66F5B853),
        Color(0x1AFFFFFF),
        Color(0x33F5B853)
    )
)

val SubtleGoldBorder = Brush.linearGradient(
    colors = listOf(
        Color(0x40F5B853),
        Color(0x10F5B853),
        Color(0x30F5B853)
    )
)
