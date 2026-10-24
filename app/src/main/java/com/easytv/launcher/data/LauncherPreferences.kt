package com.easytv.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "easy_tv_prefs")

enum class TileSizeMode {
    STANDARD,
    SENIOR_XL
}

class LauncherPreferences(private val context: Context) {

    companion object {
        val KEY_FAVORITES = stringSetPreferencesKey("favorites_set")
        val KEY_HIDDEN_APPS = stringSetPreferencesKey("hidden_apps_set")
        val KEY_PIN_CODE = stringPreferencesKey("caregiver_pin")
        val KEY_IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val KEY_TILE_SIZE = stringPreferencesKey("tile_size_mode")
        val KEY_HIGH_CONTRAST = booleanPreferencesKey("high_contrast_mode")
    }

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITES] ?: setOf(
            "com.google.android.youtube.tv",
            "com.netflix.ninja",
            "com.amazon.amazonvideo.livingroom"
        )
    }

    val hiddenAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIDDEN_APPS] ?: emptySet()
    }

    val pinCodeFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        if (prefs[KEY_IS_PIN_ENABLED] == true) prefs[KEY_PIN_CODE] else null
    }

    val tileSizeFlow: Flow<TileSizeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_TILE_SIZE] ?: TileSizeMode.SENIOR_XL.name
        try {
            TileSizeMode.valueOf(raw)
        } catch (_: Exception) {
            TileSizeMode.SENIOR_XL
        }
    }

    val highContrastFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIGH_CONTRAST] ?: true
    }

    suspend fun toggleFavorite(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[KEY_FAVORITES] = current
        }
    }

    suspend fun toggleHideApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_HIDDEN_APPS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[KEY_HIDDEN_APPS] = current
        }
    }

    suspend fun setTileSize(mode: TileSizeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TILE_SIZE] = mode.name
        }
    }

    suspend fun setPin(pin: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PIN_CODE] = pin
            prefs[KEY_IS_PIN_ENABLED] = enabled
        }
    }
}
