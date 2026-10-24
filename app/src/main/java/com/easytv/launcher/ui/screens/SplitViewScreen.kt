package com.easytv.launcher.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
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

const val MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

data class LiveStreamChannel(
    val id: String,
    val title: String,
    val category: String,
    val iconEmoji: String,
    val url: String,
    val isWebMode: Boolean = false
)

val GOOGLE_SEARCH_MOBILE = LiveStreamChannel(
    id = "google_search",
    title = "Google Search (Mobile Web)",
    category = "Mobile Web",
    iconEmoji = "🌐",
    url = "https://www.google.com",
    isWebMode = true
)

val DEFAULT_LIVE_CHANNELS = listOf(
    LiveStreamChannel(
        id = "sports",
        title = "Sports & Action Live",
        category = "Sports",
        iconEmoji = "⚽",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    ),
    GOOGLE_SEARCH_MOBILE,
    LiveStreamChannel(
        id = "nature",
        title = "Nature & Wildlife 4K",
        category = "Documentary",
        iconEmoji = "🌿",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    ),
    LiveStreamChannel(
        id = "space",
        title = "NASA & Space Cam Live",
        category = "Science",
        iconEmoji = "🚀",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    ),
    LiveStreamChannel(
        id = "news",
        title = "Global News & Finance",
        category = "News",
        iconEmoji = "🌍",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    ),
    LiveStreamChannel(
        id = "lofi",
        title = "Lo-Fi Beats & Music",
        category = "Music",
        iconEmoji = "🎵",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
    ),
    LiveStreamChannel(
        id = "gaming",
        title = "Gaming & Esports Live",
        category = "Gaming",
        iconEmoji = "🎮",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
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

    // State for Left Stream & Right Stream (Left: Video, Right: Google Search in Mobile Mode)
    var leftChannel by remember { mutableStateOf(DEFAULT_LIVE_CHANNELS[0]) }
    var rightChannel by remember { mutableStateOf(GOOGLE_SEARCH_MOBILE) }
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
                        title = "⇄ Swap",
                        onClick = {
                            val temp = leftChannel
                            leftChannel = rightChannel
                            rightChannel = temp
                            activeAudioSide = if (activeAudioSide == "LEFT") "RIGHT" else "LEFT"
                            Toast.makeText(context, "Swapped Left and Right panes", Toast.LENGTH_SHORT).show()
                        }
                    )
                    SplitControlButton(
                        title = if (rightChannel.isWebMode) "📺 Right: Video" else "🌐 Right: Google Mobile",
                        onClick = {
                            rightChannel = if (rightChannel.isWebMode) DEFAULT_LIVE_CHANNELS[2] else GOOGLE_SEARCH_MOBILE
                            Toast.makeText(context, if (rightChannel.isWebMode) "Loaded Google Search (Mobile View)" else "Switched to Nature 4K Video", Toast.LENGTH_SHORT).show()
                        }
                    )
                    SplitControlButton(
                        title = "🔊 Audio",
                        onClick = {
                            activeAudioSide = if (activeAudioSide == "LEFT") "RIGHT" else "LEFT"
                            Toast.makeText(context, "Audio routed to ${if (activeAudioSide == "LEFT") "Left" else "Right"} pane", Toast.LENGTH_SHORT).show()
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
                        title = "Left Window",
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
                        title = "Right Window",
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
                                            text = "${channel.category} • ${if (channel.isWebMode) "Mobile Webview" else "Live Video"}",
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
                    if (channel.isWebMode) {
                        Surface(
                            color = Color(0xFF0284C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "📱 MOBILE WEB",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (!channel.isWebMode) {
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
            }

            // If in Web Mode, display mobile URL bar and TV quick search shortcuts
            if (channel.isWebMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = "🔒 google.com (Pixel Mobile Mode)",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Button(
                        onClick = { webViewRef?.loadUrl("https://www.google.com") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("🏠 Home", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { webViewRef?.loadUrl("https://www.google.com/search?q=live+sports+scores") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("⚽ Scores", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { webViewRef?.loadUrl("https://www.google.com/search?q=breaking+news") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("📰 News", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { webViewRef?.reload() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("⟳", fontSize = 11.sp)
                    }
                }
            }

            // Live Content Playback Area using Hardware Accelerated WebView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (channel.isWebMode) Color.White else Color.Black),
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
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false

                            if (channel.isWebMode) {
                                settings.userAgentString = MOBILE_USER_AGENT
                                setBackgroundColor(AndroidColor.WHITE)
                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        return false
                                    }
                                }
                                loadUrl(channel.url)
                            } else {
                                settings.mediaPlaybackRequiresUserGesture = false
                                setBackgroundColor(AndroidColor.BLACK)
                                webChromeClient = WebChromeClient()
                                webViewClient = WebViewClient()
                                val html = buildVideoHtml(channel.url, hasAudio)
                                loadDataWithBaseURL("https://commondatastorage.googleapis.com", html, "text/html", "UTF-8", null)
                            }
                            webViewRef = this
                        }
                    },
                    update = { view ->
                        webViewRef = view
                        if (channel.isWebMode) {
                            view.settings.userAgentString = MOBILE_USER_AGENT
                            view.setBackgroundColor(AndroidColor.WHITE)
                            if (view.url == null || (!view.url!!.contains("google") && channel.url.contains("google"))) {
                                view.loadUrl(channel.url)
                            }
                        } else {
                            view.settings.mediaPlaybackRequiresUserGesture = false
                            view.setBackgroundColor(AndroidColor.BLACK)
                            val html = buildVideoHtml(channel.url, hasAudio)
                            view.loadDataWithBaseURL("https://commondatastorage.googleapis.com", html, "text/html", "UTF-8", null)
                        }
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
                    Text(if (channel.isWebMode) "🔄 Change Mode" else "📺 Change Stream", fontSize = 12.sp)
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
