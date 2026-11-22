package com.tehmaz.musicplayer.ui.screens.eq

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tehmaz.musicplayer.ui.components.CategoryPill
import com.tehmaz.musicplayer.ui.components.GlassIconButton
import com.tehmaz.musicplayer.ui.components.GlassmorphicCard
import com.tehmaz.musicplayer.ui.components.GlowBackground
import com.tehmaz.musicplayer.ui.components.WaveformVisualizer
import com.tehmaz.musicplayer.ui.theme.AmberGoldDark
import com.tehmaz.musicplayer.ui.theme.AmberGoldLight
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.TextMuted
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary

@Composable
fun EQScreen(
    onBackClick: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(true) }
    var selectedPreset by remember { mutableStateOf("Rock") }
    val presets = listOf("Normal", "Rock", "Pop", "Jazz", "Classical", "Custom")

    var band60Hz by remember { mutableFloatStateOf(4f) }
    var band230Hz by remember { mutableFloatStateOf(2f) }
    var band910Hz by remember { mutableFloatStateOf(-1f) }
    var band3600Hz by remember { mutableFloatStateOf(3f) }
    var band14000Hz by remember { mutableFloatStateOf(5f) }

    var bassBoost by remember { mutableFloatStateOf(0.65f) }
    var virtualizer by remember { mutableFloatStateOf(0.40f) }

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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = AmberGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Equalizer",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberGoldPrimary,
                        checkedTrackColor = Color(0xFF382518),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0xFF1E140E)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preset Pills Horizontal Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presets) { preset ->
                    CategoryPill(
                        text = preset,
                        isSelected = preset == selectedPreset,
                        onClick = { selectedPreset = preset }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 5-Band Graphic Equalizer Section
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "+10 dB",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "0 dB",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "-10 dB",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5 Vertical Sliders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EQBandSlider(label = "60Hz", value = band60Hz, onValueChange = { band60Hz = it }, enabled = isEnabled)
                        EQBandSlider(label = "230Hz", value = band230Hz, onValueChange = { band230Hz = it }, enabled = isEnabled)
                        EQBandSlider(label = "910Hz", value = band910Hz, onValueChange = { band910Hz = it }, enabled = isEnabled)
                        EQBandSlider(label = "3.6kHz", value = band3600Hz, onValueChange = { band3600Hz = it }, enabled = isEnabled)
                        EQBandSlider(label = "14kHz", value = band14000Hz, onValueChange = { band14000Hz = it }, enabled = isEnabled)
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Bass Boost & Virtualizer Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Bass Boost
                GlassmorphicCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Bass Boost",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(bassBoost * 100).toInt()}%",
                            color = AmberGoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = bassBoost,
                            onValueChange = { bassBoost = it },
                            enabled = isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = AmberGoldLight,
                                activeTrackColor = AmberGoldPrimary,
                                inactiveTrackColor = Color(0x33F5B853)
                            )
                        )
                    }
                }

                // Virtualizer
                GlassmorphicCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "3D Audio",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(virtualizer * 100).toInt()}%",
                            color = AmberGoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = virtualizer,
                            onValueChange = { virtualizer = it },
                            enabled = isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = AmberGoldLight,
                                activeTrackColor = AmberGoldPrimary,
                                inactiveTrackColor = Color(0x33F5B853)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Bottom Waveform Visualization
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Acoustic Spectrum",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                WaveformVisualizer(
                    height = 65.dp,
                    isPlaying = isEnabled
                )
            }
        }
    }
}

@Composable
private fun EQBandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Value text
        Text(
            text = if (value > 0) "+${value.toInt()}" else "${value.toInt()}",
            color = if (enabled) AmberGoldPrimary else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Vertical Slider container
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = -10f..10f,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = AmberGoldLight,
                    activeTrackColor = AmberGoldPrimary,
                    inactiveTrackColor = Color(0x33F5B853)
                ),
                modifier = Modifier
                    .size(width = 110.dp, height = 36.dp)
                    .rotate(-90f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Frequency Label
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
