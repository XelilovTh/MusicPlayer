package com.tehmaz.musicplayer.ui.screens.sleeptimer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.tehmaz.musicplayer.playback.AudioPlaybackManager
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
import org.koin.compose.koinInject
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleepTimerScreen(onBackClick: () -> Unit) {
    val playbackManager: AudioPlaybackManager = koinInject()
    val remainingMs by playbackManager.sleepTimerRemainingMs.collectAsState()
    var timerMinutes by remember { mutableIntStateOf(30) }
    val isRunning = remainingMs != null
    val displayTime = remainingMs?.let(::formatRemaining) ?: "${timerMinutes}:00"

    GlowBackground(showTopGlow = true, showCenterGlow = true, showBottomGlow = true) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(top = 44.dp, bottom = 40.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(onClick = onBackClick, size = 40.dp) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Sleep Timer", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(40.dp))
            }

            Spacer(Modifier.height(34.dp))
            Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width * 0.42f
                    drawCircle(Color(0x22F5B853), radius, center,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                    val total = (timerMinutes.coerceAtLeast(1) * 60_000L).toFloat()
                    val progress = if (remainingMs != null) {
                        (remainingMs!!.toFloat() / total).coerceIn(0f, 1f)
                    } else 1f
                    drawArc(
                        brush = Brush.sweepGradient(listOf(AmberGoldLight, AmberGoldPrimary, AmberGoldDark), center),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = AmberGoldPrimary,
                        modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(10.dp))
                    Text(displayTime, color = TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Text(if (isRunning) "PLAYBACK WILL STOP" else "MINUTES",
                        color = AmberGoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text(if (isRunning) "Timer is active" else "Choose when music should stop",
                        color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                AdjustButton(Icons.Rounded.Remove) { if (!isRunning) timerMinutes = (timerMinutes - 5).coerceAtLeast(5) }
                Text("± 5 min", color = TextSecondary, fontSize = 13.sp)
                AdjustButton(Icons.Rounded.Add) { if (!isRunning) timerMinutes = (timerMinutes + 5).coerceAtMost(240) }
            }

            Spacer(Modifier.height(28.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(15, 30, 45, 60, 90, 120).forEach { minutes ->
                    CategoryPill(
                        text = if (minutes == 60) "1 hour" else "$minutes min",
                        isSelected = !isRunning && timerMinutes == minutes,
                        onClick = { if (!isRunning) timerMinutes = minutes }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            GoldenPillButton(
                text = if (isRunning) "Stop Timer" else "Start Timer",
                icon = if (isRunning) Icons.Rounded.Stop else Icons.Rounded.Bedtime,
                onClick = {
                    if (isRunning) playbackManager.cancelSleepTimer()
                    else playbackManager.startSleepTimer(timerMinutes)
                },
                modifier = Modifier.fillMaxWidth(0.75f)
            )
        }
    }
}

private fun formatRemaining(milliseconds: Long): String {
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.coerceAtLeast(0))
    return String.format("%02d:%02d", seconds / 60, seconds % 60)
}

@Composable
private fun AdjustButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).shadow(6.dp, CircleShape).clip(CircleShape)
            .background(Color(0xFF241810)).border(1.dp, Color(0x40F5B853), CircleShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = AmberGoldPrimary, modifier = Modifier.size(24.dp))
    }
}
