package com.easytv.launcher.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.easytv.launcher.data.AppRepository
import com.easytv.launcher.data.LauncherMode
import com.easytv.launcher.data.LauncherPreferences
import com.easytv.launcher.data.TileSizeMode
import com.easytv.launcher.model.AppItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private data class AppFilterData(
    val favorites: Set<String>,
    val hidden: Set<String>,
    val locked: Set<String>,
    val kids: Set<String>,
    val privateApps: Set<String>
)

private data class DisplaySettingsData(
    val mode: LauncherMode,
    val tileSize: TileSizeMode,
    val pin: String
)

data class HomeUiState(
    val favoriteApps: List<AppItem> = emptyList(),
    val otherApps: List<AppItem> = emptyList(),
    val currentMode: LauncherMode = LauncherMode.NORMAL,
    val tileSize: TileSizeMode = TileSizeMode.STANDARD,
    val isPinProtected: Boolean = true,
    val correctPin: String = "1234",
    val lockedPackages: Set<String> = emptySet(),
    val kidsPackages: Set<String> = emptySet(),
    val privatePackages: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = LauncherPreferences(application)
    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeLauncherData()
    }

    private fun observeLauncherData() {
        viewModelScope.launch {
            val appFiltersFlow = combine(
                preferences.favoritesFlow,
                preferences.hiddenAppsFlow,
                preferences.lockedAppsFlow,
                preferences.kidsAppsFlow,
                preferences.privateAppsFlow
            ) { favorites, hidden, locked, kids, privateApps ->
                AppFilterData(favorites, hidden, locked, kids, privateApps)
            }

            val displaySettingsFlow = combine(
                preferences.launcherModeFlow,
                preferences.tileSizeFlow,
                preferences.pinCodeFlow
            ) { mode, tileSize, pin ->
                DisplaySettingsData(mode, tileSize, pin)
            }

            combine(appFiltersFlow, displaySettingsFlow) { filters, settings ->
                val allApps = repository.getInstalledApps(
                    favoritePackages = filters.favorites,
                    hiddenPackages = filters.hidden,
                    lockedPackages = filters.locked
                )

                // Filter visible apps based on the active Mode
                val modeFilteredApps = when (settings.mode) {
                    LauncherMode.KIDS -> {
                        // Kids Mode: Show ONLY parent-approved kids apps
                        allApps.filter { filters.kids.contains(it.packageName) }
                    }
                    LauncherMode.PRIVATE -> {
                        // Private Mode: Show all apps including private apps
                        allApps.filter { !it.isHidden }
                    }
                    LauncherMode.NORMAL, LauncherMode.SENIOR -> {
                        // Normal/Senior Mode: Hide private apps and hidden apps
                        allApps.filter { !it.isHidden && !filters.privateApps.contains(it.packageName) }
                    }
                }

                val favList = modeFilteredApps.filter { it.isFavorite }
                val otherList = modeFilteredApps.filter { !it.isFavorite }

                HomeUiState(
                    favoriteApps = favList,
                    otherApps = otherList,
                    currentMode = settings.mode,
                    tileSize = if (settings.mode == LauncherMode.SENIOR) TileSizeMode.SENIOR_XL else settings.tileSize,
                    isPinProtected = true,
                    correctPin = settings.pin,
                    lockedPackages = filters.locked,
                    kidsPackages = filters.kids,
                    privatePackages = filters.privateApps,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun launchApp(app: AppItem) {
        repository.launchApp(app)
    }

    fun setMode(mode: LauncherMode) {
        viewModelScope.launch {
            preferences.setLauncherMode(mode)
        }
    }

    fun toggleFavorite(app: AppItem) {
        viewModelScope.launch {
            preferences.toggleFavorite(app.packageName)
        }
    }

    fun toggleHideApp(app: AppItem) {
        viewModelScope.launch {
            preferences.toggleHideApp(app.packageName)
        }
    }

    fun toggleAppLock(app: AppItem) {
        viewModelScope.launch {
            preferences.toggleAppLock(app.packageName)
        }
    }

    fun toggleKidsApp(app: AppItem) {
        viewModelScope.launch {
            preferences.toggleKidsApp(app.packageName)
        }
    }

    fun togglePrivateApp(app: AppItem) {
        viewModelScope.launch {
            preferences.togglePrivateApp(app.packageName)
        }
    }

    fun switchTileSize() {
        viewModelScope.launch {
            val nextSize = if (_uiState.value.tileSize == TileSizeMode.SENIOR_XL) {
                TileSizeMode.STANDARD
            } else {
                TileSizeMode.SENIOR_XL
            }
            preferences.setTileSize(nextSize)
        }
    }

    fun openAndroidSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {}
    }

    fun refreshApps() {
        observeLauncherData()
    }
}
