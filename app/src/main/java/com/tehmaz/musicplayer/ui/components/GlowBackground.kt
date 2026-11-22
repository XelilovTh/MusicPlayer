package com.tehmaz.musicplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.DarkBackground

/**
 * Screen wrapper that renders a deep obsidian/espresso backdrop with
 * warm ambient amber/gold glows placed at atmospheric coordinates.
 */
@Composable
fun GlowBackground(
    modifier: Modifier = Modifier,
    showTopGlow: Boolean = true,
    showCenterGlow: Boolean = false,
    showBottomGlow: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (showTopGlow) {
                // Top-right warm amber ambient aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AmberGoldPrimary.copy(alpha = 0.18f),
                            AmberGoldPrimary.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.85f, height * 0.12f),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.75f
                )

                // Top-left subtle warm aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE69A35).copy(alpha = 0.12f),
                            Color(0xFF140E0A).copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.15f, height * 0.05f),
                        radius = width * 0.55f
                    ),
                    center = Offset(width * 0.15f, height * 0.05f),
                    radius = width * 0.55f
                )
            }

            if (showCenterGlow) {
                // Large central ambient amber spotlight for Now Playing album art
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AmberGoldPrimary.copy(alpha = 0.22f),
                            Color(0xFFD49B4E).copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.5f, height * 0.38f),
                        radius = width * 0.7f
                    ),
                    center = Offset(width * 0.5f, height * 0.38f),
                    radius = width * 0.7f
                )
            }

            if (showBottomGlow) {
                // Bottom ambient amber glow behind dock/navigation
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AmberGoldPrimary.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.5f, height * 0.95f),
                        radius = width * 0.6f
                    ),
                    center = Offset(width * 0.5f, height * 0.95f),
                    radius = width * 0.6f
                )
            }
        }

        content()
    }
}
