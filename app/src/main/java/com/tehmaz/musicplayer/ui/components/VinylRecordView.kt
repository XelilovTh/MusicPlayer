package com.tehmaz.musicplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tehmaz.musicplayer.ui.theme.AmberGoldDark
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.AmberGoldSecondary

/**
 * Custom Canvas Vinyl Record with realistic concentric grooves,
 * glossy reflection sheen, golden center label, and continuous spinning when active.
 */
@Composable
fun VinylRecordView(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    isPlaying: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )

    val currentRotation = if (isPlaying) rotation else 0f

    Box(
        modifier = modifier
            .size(size)
            .rotate(currentRotation),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = size.toPx() / 2f

            // Outer vinyl body
            drawCircle(
                color = Color(0xFF140E0A),
                radius = radius,
                center = center
            )

            // Outer gold-tinted rim highlight
            drawCircle(
                color = Color(0x33F5B853),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Concentric vinyl grooves with subtle opacities
            val grooveFractions = listOf(
                0.92f, 0.88f, 0.84f, 0.80f, 0.76f, 0.72f, 0.68f, 0.64f, 0.60f, 0.56f, 0.52f
            )
            grooveFractions.forEachIndexed { index, fraction ->
                val grooveColor = if (index % 3 == 0) {
                    Color(0x30FFFFFF)
                } else if (index % 2 == 0) {
                    Color(0x22F5B853)
                } else {
                    Color(0x18FFFFFF)
                }
                drawCircle(
                    color = grooveColor,
                    radius = radius * fraction,
                    center = center,
                    style = Stroke(width = 1.2f)
                )
            }

            // Stylized light sheen / reflection wedges
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x28FFFFFF),
                        Color(0x00FFFFFF)
                    ),
                    center = center,
                    radius = radius
                ),
                startAngle = 30f,
                sweepAngle = 45f,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = this.size
            )
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x28FFFFFF),
                        Color(0x00FFFFFF)
                    ),
                    center = center,
                    radius = radius
                ),
                startAngle = 210f,
                sweepAngle = 45f,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = this.size
            )

            // Center Label (Golden amber gradient disc)
            val labelRadius = radius * 0.38f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AmberGoldPrimary,
                        AmberGoldSecondary,
                        AmberGoldDark
                    ),
                    center = center,
                    radius = labelRadius
                ),
                radius = labelRadius,
                center = center
            )

            // Inner label ring
            drawCircle(
                color = Color(0x80140E0A),
                radius = labelRadius * 0.75f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Center spindle hole
            val holeRadius = labelRadius * 0.22f
            drawCircle(
                color = Color(0xFF0C0907),
                radius = holeRadius,
                center = center
            )
            drawCircle(
                color = AmberGoldPrimary.copy(alpha = 0.8f),
                radius = holeRadius,
                center = center,
                style = Stroke(width = 1f)
            )
        }
    }
}
