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

enum class LauncherMode(val displayName: String, val emoji: String) {
    NORMAL("Normal", "📺"),
    SENIOR("Senior", "👴"),
    KIDS("Kids", "🧒"),
    PRIVATE("Private", "🔐")
}

class LauncherPreferences(private val context: Context) {

    companion object {
        val KEY_FAVORITES = stringSetPreferencesKey("favorites_set")
        val KEY_HIDDEN_APPS = stringSetPreferencesKey("hidden_apps_set")
        val KEY_LOCKED_APPS = stringSetPreferencesKey("locked_apps_set")
        val KEY_KIDS_APPS = stringSetPreferencesKey("kids_apps_set")
        val KEY_PRIVATE_APPS = stringSetPreferencesKey("private_apps_set")

        val KEY_PIN_CODE = stringPreferencesKey("caregiver_pin")
        val KEY_IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val KEY_TILE_SIZE = stringPreferencesKey("tile_size_mode")
        val KEY_LAUNCHER_MODE = stringPreferencesKey("launcher_mode")
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

    val lockedAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOCKED_APPS] ?: emptySet()
    }

    val kidsAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_KIDS_APPS] ?: setOf(
            "com.google.android.youtube.tvkids",
            "com.google.android.youtube.tv",
            "com.disney.disneyplus"
        )
    }

    val privateAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_PRIVATE_APPS] ?: emptySet()
    }

    val pinCodeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PIN_CODE] ?: "1234"
    }

    val isPinEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_PIN_ENABLED] ?: true
    }

    val launcherModeFlow: Flow<LauncherMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_LAUNCHER_MODE] ?: LauncherMode.NORMAL.name
        try {
            LauncherMode.valueOf(raw)
        } catch (_: Exception) {
            LauncherMode.NORMAL
        }
    }

    val tileSizeFlow: Flow<TileSizeMode> = context.dataStore.data.map { prefs ->
        val mode = prefs[KEY_LAUNCHER_MODE]
        if (mode == LauncherMode.SENIOR.name) {
            TileSizeMode.SENIOR_XL
        } else {
            val raw = prefs[KEY_TILE_SIZE] ?: TileSizeMode.STANDARD.name
            try {
                TileSizeMode.valueOf(raw)
            } catch (_: Exception) {
                TileSizeMode.STANDARD
            }
        }
    }

    val highContrastFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIGH_CONTRAST] ?: true
    }

    suspend fun setLauncherMode(mode: LauncherMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAUNCHER_MODE] = mode.name
            if (mode == LauncherMode.SENIOR) {
                prefs[KEY_TILE_SIZE] = TileSizeMode.SENIOR_XL.name
            }
        }
    }

    suspend fun toggleFavorite(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) current.remove(packageName) else current.add(packageName)
            prefs[KEY_FAVORITES] = current
        }
    }

    suspend fun toggleHideApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_HIDDEN_APPS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) current.remove(packageName) else current.add(packageName)
            prefs[KEY_HIDDEN_APPS] = current
        }
    }

    suspend fun toggleAppLock(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_LOCKED_APPS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) current.remove(packageName) else current.add(packageName)
            prefs[KEY_LOCKED_APPS] = current
        }
    }

    suspend fun toggleKidsApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_KIDS_APPS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) current.remove(packageName) else current.add(packageName)
            prefs[KEY_KIDS_APPS] = current
        }
    }

    suspend fun togglePrivateApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_PRIVATE_APPS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) current.remove(packageName) else current.add(packageName)
            prefs[KEY_PRIVATE_APPS] = current
        }
    }

    suspend fun setTileSize(mode: TileSizeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TILE_SIZE] = mode.name
        }
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PIN_CODE] = pin
            prefs[KEY_IS_PIN_ENABLED] = true
        }
    }
}
