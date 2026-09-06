package com.applenotes.ai.core.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class HapticFeedbackHelper(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Subtle, crisp mechanical tick (Apple Taptic Engine click).
     * Ideal for checklist checkmarks, toggles, icon selections, slider movements.
     */
    fun tick() {
        try {
            if (vibrator?.hasVibrator() != true) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(10L, 70))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10L)
            }
        } catch (e: Exception) {
            // Gracefully ignore devices without vibration support or disabled in system settings
        }
    }

    /**
     * Confirmatory double tick.
     * Ideal for completed AI tasks, successful cloud sync, scheduled reminder.
     */
    fun success() {
        try {
            if (vibrator?.hasVibrator() != true) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 12, 60, 16)
                val amplitudes = intArrayOf(0, 80, 0, 110)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 15, 60, 20), -1)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Firm, tactile warning bump.
     * Ideal for swipe threshold reached, deletion confirmation, trash empty.
     */
    fun warning() {
        try {
            if (vibrator?.hasVibrator() != true) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30L, 180))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30L)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Subtle, low-amplitude selection pulse.
     * Ideal for scrolling through tabs or color swatches.
     */
    fun selection() {
        try {
            if (vibrator?.hasVibrator() != true) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(8L, 50))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(8L)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Heavy, prominent mechanical impulse.
     * Ideal for long press context menus, drag start.
     */
    fun heavy() {
        try {
            if (vibrator?.hasVibrator() != true) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(35L, 220))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35L)
            }
        } catch (e: Exception) {
            // ignore
        }
    }
}

@Composable
fun rememberHapticFeedbackHelper(): HapticFeedbackHelper {
    val context = LocalContext.current
    return remember(context) { HapticFeedbackHelper(context) }
}
