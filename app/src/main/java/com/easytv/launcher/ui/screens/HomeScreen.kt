package com.easytv.launcher.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easytv.launcher.data.LauncherMode
import com.easytv.launcher.data.TileSizeMode
import com.easytv.launcher.model.AppItem
import com.easytv.launcher.ui.components.*
import com.easytv.launcher.ui.theme.*
import com.easytv.launcher.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedAppForMenu by remember { mutableStateOf<AppItem?>(null) }
    var showPinDialogForAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun handleAppClick(app: AppItem) {
        if (app.isLocked) {
            // App is locked by App Locker -> require PIN
            showPinDialogForAction = { viewModel.launchApp(app) }
        } else {
            viewModel.launchApp(app)
        }
    }

    fun handleModeSwitch(targetMode: LauncherMode) {
        if (targetMode == uiState.currentMode) return

        // Switching to Private Mode or leaving Kids Mode requires PIN verification
        if (targetMode == LauncherMode.PRIVATE || uiState.currentMode == LauncherMode.KIDS) {
            showPinDialogForAction = {
                viewModel.setMode(targetMode)
                Toast.makeText(context, "Mode switched to ${targetMode.displayName}", Toast.LENGTH_SHORT).show()
            }
        } else {
            viewModel.setMode(targetMode)
            Toast.makeText(context, "Mode switched to ${targetMode.displayName}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // 1. Status Bar Header
            item {
                TopStatusBar()
            }

            // 2. Launcher Profiles & App Locker Mode Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mode:",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    LauncherMode.values().forEach { mode ->
                        val isSelected = uiState.currentMode == mode
                        ModeChip(
                            mode = mode,
                            isSelected = isSelected,
                            onClick = { handleModeSwitch(mode) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. Favorite Apps Row (if any)
            if (uiState.favoriteApps.isNotEmpty()) {
                item {
                    SectionHeader(title = "★ Favorites")
                }
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(uiState.favoriteApps, key = { it.packageName }) { app ->
                            AppCard(
                                app = app,
                                tileSizeMode = uiState.tileSize,
                                onClick = { handleAppClick(app) },
                                onLongClick = { selectedAppForMenu = app }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            // 4. All Apps Row / Section
            item {
                val sectionTitle = when (uiState.currentMode) {
                    LauncherMode.KIDS -> "🧒 Kids Safe Applications"
                    LauncherMode.PRIVATE -> "🔐 Private Mode (All Apps Visible)"
                    LauncherMode.SENIOR -> "👴 Senior Large Grid"
                    LauncherMode.NORMAL -> "All Applications"
                }
                SectionHeader(title = sectionTitle)
            }
            item {
                if (uiState.otherApps.isEmpty() && uiState.favoriteApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = if (uiState.isLoading) "Loading applications..."
                            else if (uiState.currentMode == LauncherMode.KIDS) "No kids apps assigned. Long-press any app in Normal mode to allow."
                            else "No applications found.",
                            color = TextSecondary,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(uiState.otherApps, key = { it.packageName }) { app ->
                            AppCard(
                                app = app,
                                tileSizeMode = uiState.tileSize,
                                onClick = { handleAppClick(app) },
                                onLongClick = { selectedAppForMenu = app }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // 5. Quick TV Controls & Inputs (Hidden in Kids Mode for Safety)
            if (uiState.currentMode != LauncherMode.KIDS) {
                item {
                    SectionHeader(title = "Quick Controls")
                }
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ActionCard(
                                title = "HDMI 1",
                                icon = Icons.Default.Tv,
                                onClick = {
                                    Toast.makeText(context, "Switching to HDMI 1...", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        item {
                            ActionCard(
                                title = "HDMI 2",
                                icon = Icons.Default.Tv,
                                onClick = {
                                    Toast.makeText(context, "Switching to HDMI 2...", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        item {
                            ActionCard(
                                title = if (uiState.tileSize == TileSizeMode.SENIOR_XL) "Size: XL" else "Size: Standard",
                                icon = Icons.Default.ZoomIn,
                                onClick = { viewModel.switchTileSize() }
                            )
                        }
                        item {
                            ActionCard(
                                title = "Settings",
                                icon = Icons.Default.Settings,
                                onClick = {
                                    if (uiState.isPinProtected) {
                                        showPinDialogForAction = { viewModel.openAndroidSettings() }
                                    } else {
                                        viewModel.openAndroidSettings()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // App Options Dialog (on long-press) with App Locker controls
        selectedAppForMenu?.let { app ->
            val isKidsApp = uiState.kidsPackages.contains(app.packageName)
            val isPrivateApp = uiState.privatePackages.contains(app.packageName)

            AlertDialog(
                onDismissRequest = { selectedAppForMenu = null },
                title = { Text(text = "${app.displayTitle} Options", color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Favorite toggle
                        Button(
                            onClick = {
                                viewModel.toggleFavorite(app)
                                selectedAppForMenu = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (app.isFavorite) "★ Remove from Favorites" else "★ Add to Favorites")
                        }

                        // App Locker toggle (Require PIN to launch)
                        OutlinedButton(
                            onClick = {
                                showPinDialogForAction = {
                                    viewModel.toggleAppLock(app)
                                    selectedAppForMenu = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (app.isLocked) "🔓 Unlock App (Remove PIN)" else "🔒 Lock App (Require PIN)")
                        }

                        // Kids Mode allow/disallow toggle
                        OutlinedButton(
                            onClick = {
                                viewModel.toggleKidsApp(app)
                                selectedAppForMenu = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isKidsApp) "🧒 Remove from Kids Mode" else "🧒 Allow in Kids Mode")
                        }

                        // Private App toggle
                        OutlinedButton(
                            onClick = {
                                showPinDialogForAction = {
                                    viewModel.togglePrivateApp(app)
                                    selectedAppForMenu = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isPrivateApp) "👁️ Make Public (Visible in Normal)" else "🔐 Mark as Private (Hide in Normal)")
                        }

                        // Hide App toggle
                        OutlinedButton(
                            onClick = {
                                viewModel.toggleHideApp(app)
                                selectedAppForMenu = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🚫 Hide from Launcher")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedAppForMenu = null }) {
                        Text("Close", color = TextSecondary)
                    }
                },
                containerColor = CardBackground
            )
        }

        // PIN Verification Dialog for Settings, Locked Apps & Mode Switches
        showPinDialogForAction?.let { action ->
            PinLockDialog(
                correctPin = uiState.correctPin,
                onSuccess = {
                    showPinDialogForAction = null
                    action()
                },
                onDismiss = { showPinDialogForAction = null }
            )
        }
    }
}

@Composable
private fun ModeChip(
    mode: LauncherMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(120),
        label = "chip_scale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryAccent.copy(alpha = 0.35f) else CardBackground,
        border = BorderStroke(
            width = if (isFocused) 2.5.dp else if (isSelected) 1.5.dp else 1.dp,
            color = if (isFocused) HighVisibilityFocusGlow else if (isSelected) PrimaryAccent else CardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = mode.emoji, fontSize = 14.sp)
            Text(
                text = mode.displayName,
                color = if (isFocused) HighVisibilityFocusGlow else if (isSelected) TextPrimary else TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
    )
}
