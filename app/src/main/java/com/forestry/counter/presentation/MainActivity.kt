package com.forestry.counter.presentation

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.forestry.counter.ForestryCounterApplication
import com.forestry.counter.presentation.navigation.ForestryNavigation
import com.forestry.counter.presentation.theme.ForestryCounterTheme
import com.forestry.counter.presentation.theme.parseAccentColor
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.view.WindowManager
import com.forestry.counter.data.preferences.FontSize

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()
        var keepOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepOnScreen }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // FLAG_SECURE : empêche captures d'écran et enregistrement vidéo (RGPD).
        // Levé uniquement sur les builds debug, sinon la revue visuelle du
        // design est impossible — `adb screencap` ne renvoie qu'une image noire.
        // Les builds release conservent la protection intégralement.
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebuggable) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        // Release splash after 500ms
        Handler(Looper.getMainLooper()).postDelayed({ keepOnScreen = false }, 500)

        val app = application as ForestryCounterApplication
        val prefsManager = app.userPreferences

        setContent {
            val themeMode by prefsManager.themeMode.collectAsStateWithLifecycle(initialValue = com.forestry.counter.data.preferences.ThemeMode.SYSTEM)
            val accentColorString by prefsManager.accentColor.collectAsStateWithLifecycle(initialValue = "#2D5F3F")
            val containerAccentColorString by prefsManager.containerAccentColor.collectAsStateWithLifecycle(initialValue = null)
            val cardAccentColorString by prefsManager.cardAccentColor.collectAsStateWithLifecycle(initialValue = null)
            val dynamicColorEnabled by prefsManager.dynamicColorEnabled.collectAsStateWithLifecycle(initialValue = false)
            val keepOn by prefsManager.keepScreenOn.collectAsStateWithLifecycle(initialValue = false)
            val fontSize by prefsManager.fontSize.collectAsStateWithLifecycle(initialValue = FontSize.MEDIUM)
            val accentColor = parseAccentColor(accentColorString)
            val containerAccentColor = containerAccentColorString?.let { parseAccentColor(it) }
            val cardAccentColor = cardAccentColorString?.let { parseAccentColor(it) }

            ForestryCounterTheme(
                themeMode = themeMode,
                accentColor = accentColor,
                containerAccentColor = containerAccentColor,
                cardAccentColor = cardAccentColor,
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
