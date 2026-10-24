package com.easytv.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.easytv.launcher.ui.screens.HomeScreen
import com.easytv.launcher.ui.theme.EasyTVTheme
import com.easytv.launcher.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_PACKAGE_CHANGED = "com.easytv.launcher.ACTION_PACKAGE_CHANGED"
    }

    private val viewModel: MainViewModel by viewModels()

    private val internalPackageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.refreshApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register internal receiver to listen for app install/uninstall triggers
        val filter = IntentFilter(ACTION_PACKAGE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(internalPackageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(internalPackageReceiver, filter)
        }

        setContent {
            EasyTVTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh apps when returning from launched apps
        viewModel.refreshApps()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(internalPackageReceiver)
        } catch (_: Exception) {}
    }

    /**
     * Prevent back button from exiting the TV home screen.
     * Keeps user on the launcher safely.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Stay on home screen, do not exit
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
