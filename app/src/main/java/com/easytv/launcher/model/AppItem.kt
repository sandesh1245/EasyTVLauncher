package com.easytv.launcher.model

import android.graphics.drawable.Drawable

/**
 * Represents an installed Android application displayed on the TV launcher.
 */
data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val customLabel: String? = null,
    val icon: Drawable? = null,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isLeanbackApp: Boolean = false
) {
    val displayTitle: String
        get() = customLabel?.takeIf { it.isNotBlank() } ?: label
}

/**
 * Quick action items for TV hardware control, sleep timers, and inputs.
 */
data class QuickActionItem(
    val id: String,
    val title: String,
    val iconResId: Int? = null,
    val actionType: ActionType
)

enum class ActionType {
    HDMI_1,
    HDMI_2,
    SLEEP_TIMER,
    SETTINGS
}
