package com.p2ptaskmanager.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.p2ptaskmanager.data.repository.PrefsRepository
import com.p2ptaskmanager.ui.AppRoot
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val prefs: PrefsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by prefs.observeThemeMode().collectAsState(initial = "system")
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> null
            }
            AppRoot(darkTheme = darkTheme)
        }
    }
}
