package com.tehmaz.musicplayer.data.store

import android.content.Context
import android.content.SharedPreferences

/**
 * Tiny, crash-proof wrapper around SharedPreferences.
 *
 * Every value is stored as a String so that a missing or mocked Context (unit tests)
 * simply behaves like an empty store instead of returning misleading defaults.
 */
class KeyValueStore(
    private val context: Context?,
    private val fileName: String = "music_player_prefs"
) {

    private val prefs: SharedPreferences? by lazy {
        try {
            context?.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        } catch (_: Throwable) {
            null
        }
    }

    fun getString(key: String): String? {
        return try {
            prefs?.getString(key, null)?.takeIf { it.isNotEmpty() }
        } catch (_: Throwable) {
            null
        }
    }

    fun putString(key: String, value: String?) {
        try {
            val editor = prefs?.edit() ?: return
            if (value.isNullOrEmpty()) {
                editor.remove(key)
            } else {
                editor.putString(key, value)
            }
            editor.apply()
        } catch (_: Throwable) {
            // Persistence is best-effort; the app keeps working with in-memory state.
        }
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        val raw = getString(key) ?: return default
        return raw == "1"
    }

    fun putBoolean(key: String, value: Boolean) {
        putString(key, if (value) "1" else "0")
    }

    fun getFloat(key: String, default: Float): Float {
        return getString(key)?.toFloatOrNull() ?: default
    }

    fun putFloat(key: String, value: Float) {
        putString(key, value.toString())
    }
}
