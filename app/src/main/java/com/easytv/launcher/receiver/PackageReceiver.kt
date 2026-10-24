package com.easytv.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.easytv.launcher.MainActivity

class PackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_PACKAGE_ADDED ||
            action == Intent.ACTION_PACKAGE_REMOVED ||
            action == Intent.ACTION_PACKAGE_REPLACED
        ) {
            // Re-trigger launcher app list refresh if active
            val refreshIntent = Intent(MainActivity.ACTION_PACKAGE_CHANGED)
            context?.sendBroadcast(refreshIntent)
        }
    }
}
