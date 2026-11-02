package com.aurel.music.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aurel_user_preferences")

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

interface UserPreferencesRepository {
    val themeMode: Flow<ThemeMode>
    val minTrackDurationSec: Flow<Int>
    val dynamicColorsEnabled: Flow<Boolean>
    val gaplessEnabled: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setMinTrackDurationSec(seconds: Int)
    suspend fun setDynamicColorsEnabled(enabled: Boolean)
    suspend fun setGaplessEnabled(enabled: Boolean)
}

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MIN_TRACK_DURATION_SEC = intPreferencesKey("min_track_duration_sec")
        val DYNAMIC_COLORS_ENABLED = booleanPreferencesKey("dynamic_colors_enabled")
        val GAPLESS_ENABLED = booleanPreferencesKey("gapless_enabled")
    }

    override val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    override val minTrackDurationSec: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MIN_TRACK_DURATION_SEC] ?: 15 // Default 15s per PRD Section 12
    }

    override val dynamicColorsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLORS_ENABLED] ?: true
    }

    override val gaplessEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GAPLESS_ENABLED] ?: true
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    override suspend fun setMinTrackDurationSec(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIN_TRACK_DURATION_SEC] = seconds
        }
    }

    override suspend fun setDynamicColorsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS_ENABLED] = enabled
        }
    }

    override suspend fun setGaplessEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GAPLESS_ENABLED] = enabled
        }
    }
}

