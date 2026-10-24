package com.easytv.launcher.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.easytv.launcher.model.AppItem
import com.easytv.launcher.ui.theme.*

@Composable
fun SplitViewScreen(
    installedApps: List<AppItem>,
    onLaunchApp: (AppItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // State for Left Pane & Right Pane
    var leftApp by remember { mutableStateOf(installedApps.firstOrNull()) }
    var rightApp by remember { mutableStateOf(installedApps.getOrNull(1) ?: installedApps.firstOrNull()) }
    var activeAudioSide by remember { mutableStateOf("LEFT") } // "LEFT" or "RIGHT"
    var showAppPickerForPane by remember { mutableStateOf<String?>(null) } // "LEFT" or "RIGHT" or null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🔲 Dual-View Split Screen",
                        color = PrimaryAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = "Audio: ${if (activeAudioSide == "LEFT") "Left Pane 🔊" else "Right Pane 🔊"}",
                            color = HighVisibilityFocusGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Control action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SplitControlButton(
                        title = "⇄ Swap",
                        onClick = {
                            val temp = leftApp
                            leftApp = rightApp
                            rightApp = temp
                            activeAudioSide = if (activeAudioSide == "LEFT") "RIGHT" else "LEFT"
                            Toast.makeText(context, "Swapped Left and Right windows", Toast.LENGTH_SHORT).show()
                        }
                    )
                    SplitControlButton(
                        title = "🔊 Audio",
                        onClick = {
                            activeAudioSide = if (activeAudioSide == "LEFT") "RIGHT" else "LEFT"
                            Toast.makeText(context, "Switched audio to ${if (activeAudioSide == "LEFT") "Left" else "Right"}", Toast.LENGTH_SHORT).show()
                        }
                    )
                    SplitControlButton(
                        title = "✕ Exit",
                        onClick = onClose,
                        isDanger = true
                    )
                }
            }

            // Split 50 / 50 Dual Panes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Window Pane (50%)
                SplitPaneCard(
                    title = "Primary Window",
                    app = leftApp,
                    hasAudio = activeAudioSide == "LEFT",
                    onSelectApp = { showAppPickerForPane = "LEFT" },
                    onLaunchFull = { leftApp?.let(onLaunchApp) },
                    modifier = Modifier.weight(1f)
                )

                // Right Window Pane (50%)
                SplitPaneCard(
                    title = "Secondary Window",
                    app = rightApp,
                    hasAudio = activeAudioSide == "RIGHT",
                    onSelectApp = { showAppPickerForPane = "RIGHT" },
                    onLaunchFull = { rightApp?.let(onLaunchApp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // App Picker Dialog
        showAppPickerForPane?.let { pane ->
            AlertDialog(
                onDismissRequest = { showAppPickerForPane = null },
                title = { Text(text = "Select App for ${if (pane == "LEFT") "Left Window" else "Right Window"}", color = TextPrimary) },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(installedApps) { app ->
                            Button(
                                onClick = {
                                    if (pane == "LEFT") leftApp = app else rightApp = app
                                    showAppPickerForPane = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    app.icon?.let { icon ->
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(icon)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = app.displayTitle,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Text(
                                        text = app.displayTitle,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAppPickerForPane = null }) {
                        Text("Cancel", color = DangerRed)
                    }
                }
            )
        }
    }
}

@Composable
fun SplitPaneCard(
    title: String,
    app: AppItem?,
    hasAudio: Boolean,
    onSelectApp: () -> Unit,
    onLaunchFull: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(
                    width = if (isFocused) 3.dp else 1.dp,
                    color = if (isFocused) HighVisibilityFocusGlow else CardBorder
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        color = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Pane Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                if (hasAudio) {
                    Text(
                        text = "🔊 AUDIO ACTIVE",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "🔇 MUTED",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Central Simulated App Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (app != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        app.icon?.let { icon ->
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(icon)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = app.displayTitle,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Text(
                            text = app.displayTitle,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Running in Split Window (50%)",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = "Select an Application",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            // Pane Bottom Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSelectApp,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Text("Change App", fontSize = 12.sp)
                }
                Button(
                    onClick = onLaunchFull,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
                ) {
                    Text("⛶ Fullscreen", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SplitControlButton(
    title: String,
    onClick: () -> Unit,
    isDanger: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isDanger) Color(0xFF7F1D1D) else if (isFocused) HighVisibilityFocusGlow else Color(0xFF1E293B),
        border = BorderStroke(
            width = if (isFocused) 2.dp else 1.dp,
            color = if (isFocused) HighVisibilityFocusGlow else Color(0xFF334155)
        )
    ) {
        Text(
            text = title,
            color = if (isFocused && !isDanger) Color.Black else TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
