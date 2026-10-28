package org.coiltester.app.ui.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import org.coiltester.app.viewmodel.SettingsViewModel
import org.coiltester.core.model.ThemeMode
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = koinViewModel()

    @Suppress("FlowOperatorInvokedInComposition")
    val themeMode by settingsViewModel.state
        .map { it.settings.themeMode }
        .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

    val pureBlack = themeMode == ThemeMode.PURE_BLACK
    val darkTheme =
        when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK, ThemeMode.PURE_BLACK -> true
            ThemeMode.SYSTEM -> {
                val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }

    CoilTesterTheme(darkTheme = darkTheme, pureBlack = pureBlack, content = content)
}
