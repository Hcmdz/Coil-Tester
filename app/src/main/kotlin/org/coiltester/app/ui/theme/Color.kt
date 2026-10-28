package org.coiltester.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
    androidx.compose.material3.lightColorScheme(
        primary = Color(0xFF0066CC),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD6E8FF),
        onPrimaryContainer = Color(0xFF001D3D),
        secondary = Color(0xFF4A6572),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD3E3F0),
        onSecondaryContainer = Color(0xFF041F2A),
        tertiary = Color(0xFF006874),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFD4F3FF),
        onTertiaryContainer = Color(0xFF001F24),
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFCFCFF),
        onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFFCFCFF),
        onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFDAE5EC),
        onSurfaceVariant = Color(0xFF3F494E),
        outline = Color(0xFF6F797E),
        outlineVariant = Color(0xFFBEC9CE),
    )

private val DarkColorScheme =
    androidx.compose.material3.darkColorScheme(
        primary = Color(0xFFA6D0FF),
        onPrimary = Color(0xFF003366),
        primaryContainer = Color(0xFF004B99),
        onPrimaryContainer = Color(0xFFD6E8FF),
        secondary = Color(0xFFB7C7D2),
        onSecondary = Color(0xFF1D333C),
        secondaryContainer = Color(0xFF344B56),
        onSecondaryContainer = Color(0xFFD3E3F0),
        tertiary = Color(0xFF9FD8E8),
        onTertiary = Color(0xFF00353C),
        tertiaryContainer = Color(0xFF004E58),
        onTertiaryContainer = Color(0xFFD4F3FF),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1A1C1E),
        onBackground = Color(0xFFE3E2E6),
        surface = Color(0xFF1A1C1E),
        onSurface = Color(0xFFE3E2E6),
        surfaceVariant = Color(0xFF3F494E),
        onSurfaceVariant = Color(0xFFBEC9CE),
        surfaceContainer = Color(0xFF1F2023),
        surfaceContainerLow = Color(0xFF1A1C1E),
        surfaceContainerHigh = Color(0xFF242628),
        surfaceContainerHighest = Color(0xFF2E3033),
        outline = Color(0xFF899399),
        outlineVariant = Color(0xFF3F494E),
    )

private val PureBlackColorScheme =
    androidx.compose.material3.darkColorScheme(
        primary = Color(0xFFFF6B6B),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF8B0000),
        onPrimaryContainer = Color(0xFFFFDAD6),
        secondary = Color(0xFFB7C7D2),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF1C1C1E),
        onSecondaryContainer = Color(0xFFD3E3F0),
        tertiary = Color(0xFF9FD8E8),
        onTertiary = Color(0xFF000000),
        tertiaryContainer = Color(0xFF00353C),
        onTertiaryContainer = Color(0xFFD4F3FF),
        error = Color(0xFFFF3B30),
        onError = Color(0xFF000000),
        errorContainer = Color(0xFF4A0000),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF000000),
        onBackground = Color(0xFFE3E2E6),
        surface = Color(0xFF000000),
        onSurface = Color(0xFFE3E2E6),
        surfaceVariant = Color(0xFF1C1C1E),
        onSurfaceVariant = Color(0xFFBEC9CE),
        surfaceContainer = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF000000),
        surfaceContainerHigh = Color(0xFF121212),
        surfaceContainerHighest = Color(0xFF1C1C1E),
        outline = Color(0xFF48484A),
        outlineVariant = Color(0xFF2C2C2E),
    )

@Composable
fun CoilTesterTheme(
    darkTheme: Boolean = false,
    pureBlack: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            pureBlack -> PureBlackColorScheme
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}

@Composable
fun DynamicCoilTesterTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val useDynamicColor = false
    val colorScheme =
        if (useDynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        } else if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
