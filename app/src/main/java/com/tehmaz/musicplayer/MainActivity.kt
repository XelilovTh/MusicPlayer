package com.tehmaz.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tehmaz.musicplayer.ui.navigation.AppNavigation
import com.tehmaz.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicPlayerTheme {
                AppNavigation()
            }
        }
    }
}
