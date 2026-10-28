package org.coiltester.app

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.coiltester.app.di.AppRoute
import org.coiltester.app.ui.navigation.AppNavHost
import org.coiltester.app.ui.theme.AppTheme
import org.coiltester.app.viewmodel.DetectionViewModel
import org.coiltester.app.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        // Read language from DataStore synchronously (blocking is OK here as it's small)
        val language = try {
            kotlinx.coroutines.runBlocking {
                val dataStore = org.koin.core.context.GlobalContext.get().get<androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>>()
                dataStore.data.first()[androidx.datastore.preferences.core.stringPreferencesKey("language")] ?: "fr"
            }
        } catch (e: Exception) {
            "fr"
        }
        val newLocale = java.util.Locale(language)
        val config = Configuration(context.resources.configuration)
        config.setLocale(newLocale)
        config.setLayoutDirection(newLocale)
        return context.createConfigurationContext(config)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val detectionViewModel: DetectionViewModel = koinViewModel()
                val settingsViewModel: SettingsViewModel = koinViewModel()
                AppNavHost(
                    navController = navController,
                    detectionViewModel = detectionViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}