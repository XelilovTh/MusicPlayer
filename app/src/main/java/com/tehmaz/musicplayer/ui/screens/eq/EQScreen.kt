package com.tehmaz.musicplayer.ui.screens.eq

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tehmaz.musicplayer.audio.EqualizerController
import com.tehmaz.musicplayer.ui.components.CategoryPill
import com.tehmaz.musicplayer.ui.components.GlassIconButton
import com.tehmaz.musicplayer.ui.components.GlassmorphicCard
import com.tehmaz.musicplayer.ui.components.GlowBackground
import com.tehmaz.musicplayer.ui.theme.AmberGoldLight
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.TextMuted
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary
import org.koin.androidx.compose.koinInject

@Composable
fun EQScreen(onBackClick: () -> Unit) {
    val controller: EqualizerController = koinInject()
    val state by controller.state.collectAsState()
    val presets = EqualizerController.PRESET_NAMES

    GlowBackground(showTopGlow = true, showCenterGlow = false, showBottomGlow = true) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(top = 44.dp, bottom = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(onClick = onBackClick, size = 40.dp) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = AmberGoldPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Equalizer", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = state.enabled,
                    onCheckedChange = controller::setEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberGoldPrimary,
                        checkedTrackColor = Color(0xFF382518),
                        uncheckedThumbColor = TextMuted
                    )
                )
            }

            Spacer(Modifier.height(22.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presets) { preset ->
                    CategoryPill(
                        text = preset,
                        isSelected = preset == state.preset,
                        onClick = { controller.applyPreset(preset) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            if (!state.available) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(
                        "The device equalizer is not available yet. Start playing a local track; some devices do not expose audio effects.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            } else {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(String.format("%.0f dB", state.maxDb), color = TextMuted, fontSize = 11.sp)
                            Text("0 dB", color = TextMuted, fontSize = 11.sp)
                            Text(String.format("%.0f dB", state.minDb), color = TextMuted, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth().height(180.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            state.bands.forEachIndexed { index, band ->
                                val level = state.levelsDb.getOrElse(index) { 0f }
                                BandSlider(
                                    label = band.label,
                                    value = level,
                                    min = state.minDb,
                                    max = state.maxDb,
                                    enabled = state.enabled,
                                    onValueChange = { controller.setBandLevel(band.index, it) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EffectCard(
                    title = "Bass Boost",
                    value = state.bassBoost,
                    enabled = state.enabled && state.bassBoostSupported,
                    onValueChange = controller::setBassBoost,
                    modifier = Modifier.weight(1f)
                )
                EffectCard(
                    title = "3D Audio",
                    value = state.virtualizer,
                    enabled = state.enabled && state.virtualizerSupported,
                    onValueChange = controller::setVirtualizer,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!state.bassBoostSupported || !state.virtualizerSupported) {
                Text(
                    "Some audio effects are not supported by this device or its audio driver.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun BandSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (value > 0) "+${value.toInt()}" else value.toInt().toString(),
            color = if (enabled) AmberGoldPrimary else TextMuted, fontSize = 11.sp)
        Box(Modifier.width(36.dp).height(112.dp), contentAlignment = Alignment.Center) {
            Slider(
                value = value.coerceIn(min, max),
                onValueChange = onValueChange,
                valueRange = min..max,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = AmberGoldLight,
                    activeTrackColor = AmberGoldPrimary,
                    inactiveTrackColor = Color(0x33F5B853)
                ),
                modifier = Modifier.size(width = 110.dp, height = 36.dp).rotate(-90f)
            )
        }
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun EffectCard(
    title: String,
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("${(value * 100).toInt()}%", color = AmberGoldPrimary, fontSize = 12.sp)
            Slider(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = AmberGoldLight,
                    activeTrackColor = AmberGoldPrimary,
                    inactiveTrackColor = Color(0x33F5B853)
                )
            )
        }
    }
}
