package com.tehmaz.musicplayer.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tehmaz.musicplayer.data.model.Song

private const val PERMISSION_PREFS = "audio_permission_state"
private const val KEY_AUDIO_PERMISSION_REQUESTED = "audio_permission_requested"

/** Android 13+ requires READ_MEDIA_AUDIO; older versions use READ_EXTERNAL_STORAGE. */
fun audioPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

fun hasAudioPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, audioPermission()) == PackageManager.PERMISSION_GRANTED

fun hasNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

fun shareSong(context: Context, song: Song) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Listening to ${song.title} — ${song.artist}")
    }
    runCatching {
        context.startActivity(Intent.createChooser(send, "Share song").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure {
        Toast.makeText(context, "Nothing to share with", Toast.LENGTH_SHORT).show()
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Requests only the audio-library permission. A first denial does not incorrectly send the
 * user to Settings; Settings is offered after Android reports that the permission cannot
 * be requested again.
 */
@Composable
fun rememberPermissionRequester(onResult: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE)
    }
    var requestedBefore by remember {
        mutableStateOf(preferences.getBoolean(KEY_AUDIO_PERMISSION_REQUESTED, false))
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        val granted = hasAudioPermission(context)
        if (!granted) {
            val activity = context.findActivity()
            val canAskAgain = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, audioPermission())
            } ?: false
            val message = if (requestedBefore && !canAskAgain) {
                "Audio access was denied permanently. Enable Music and audio access in App settings."
            } else {
                "Allow Music and audio access to scan songs stored on this device."
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        requestedBefore = true
        preferences.edit().putBoolean(KEY_AUDIO_PERMISSION_REQUESTED, true).apply()
        onResult()
    }

    return remember(launcher, requestedBefore) {
        {
            if (hasAudioPermission(context)) {
                onResult()
            } else {
                val activity = context.findActivity()
                val permanentlyDenied = requestedBefore && activity != null &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, audioPermission())
                if (permanentlyDenied) {
                    openAppSettings(context)
                } else {
                    preferences.edit().putBoolean(KEY_AUDIO_PERMISSION_REQUESTED, true).apply()
                    launcher.launch(audioPermission())
                }
            }
        }
    }
}
