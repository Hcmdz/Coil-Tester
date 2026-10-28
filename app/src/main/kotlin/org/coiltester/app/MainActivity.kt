package org.coiltester.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import org.coiltester.app.ui.navigation.AppNavHost
import org.coiltester.app.ui.theme.AppTheme
import org.coiltester.app.viewmodel.DetectionViewModel
import org.coiltester.app.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun updateBaseContextLocale(context: Context): Context {
        // Read language from DataStore synchronously (blocking is OK here as it's small)
        val language =
            try {
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
                    settingsViewModel = settingsViewModel,
                )
            }
        }
    }
}
