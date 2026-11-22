package com.tehmaz.musicplayer.ui.screens.sleeptimer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tehmaz.musicplayer.ui.components.CategoryPill
import com.tehmaz.musicplayer.ui.components.GlassIconButton
import com.tehmaz.musicplayer.ui.components.GlowBackground
import com.tehmaz.musicplayer.ui.components.GoldenPillButton
import com.tehmaz.musicplayer.ui.theme.AmberGoldDark
import com.tehmaz.musicplayer.ui.theme.AmberGoldLight
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.TextMuted
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleepTimerScreen(
    onBackClick: () -> Unit
) {
    var timerMinutes by remember { mutableIntStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf("30 min") }

    val presetOptions = listOf("Off", "15 min", "30 min", "45 min", "1 hour", "Custom")

    GlowBackground(
        showTopGlow = true,
        showCenterGlow = true,
        showBottomGlow = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 44.dp, bottom = 40.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    text = "Sleep Timer",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Placeholder to balance title
                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Glowing Circular Countdown Dial
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                // Dial Canvas with arc track and glowing progress
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width * 0.42f

                    // Background Track
                    drawCircle(
                        color = Color(0x22F5B853),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Active Progress Arc
                    val sweep = ((timerMinutes.coerceIn(0, 120)) / 120f) * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                AmberGoldLight,
                                AmberGoldPrimary,
                                AmberGoldDark,
                                AmberGoldLight
                            ),
                            center = center
                        ),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Dial Center Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = null,
                        tint = AmberGoldPrimary,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$timerMinutes",
                        color = TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "MINUTES",
                        color = AmberGoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isTimerRunning) "Active • Stopping playback" else "Timer paused",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Plus & Minus adjusters
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdjustCircleButton(
                    icon = Icons.Rounded.Remove,
                    onClick = {
                        if (timerMinutes > 5) {
                            timerMinutes -= 5
                            selectedPreset = "Custom"
                        }
                    }
                )

                Text(
                    text = "± 5 min",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                AdjustCircleButton(
                    icon = Icons.Rounded.Add,
                    onClick = {
                        if (timerMinutes < 180) {
                            timerMinutes += 5
                            selectedPreset = "Custom"
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timer Preset Options FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presetOptions.forEach { preset ->
                    CategoryPill(
                        text = preset,
                        isSelected = preset == selectedPreset,
                        onClick = {
                            selectedPreset = preset
                            when (preset) {
                                "Off" -> {
                                    timerMinutes = 0
                                    isTimerRunning = false
                                }
                                "15 min" -> timerMinutes = 15
                                "30 min" -> timerMinutes = 30
                                "45 min" -> timerMinutes = 45
                                "1 hour" -> timerMinutes = 60
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Start / Stop Timer Button
            GoldenPillButton(
                text = if (isTimerRunning) "■ Stop Timer" else "▶ Start Timer",
                icon = if (isTimerRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                onClick = { isTimerRunning = !isTimerRunning },
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}

@Composable
private fun AdjustCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                spotColor = AmberGoldPrimary.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(Color(0xFF241810))
            .border(1.dp, Color(0x40F5B853), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = AmberGoldPrimary),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AmberGoldPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}
