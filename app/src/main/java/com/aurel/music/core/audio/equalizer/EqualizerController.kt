package com.aurel.music.core.audio.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val minLevelMilliBel: Short,
    val maxLevelMilliBel: Short,
    val currentLevelMilliBel: Short
)

@Immutable
data class EqualizerUiState(
    val isAvailable: Boolean = false,
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val currentPreset: String = "Flat",
    val availablePresets: List<String> = emptyList(),
    val bassBoostStrength: Short = 0,
    val isBassBoostSupported: Boolean = false
)

@Singleton
class EqualizerController @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    fun bindAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return

        try {
            release()
            val eq = Equalizer(0, audioSessionId)
            equalizer = eq

            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]
            val numberOfBands = eq.numberOfBands

            val bandList = mutableListOf<EqualizerBand>()
            for (i in 0 until numberOfBands) {
                val bandIndex = i.toShort()
                val freq = eq.getCenterFreq(bandIndex) / 1000 // Convert mHz to Hz
                val level = eq.getBandLevel(bandIndex)
                bandList.add(
                    EqualizerBand(
                        index = bandIndex,
                        centerFreqHz = freq,
                        minLevelMilliBel = minLevel,
                        maxLevelMilliBel = maxLevel,
                        currentLevelMilliBel = level
                    )
                )
            }

            val presets = mutableListOf<String>()
            for (p in 0 until eq.numberOfPresets) {
                presets.add(eq.getPresetName(p.toShort()))
            }
            presets.add("Custom")

            // Attempt BassBoost initialization
            var hasBass = false
            try {
                val bb = BassBoost(0, audioSessionId)
                if (bb.strengthSupported) {
                    bassBoost = bb
                    hasBass = true
                }
            } catch (_: Exception) {
                bassBoost = null
            }

            _uiState.value = EqualizerUiState(
                isAvailable = true,
                isEnabled = eq.enabled,
                bands = bandList,
                currentPreset = "Flat",
                availablePresets = presets,
                isBassBoostSupported = hasBass
            )
        } catch (_: Exception) {
            // Audio effect unsupported or conflict with OEM DSP
            _uiState.value = EqualizerUiState(isAvailable = false)
        }
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        _uiState.value = _uiState.value.copy(isEnabled = enabled)
    }

    fun setBandLevel(bandIndex: Short, levelMilliBel: Short) {
        equalizer?.setBandLevel(bandIndex, levelMilliBel)
        val updatedBands = _uiState.value.bands.map {
            if (it.index == bandIndex) it.copy(currentLevelMilliBel = levelMilliBel) else it
        }
        _uiState.value = _uiState.value.copy(bands = updatedBands, currentPreset = "Custom")
    }

    fun setPreset(presetName: String) {
        val eq = equalizer ?: return
        val presetIndex = _uiState.value.availablePresets.indexOf(presetName)
        if (presetIndex >= 0 && presetIndex < eq.numberOfPresets) {
            eq.usePreset(presetIndex.toShort())
            val updatedBands = _uiState.value.bands.map {
                it.copy(currentLevelMilliBel = eq.getBandLevel(it.index))
            }
            _uiState.value = _uiState.value.copy(bands = updatedBands, currentPreset = presetName)
        }
    }

    fun setBassBoost(strength: Short) {
        bassBoost?.setStrength(strength)
        _uiState.value = _uiState.value.copy(bassBoostStrength = strength)
    }

    fun release() {
        equalizer?.release()
        equalizer = null
        bassBoost?.release()
        bassBoost = null
        _uiState.value = _uiState.value.copy(isAvailable = false)
    }
}

