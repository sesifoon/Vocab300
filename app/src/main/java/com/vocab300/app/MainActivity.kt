package com.vocab300.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.vocab300.app.data.datastore.DarkModeOption
import com.vocab300.app.ui.navigation.Vocab300NavGraph
import com.vocab300.app.ui.theme.Vocab300Theme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as VocabApp

        setContent {
            val darkModeOptionFlow = remember(app.userPrefsManager) {
                app.userPrefsManager.userStats.map { it.darkMode }
            }
            val darkModeOption by darkModeOptionFlow.collectAsState(initial = DarkModeOption.SYSTEM)

            val useDarkTheme = when (darkModeOption) {
                DarkModeOption.LIGHT -> false
                DarkModeOption.DARK -> true
                DarkModeOption.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            Vocab300Theme(darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Vocab300NavGraph(app = app)
                }
            }
        }
    }
}
