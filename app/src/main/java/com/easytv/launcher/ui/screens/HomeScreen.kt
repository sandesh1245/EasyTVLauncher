package com.easytv.launcher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

            // 2. Favorite Apps Row (if any)
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
                                onClick = { viewModel.launchApp(app) },
                                onLongClick = { selectedAppForMenu = app }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            // 3. All Apps Row / Section
            item {
                SectionHeader(title = "All Applications")
            }
            item {
                if (uiState.otherApps.isEmpty() && uiState.favoriteApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = if (uiState.isLoading) "Loading applications..." else "No applications found.",
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
                                onClick = { viewModel.launchApp(app) },
                                onLongClick = { selectedAppForMenu = app }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // 4. Quick TV Controls & Inputs
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

        // App Options Dialog (on long-press)
        selectedAppForMenu?.let { app ->
            AlertDialog(
                onDismissRequest = { selectedAppForMenu = null },
                title = { Text(text = app.displayTitle, color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                viewModel.toggleFavorite(app)
                                selectedAppForMenu = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
                        ) {
                            Text(if (app.isFavorite) "Remove from Favorites" else "Add to Favorites")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.toggleHideApp(app)
                                selectedAppForMenu = null
                            }
                        ) {
                            Text("Hide App from Home")
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

        // PIN Verification Dialog for Settings/Restricted actions
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
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
    )
}
