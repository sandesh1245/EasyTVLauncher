package com.easytv.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.easytv.launcher.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(
        favoritePackages: Set<String>,
        hiddenPackages: Set<String>
    ): List<AppItem> = withContext(Dispatchers.IO) {
        val appMap = mutableMapOf<String, AppItem>()

        // 1. Query Leanback Launcher Apps (Primary for Android TV)
        val leanbackIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val leanbackResolveInfos = packageManager.queryIntentActivities(leanbackIntent, 0)
        for (info in leanbackResolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName) continue // exclude self
            val label = info.loadLabel(packageManager).toString()
            val icon = info.loadIcon(packageManager)
            val isFav = favoritePackages.contains(pkg)
            val isHidden = hiddenPackages.contains(pkg)
            appMap[pkg] = AppItem(
                packageName = pkg,
                activityName = info.activityInfo.name,
                label = label,
                icon = icon,
                isFavorite = isFav,
                isHidden = isHidden,
                isLeanbackApp = true
            )
        }

        // 2. Query Standard Launcher Apps (covers sideloaded & utility apps)
        val standardIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val standardResolveInfos = packageManager.queryIntentActivities(standardIntent, 0)
        for (info in standardResolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName) continue // exclude self
            if (!appMap.containsKey(pkg)) {
                val label = info.loadLabel(packageManager).toString()
                val icon = info.loadIcon(packageManager)
                val isFav = favoritePackages.contains(pkg)
                val isHidden = hiddenPackages.contains(pkg)
                appMap[pkg] = AppItem(
                    packageName = pkg,
                    activityName = info.activityInfo.name,
                    label = label,
                    icon = icon,
                    isFavorite = isFav,
                    isHidden = isHidden,
                    isLeanbackApp = false
                )
            }
        }

        appMap.values.sortedBy { it.label.lowercase() }
    }

    fun launchApp(app: AppItem): Boolean {
        return try {
            val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(app.packageName)
                ?: packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
