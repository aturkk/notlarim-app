package com.applenotes.ai.core.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.frostedGlass(
    blurRadius: Dp = 20.dp,
    tintColor: Color? = null
): Modifier = composed {
    val isDark = isAppDarkTheme()
    val defaultTint = if (isDark) iOSBlurOverlayDark else iOSBlurOverlayLight
    val activeTint = tintColor ?: defaultTint

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this
            .blur(blurRadius)
            .background(activeTint)
    } else {
        this.background(activeTint)
    }
}
