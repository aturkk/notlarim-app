package com.applenotes.ai.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode(val displayName: String) {
    LIGHT("Açık"),
    DARK("Koyu"),
    SYSTEM("Sistem")
}

enum class AppAccentColor(
    val displayName: String,
    val primary: androidx.compose.ui.graphics.Color,
    val lightContainer: androidx.compose.ui.graphics.Color,
    val darkContainer: androidx.compose.ui.graphics.Color
) {
    YELLOW("Apple Sarısı", AppleYellow, AppleYellowLight, AppleYellowDark),
    BLUE("Okyanus Mavisi", iOSBlue, androidx.compose.ui.graphics.Color(0xFF409CFF), androidx.compose.ui.graphics.Color(0xFF0056B3)),
    GREEN("Zümrüt Yeşili", iOSGreen, androidx.compose.ui.graphics.Color(0xFF5CD67A), androidx.compose.ui.graphics.Color(0xFF248A3D)),
    PURPLE("Ametist Moru", iOSPurple, androidx.compose.ui.graphics.Color(0xFFC379E6), androidx.compose.ui.graphics.Color(0xFF8933B2)),
    ORANGE("Gün Batımı Turuncusu", iOSOrange, androidx.compose.ui.graphics.Color(0xFFFFAA4D), androidx.compose.ui.graphics.Color(0xFFCC7700)),
    RED("Mercan Kırmızı", iOSRed, androidx.compose.ui.graphics.Color(0xFFFF6961), androidx.compose.ui.graphics.Color(0xFFD70015))
}

enum class AppFontFamily(
    val displayName: String,
    val fontFamily: androidx.compose.ui.text.font.FontFamily
) {
    SYSTEM("Sistem (Modern)", androidx.compose.ui.text.font.FontFamily.SansSerif),
    SERIF("Kitap & Edebiyat (Serif)", androidx.compose.ui.text.font.FontFamily.Serif),
    MONOSPACE("Daktilo & Kod (Monospace)", androidx.compose.ui.text.font.FontFamily.Monospace)
}

val LocalThemeIsDark = compositionLocalOf { false }
val LocalAccentColor = compositionLocalOf { AppleYellow }

@Composable
fun isAppDarkTheme(): Boolean = LocalThemeIsDark.current

@Composable
fun rememberAccentColor(): androidx.compose.ui.graphics.Color = LocalAccentColor.current

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
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    accentColor: AppAccentColor = AppAccentColor.YELLOW,
    fontFamily: AppFontFamily = AppFontFamily.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> systemDark
    }
    AppleNotesTheme(
        darkTheme = isDark,
        accentColor = accentColor,
        fontFamily = fontFamily,
        content = content
    )
}

@Composable
fun AppleNotesTheme(
    darkTheme: Boolean,
    accentColor: AppAccentColor = AppAccentColor.YELLOW,
    fontFamily: AppFontFamily = AppFontFamily.SYSTEM,
    content: @Composable () -> Unit
) {
    val activeAccent = accentColor.primary
    CompositionLocalProvider(
        LocalThemeIsDark provides darkTheme,
        LocalAccentColor provides activeAccent
    ) {
        val baseDark = DarkColorScheme.copy(
            primary = activeAccent,
            primaryContainer = accentColor.darkContainer
        )
        val baseLight = LightColorScheme.copy(
            primary = activeAccent,
            primaryContainer = accentColor.lightContainer
        )
        val colorScheme = if (darkTheme) baseDark else baseLight
        val typography = getAppleTypography(fontFamily.fontFamily)

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
            typography = typography,
            content = content
        )
    }
}
