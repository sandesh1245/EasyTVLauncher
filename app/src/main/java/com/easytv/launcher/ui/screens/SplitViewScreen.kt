package com.easytv.launcher.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
import com.easytv.launcher.model.AppItem
import com.easytv.launcher.ui.theme.*

data class LiveStreamChannel(
    val id: String,
    val title: String,
    val category: String,
    val iconEmoji: String,
    val videoUrl: String
)

val DEFAULT_LIVE_CHANNELS = listOf(
    LiveStreamChannel(
        id = "sports",
        title = "Sports & Action Live",
        category = "Sports",
        iconEmoji = "⚽",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    ),
    LiveStreamChannel(
        id = "nature",
        title = "Nature & Wildlife 4K",
        category = "Documentary",
        iconEmoji = "🌿",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    ),
    LiveStreamChannel(
        id = "space",
        title = "NASA & Space Cam Live",
        category = "Science",
        iconEmoji = "🚀",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    ),
    LiveStreamChannel(
        id = "news",
        title = "Global News & Finance",
        category = "News",
        iconEmoji = "🌍",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    ),
    LiveStreamChannel(
        id = "lofi",
        title = "Lo-Fi Beats & Music",
        category = "Music",
        iconEmoji = "🎵",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
    ),
    LiveStreamChannel(
        id = "gaming",
        title = "Gaming & Esports Live",
        category = "Gaming",
        iconEmoji = "🎮",
        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    )
)

@Composable
fun SplitViewScreen(
    installedApps: List<AppItem>,
    onLaunchApp: (AppItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // State for Left Stream & Right Stream
    var leftChannel by remember { mutableStateOf(DEFAULT_LIVE_CHANNELS[0]) }
    var rightChannel by remember { mutableStateOf(DEFAULT_LIVE_CHANNELS[1]) }
    var activeAudioSide by remember { mutableStateOf("LEFT") } // "LEFT" or "RIGHT"
    var fullscreenPane by remember { mutableStateOf<String?>(null) } // "LEFT" or "RIGHT" or null (50/50)
    var showChannelPickerForPane by remember { mutableStateOf<String?>(null) } // "LEFT" or "RIGHT" or null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🔲 Real Dual-Stream Player",
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
                            text = "Audio: ${if (activeAudioSide == "LEFT") "Left Stream 🔊" else "Right Stream 🔊"}",
                            color = HighVisibilityFocusGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    if (fullscreenPane != null) {
                        Surface(
                            color = Color(0xFF0F766E),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "FULLSCREEN MODE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Header Control Actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SplitControlButton(
                        title = "⇄ Swap Streams",
                        onClick = {
                            val temp = leftChannel
                            leftChannel = rightChannel
                            rightChannel = temp
                            activeAudioSide = if (activeAudioSide == "LEFT") "RIGHT" else "LEFT"
                            Toast.makeText(context, "Swapped Left and Right streams", Toast.LENGTH_SHORT).show()
                        }
                    )
                    SplitControlButton(
                        title = "🔊 Switch Audio",
                        onClick = {
                            activeAudioSide = if (activeAudioSide == "LEFT") "RIGHT" else "LEFT"
                            Toast.makeText(context, "Audio routed to ${if (activeAudioSide == "LEFT") "Left Stream" else "Right Stream"}", Toast.LENGTH_SHORT).show()
                        }
                    )
                    if (fullscreenPane != null) {
                        SplitControlButton(
                            title = "⛶ 50/50 Split",
                            onClick = { fullscreenPane = null }
                        )
                    }
                    SplitControlButton(
                        title = "✕ Exit",
                        onClick = onClose,
                        isDanger = true
                    )
                }
            }

            // Dual Panes Container (50/50 or Fullscreen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Window Stream
                if (fullscreenPane == null || fullscreenPane == "LEFT") {
                    RealStreamPane(
                        title = "Primary Stream",
                        channel = leftChannel,
                        hasAudio = activeAudioSide == "LEFT",
                        isFullscreen = fullscreenPane == "LEFT",
                        onChangeChannel = { showChannelPickerForPane = "LEFT" },
                        onToggleFullscreen = {
                            fullscreenPane = if (fullscreenPane == "LEFT") null else "LEFT"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Right Window Stream
                if (fullscreenPane == null || fullscreenPane == "RIGHT") {
                    RealStreamPane(
                        title = "Secondary Stream",
                        channel = rightChannel,
                        hasAudio = activeAudioSide == "RIGHT",
                        isFullscreen = fullscreenPane == "RIGHT",
                        onChangeChannel = { showChannelPickerForPane = "RIGHT" },
                        onToggleFullscreen = {
                            fullscreenPane = if (fullscreenPane == "RIGHT") null else "RIGHT"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live Channel Selection Dialog
        showChannelPickerForPane?.let { pane ->
            AlertDialog(
                onDismissRequest = { showChannelPickerForPane = null },
                title = {
                    Text(
                        text = "Select Stream for ${if (pane == "LEFT") "Left Window" else "Right Window"}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DEFAULT_LIVE_CHANNELS) { channel ->
                            Button(
                                onClick = {
                                    if (pane == "LEFT") leftChannel = channel else rightChannel = channel
                                    showChannelPickerForPane = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(text = channel.iconEmoji, fontSize = 24.sp)
                                    Column {
                                        Text(
                                            text = channel.title,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${channel.category} • Live Stream",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChannelPickerForPane = null }) {
                        Text("Cancel", color = DangerRed)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RealStreamPane(
    title: String,
    channel: LiveStreamChannel,
    hasAudio: Boolean,
    isFullscreen: Boolean,
    onChangeChannel: () -> Unit,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Update audio mute/unmute state in WebView when hasAudio changes
    LaunchedEffect(hasAudio, webViewRef) {
        val js = if (hasAudio) {
            "var v = document.getElementById('videoPlayer'); if (v) { v.muted = false; v.play(); }"
        } else {
            "var v = document.getElementById('videoPlayer'); if (v) { v.muted = true; }"
        }
        webViewRef?.evaluateJavascript(js, null)
    }

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
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Stream Pane Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${channel.iconEmoji} ${channel.title}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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

            // Live Video Playback Area using Hardware Accelerated WebView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.domStorageEnabled = true
                            setBackgroundColor(AndroidColor.BLACK)
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            
                            val html = buildVideoHtml(channel.videoUrl, hasAudio)
                            loadDataWithBaseURL("https://commondatastorage.googleapis.com", html, "text/html", "UTF-8", null)
                            webViewRef = this
                        }
                    },
                    update = { view ->
                        webViewRef = view
                        val html = buildVideoHtml(channel.videoUrl, hasAudio)
                        view.loadDataWithBaseURL("https://commondatastorage.googleapis.com", html, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Stream Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onChangeChannel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📺 Change Stream", fontSize = 12.sp)
                }
                Button(
                    onClick = onToggleFullscreen,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isFullscreen) "⛶ 50/50 Split" else "⛶ Fullscreen", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun buildVideoHtml(videoUrl: String, hasAudio: Boolean): String {
    val mutedAttr = if (hasAudio) "" else "muted"
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; background: #000; }
                html, body { width: 100%; height: 100%; overflow: hidden; display: flex; align-items: center; justify-content: center; }
                video { width: 100%; height: 100%; object-fit: contain; }
            </style>
        </head>
        <body>
            <video id="videoPlayer" src="$videoUrl" autoplay loop playsinline $mutedAttr></video>
            <script>
                var v = document.getElementById('videoPlayer');
                if (v) {
                    v.play().catch(function(e) {});
                }
            </script>
        </body>
        </html>
    """.trimIndent()
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
