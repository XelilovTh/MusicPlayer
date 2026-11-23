package com.tehmaz.musicplayer.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tehmaz.musicplayer.ui.theme.AmberGoldDark
import com.tehmaz.musicplayer.ui.theme.AmberGoldLight
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import kotlin.math.sin

/**
 * Animated audio waveform visualizer for Equalizer screen and player headers.
 */
@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    isPlaying: Boolean = true
) {
    val phase = if (isPlaying) {
        val transition = rememberInfiniteTransition(label = "waveform_anim")
        val animatedPhase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase_shift"
        )
        animatedPhase
    } else {
        0f
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val midY = size.height / 2f
        val points = 60
        val step = width / points

        val primaryPath = Path()
        val secondaryPath = Path()

        for (i in 0..points) {
            val x = i * step
            val normalizedX = (i.toFloat() / points)
            val envelope = sin(normalizedX * Math.PI).toFloat()

            val effectivePhase = if (isPlaying) phase else 0f
            val y1 = midY + sin(normalizedX * 12f + effectivePhase) * (size.height * 0.35f * envelope)
            val y2 = midY + sin(normalizedX * 8f - effectivePhase * 0.7f) * (size.height * 0.25f * envelope)

            if (i == 0) {
                primaryPath.moveTo(x, y1)
                secondaryPath.moveTo(x, y2)
            } else {
                primaryPath.lineTo(x, y1)
                secondaryPath.lineTo(x, y2)
            }
        }

        // Secondary subtle wave
        drawPath(
            path = secondaryPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    AmberGoldDark.copy(alpha = 0.35f),
                    AmberGoldPrimary.copy(alpha = 0.45f),
                    Color.Transparent
                )
            ),
            style = Stroke(width = 2.dp.toPx())
        )

        // Primary glowing gold wave
        drawPath(
            path = primaryPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    AmberGoldLight,
                    AmberGoldPrimary,
                    Color.Transparent
                )
            ),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
