package com.tehmaz.musicplayer.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.tehmaz.musicplayer.data.store.KeyValueStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Real audio effects (equalizer, bass boost, virtualizer) attached to the player's audio session.
 *
 * The playback service calls [attach] with the ExoPlayer audio session id; the UI reads [state]
 * and changes settings. Settings are persisted and re-applied whenever the session changes.
 */
class EqualizerController(context: Context?) {

    data class Band(val index: Int, val centerFreqHz: Int) {
        val label: String
            get() = if (centerFreqHz >= 1000) {
                String.format(Locale.US, "%.1f", centerFreqHz / 1000f).removeSuffix(".0") + "kHz"
            } else {
                "${centerFreqHz}Hz"
            }
    }

    data class State(
        val available: Boolean = false,
        val enabled: Boolean = false,
        val bands: List<Band> = emptyList(),
        val levelsDb: List<Float> = emptyList(),
        val minDb: Float = -10f,
        val maxDb: Float = 10f,
        val preset: String = PRESET_NORMAL,
        val bassBoost: Float = 0f,
        val bassBoostSupported: Boolean = false,
        val virtualizer: Float = 0f,
        val virtualizerSupported: Boolean = false
    )

    companion object {
        private const val TAG = "EqualizerController"
        const val PRESET_NORMAL = "Normal"
        const val PRESET_CUSTOM = "Custom"

        private val REFERENCE_FREQS = intArrayOf(60, 230, 910, 3600, 14000)

        /** Preset curves in dB for the reference frequencies above. */
        val PRESETS: Map<String, List<Float>> = linkedMapOf(
            PRESET_NORMAL to listOf(0f, 0f, 0f, 0f, 0f),
            "Rock" to listOf(4f, 2f, -1f, 3f, 5f),
            "Pop" to listOf(-1f, 2f, 4f, 2f, -1f),
            "Jazz" to listOf(3f, 1f, -2f, 2f, 4f),
            "Classical" to listOf(4f, 2f, -1f, 2f, 3f)
        )

        val PRESET_NAMES: List<String> = PRESETS.keys.toList() + PRESET_CUSTOM

        private const val KEY_ENABLED = "eq_enabled"
        private const val KEY_PRESET = "eq_preset"
        private const val KEY_LEVELS = "eq_levels"
        private const val KEY_BASS = "eq_bass"
        private const val KEY_VIRTUALIZER = "eq_virtualizer"
    }

    private val store = KeyValueStore(context)

    private var settingsEnabled = store.getBoolean(KEY_ENABLED, false)
    private var settingsPreset = store.getString(KEY_PRESET) ?: PRESET_NORMAL
    private var settingsLevels: List<Float> =
        store.getString(KEY_LEVELS)?.split(',')?.mapNotNull { it.toFloatOrNull() } ?: emptyList()
    private var settingsBass = store.getFloat(KEY_BASS, 0f)
    private var settingsVirtualizer = store.getFloat(KEY_VIRTUALIZER, 0f)

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var sessionId: Int = -1

    private val _state = MutableStateFlow(
        State(
            enabled = settingsEnabled,
            preset = settingsPreset,
            bassBoost = settingsBass,
            virtualizer = settingsVirtualizer
        )
    )
    val state: StateFlow<State> = _state.asStateFlow()

    /** Attaches the effects to the player's audio session (called by the playback service). */
    fun attach(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        if (audioSessionId == sessionId && equalizer != null) return

        releaseEffects()
        sessionId = audioSessionId

        equalizer = try {
            Equalizer(0, audioSessionId)
        } catch (t: Throwable) {
            Log.w(TAG, "Equalizer unavailable: ${t.message}")
            null
        }
        bassBoost = try {
            val effect = BassBoost(0, audioSessionId)
            if (effect.strengthSupported) effect else { effect.release(); null }
        } catch (t: Throwable) {
            null
        }
        virtualizer = try {
            val effect = Virtualizer(0, audioSessionId)
            if (effect.strengthSupported) effect else { effect.release(); null }
        } catch (t: Throwable) {
            null
        }

        applyAndPublish()
    }

    /** Releases all effects (called when the playback service is destroyed). */
    fun release() {
        releaseEffects()
        sessionId = -1
        _state.value = _state.value.copy(
            available = false,
            bands = emptyList(),
            levelsDb = emptyList(),
            bassBoostSupported = false,
            virtualizerSupported = false
        )
    }

    fun setEnabled(enabled: Boolean) {
        settingsEnabled = enabled
        store.putBoolean(KEY_ENABLED, enabled)
        applyAndPublish()
    }

    fun setBandLevel(index: Int, db: Float) {
        val current = _state.value
        if (index !in current.levelsDb.indices) return
        val updated = current.levelsDb.toMutableList()
        updated[index] = db.coerceIn(current.minDb, current.maxDb)
        settingsLevels = updated
        settingsPreset = PRESET_CUSTOM
        persistCurve()
        applyAndPublish()
    }

    fun applyPreset(name: String) {
        val current = _state.value
        settingsPreset = name
        if (name != PRESET_CUSTOM) {
            settingsLevels = presetLevelsFor(name, current.bands)
        }
        persistCurve()
        applyAndPublish()
    }

    fun setBassBoost(strength: Float) {
        settingsBass = strength.coerceIn(0f, 1f)
        store.putFloat(KEY_BASS, settingsBass)
        applyAndPublish()
    }

    fun setVirtualizer(strength: Float) {
        settingsVirtualizer = strength.coerceIn(0f, 1f)
        store.putFloat(KEY_VIRTUALIZER, settingsVirtualizer)
        applyAndPublish()
    }

    private fun persistCurve() {
        store.putString(KEY_PRESET, settingsPreset)
        store.putString(KEY_LEVELS, settingsLevels.joinToString(","))
    }

    private fun presetLevelsFor(name: String, bands: List<Band>): List<Float> {
        val curve = PRESETS[name] ?: return List(bands.size) { 0f }
        return bands.map { band ->
            val logFreq = ln(band.centerFreqHz.toDouble().coerceAtLeast(1.0))
            val nearest = REFERENCE_FREQS.indices.minByOrNull {
                abs(logFreq - ln(REFERENCE_FREQS[it].toDouble()))
            } ?: 0
            curve[nearest]
        }
    }

    private fun applyAndPublish() {
        var bands: List<Band> = emptyList()
        var levels: List<Float> = emptyList()
        var minDb = -10f
        var maxDb = 10f

        val eq = equalizer
        if (eq != null) {
            try {
                val range = eq.bandLevelRange
                minDb = range[0] / 100f
                maxDb = range[1] / 100f
                val count = eq.numberOfBands.toInt()
                bands = (0 until count).map { Band(it, eq.getCenterFreq(it.toShort()) / 1000) }

                val wanted = if (settingsLevels.size == count) {
                    settingsLevels
                } else {
                    presetLevelsFor(settingsPreset, bands)
                }
                levels = wanted.map { it.coerceIn(minDb, maxDb) }
                settingsLevels = levels

                levels.forEachIndexed { index, db ->
                    eq.setBandLevel(index.toShort(), (db * 100).roundToInt().toShort())
                }
                eq.setEnabled(settingsEnabled)
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to apply equalizer: ${t.message}")
            }
        }

        bassBoost?.let { effect ->
            try {
                effect.setStrength((settingsBass * 1000).roundToInt().toShort())
                effect.setEnabled(settingsEnabled && settingsBass > 0f)
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to apply bass boost: ${t.message}")
            }
        }

        virtualizer?.let { effect ->
            try {
                effect.setStrength((settingsVirtualizer * 1000).roundToInt().toShort())
                effect.setEnabled(settingsEnabled && settingsVirtualizer > 0f)
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to apply virtualizer: ${t.message}")
            }
        }

        _state.value = State(
            available = eq != null && bands.isNotEmpty(),
            enabled = settingsEnabled,
            bands = bands,
            levelsDb = levels,
            minDb = minDb,
            maxDb = maxDb,
            preset = settingsPreset,
            bassBoost = settingsBass,
            bassBoostSupported = bassBoost != null,
            virtualizer = settingsVirtualizer,
            virtualizerSupported = virtualizer != null
        )
    }

    private fun releaseEffects() {
        try { equalizer?.release() } catch (_: Throwable) { }
        try { bassBoost?.release() } catch (_: Throwable) { }
        try { virtualizer?.release() } catch (_: Throwable) { }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}
