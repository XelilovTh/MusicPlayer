package com.tehmaz.musicplayer.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tehmaz.musicplayer.ui.components.GlassIconButton
import com.tehmaz.musicplayer.ui.components.GlassmorphicCard
import com.tehmaz.musicplayer.ui.components.GlowBackground
import com.tehmaz.musicplayer.ui.components.GoldenPillButton
import com.tehmaz.musicplayer.ui.components.hasAudioPermission
import com.tehmaz.musicplayer.ui.components.openAppSettings
import com.tehmaz.musicplayer.ui.theme.AmberGoldPrimary
import com.tehmaz.musicplayer.ui.theme.TextPrimary
import com.tehmaz.musicplayer.ui.theme.TextSecondary

@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasAudioPermission(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionGranted = hasAudioPermission(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    GlowBackground(showTopGlow = true, showCenterGlow = false, showBottomGlow = true) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(top = 44.dp, bottom = 40.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(onClick = onBackClick, size = 40.dp) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("Settings", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(40.dp))
            }

            Spacer(Modifier.height(28.dp))
            Text("MUSIC LIBRARY", color = AmberGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            GlassmorphicCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = AmberGoldPrimary)
                        Spacer(Modifier.size(10.dp))
                        Column {
                            Text("Local music", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (permissionGranted) "Access to device audio is allowed"
                                else "Permission is required to read songs on this device",
                                color = TextSecondary, fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    GoldenPillButton(
                        text = "Manage app permissions",
                        onClick = {
                            openAppSettings(context)
                            permissionGranted = hasAudioPermission(context)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("ABOUT", color = AmberGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            GlassmorphicCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = AmberGoldPrimary)
                        Spacer(Modifier.size(10.dp))
                        Column {
                            Text("Nota", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Version 1.0.0", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = AmberGoldPrimary)
                        Spacer(Modifier.size(10.dp))
                        Column {
                            Text("Offline by design", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Your music stays on your device. No streaming catalog or account is required.",
                                color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
