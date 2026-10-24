package com.easytv.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.easytv.launcher.data.TileSizeMode
import com.easytv.launcher.model.AppItem
import com.easytv.launcher.ui.theme.CardBackground
import com.easytv.launcher.ui.theme.CardBorder
import com.easytv.launcher.ui.theme.HighVisibilityFocusGlow
import com.easytv.launcher.ui.theme.TextPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    app: AppItem,
    tileSizeMode: TileSizeMode,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "tile_scale"
    )

    val isSenior = tileSizeMode == TileSizeMode.SENIOR_XL
    val cardWidth = if (isSenior) 220.dp else 170.dp
    val cardHeight = if (isSenior) 160.dp else 130.dp
    val iconSize = if (isSenior) 64.dp else 52.dp
    val textSize = if (isSenior) 18.sp else 15.sp

    Card(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) CardBackground.copy(alpha = 0.95f) else CardBackground
        ),
        border = BorderStroke(
            width = if (isFocused) 3.5.dp else 1.dp,
            color = if (isFocused) HighVisibilityFocusGlow else CardBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 12.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(app.icon)
                    .crossfade(true)
                    .build(),
                contentDescription = app.displayTitle,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // App Label
            Text(
                text = app.displayTitle,
                color = if (isFocused) HighVisibilityFocusGlow else TextPrimary,
                fontSize = textSize,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
