package com.easytv.launcher.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.easytv.launcher.data.AppRepository
import com.easytv.launcher.data.LauncherPreferences
import com.easytv.launcher.data.TileSizeMode
import com.easytv.launcher.model.AppItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val favoriteApps: List<AppItem> = emptyList(),
    val otherApps: List<AppItem> = emptyList(),
    val tileSize: TileSizeMode = TileSizeMode.SENIOR_XL,
    val isPinProtected: Boolean = false,
    val correctPin: String = "",
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
            combine(
                preferences.favoritesFlow,
                preferences.hiddenAppsFlow,
                preferences.tileSizeFlow,
                preferences.pinCodeFlow
            ) { favorites, hidden, tileSize, pin ->
                val allApps = repository.getInstalledApps(favorites, hidden)
                val visibleApps = allApps.filter { !it.isHidden }

                val favList = visibleApps.filter { it.isFavorite }
                val otherList = visibleApps.filter { !it.isFavorite }

                HomeUiState(
                    favoriteApps = favList,
                    otherApps = otherList,
                    tileSize = tileSize,
                    isPinProtected = !pin.isNullOrEmpty(),
                    correctPin = pin ?: "",
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
