package com.forestry.counter.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.forestry.counter.ForestryCounterApplication
import com.forestry.counter.presentation.navigation.ForestryNavigation
import com.forestry.counter.presentation.theme.ForestryCounterTheme
import com.forestry.counter.presentation.theme.parseAccentColor
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.view.WindowManager
import com.forestry.counter.data.preferences.FontSize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()
        var keepOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepOnScreen }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Release splash after 500ms
        Handler(Looper.getMainLooper()).postDelayed({ keepOnScreen = false }, 500)

        val app = application as ForestryCounterApplication
        val prefsManager = app.userPreferences

        setContent {
            val themeMode by prefsManager.themeMode.collectAsState(initial = com.forestry.counter.data.preferences.ThemeMode.SYSTEM)
            val accentColorString by prefsManager.accentColor.collectAsState(initial = "#4CAF50")
            val dynamicColorEnabled by prefsManager.dynamicColorEnabled.collectAsState(initial = true)
            val keepOn by prefsManager.keepScreenOn.collectAsState(initial = false)
            val fontSize by prefsManager.fontSize.collectAsState(initial = FontSize.MEDIUM)
            val accentColor = parseAccentColor(accentColorString)

            ForestryCounterTheme(
                themeMode = themeMode,
                accentColor = accentColor,
                dynamicColor = dynamicColorEnabled,
                fontSize = fontSize
            ) {
                DisposableEffect(keepOn) {
                    if (keepOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    onDispose { }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ForestryNavigation(app)
                }
            }
        }
    }
}
