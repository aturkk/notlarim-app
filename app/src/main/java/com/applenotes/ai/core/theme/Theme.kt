package com.applenotes.ai.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppleYellow,
    onPrimary = iOSBackgroundDark,
    primaryContainer = AppleYellowDark,
    onPrimaryContainer = iOSTextPrimaryDark,
    background = iOSBackgroundDark,
    onBackground = iOSTextPrimaryDark,
    surface = iOSCardBackgroundDark,
    onSurface = iOSTextPrimaryDark,
    surfaceVariant = iOSSecondaryBackgroundDark,
    onSurfaceVariant = iOSTextSecondaryDark,
    outline = iOSSeparatorDark
)

private val LightColorScheme = lightColorScheme(
    primary = AppleYellow,
    onPrimary = iOSCardBackgroundLight,
    primaryContainer = AppleYellowLight,
    onPrimaryContainer = iOSTextPrimaryLight,
    background = iOSBackgroundLight,
    onBackground = iOSTextPrimaryLight,
    surface = iOSCardBackgroundLight,
    onSurface = iOSTextPrimaryLight,
    surfaceVariant = iOSSecondaryBackgroundLight,
    onSurfaceVariant = iOSTextSecondaryLight,
    outline = iOSSeparatorLight
)

@Composable
fun AppleNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppleTypography,
        content = content
    )
}
