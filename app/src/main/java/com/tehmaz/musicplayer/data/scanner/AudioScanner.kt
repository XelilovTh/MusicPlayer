package com.tehmaz.musicplayer.data.scanner

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.tehmaz.musicplayer.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Scans local device storage for audio files using Android MediaStore.
 */
class AudioScanner(private val context: Context) {

    companion object {
        private const val TAG = "AudioScanner"
        private const val ALBUM_ART_BASE_URI_STRING = "content://media/external/audio/albumart"
        private val ALBUM_ART_URI: Uri by lazy {
            try {
                Uri.parse(ALBUM_ART_BASE_URI_STRING)
            } catch (_: Throwable) {
                Uri.EMPTY
            }
        }
    }

    /**
     * Checks if the required read audio / storage permission is granted.
     */
    fun hasPermission(): Boolean {
        return try {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Queries MediaStore for all local music tracks on Dispatchers.IO.
     * Returns an empty list if permission is missing, or in case of query failure.
     */
    suspend fun scanLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        if (!hasPermission()) {
            try {
                Log.d(TAG, "Audio permission not granted; returning empty list.")
            } catch (_: Throwable) {}
            return@withContext emptyList()
        }

        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val rawTitle = cursor.getString(titleCol)
                    val rawArtist = cursor.getString(artistCol)
                    val rawAlbum = cursor.getString(albumCol)
                    val durationMs = cursor.getLong(durationCol)
                    val albumId = cursor.getLong(albumIdCol)

                    val contentUri = try {
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        ).toString()
                    } catch (_: Throwable) {
                        "content://media/external/audio/media/$id"
                    }

                    val albumArtUri = if (albumId > 0) {
                        try {
                            ContentUris.withAppendedId(ALBUM_ART_URI, albumId).toString()
                        } catch (_: Throwable) {
                            "$ALBUM_ART_BASE_URI_STRING/$albumId"
                        }
                    } else {
                        null
                    }

                    songs.add(
                        Song(
                            id = id,
                            title = if (rawTitle.isNullOrBlank()) "Unknown Title" else rawTitle,
                            artist = if (rawArtist.isNullOrBlank() || rawArtist == "<unknown>") "Unknown Artist" else rawArtist,
                            album = if (rawAlbum.isNullOrBlank()) "Unknown Album" else rawAlbum,
                            durationMs = durationMs,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            try { Log.e(TAG, "SecurityException while scanning audio: ${e.message}", e) } catch (_: Throwable) {}
        } catch (e: Exception) {
            try { Log.e(TAG, "Exception while scanning audio: ${e.message}", e) } catch (_: Throwable) {}
        }

        try { Log.d(TAG, "Scanned ${songs.size} local audio files.") } catch (_: Throwable) {}
        return@withContext songs
    }
}
