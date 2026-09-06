package com.applenotes.ai.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*
import kotlinx.coroutines.delay

/**
 * Emil Kowalski Motion Skill: Tactile Depress / Bouncy Press
 * Scales element down to 0.96f on press with physics-based spring return and subtle haptic feedback.
 */
@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncyScale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

data class SegmentItem(
    val title: String,
    val icon: ImageVector? = null
)

/**
 * Emil Kowalski & Apple Design Skill: Physics-based Segmented Control
 * Fast, tactile view switching with animated sliding background indicator.
 */
@Composable
fun AppleSegmentedControl(
    items: List<SegmentItem>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val haptic = LocalHapticFeedback.current

    val containerBg = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val selectedPillBg = if (isDark) Color(0xFF1C1C1E) else Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerBg)
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(11.dp))
                                    .background(selectedPillBg)
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onIndexSelected(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) {
                                    AppleYellow
                                } else {
                                    if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                                },
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
                            } else {
                                if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Emil Kowalski & Notism AI: Smart AI Action Capsule
 * Subtle glowing pill to launch Morning Digest, Global AI Chat, and Synthesis without cluttering header.
 */
@Composable
fun AiSmartPillHeader(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            AppleYellow.copy(alpha = 0.25f),
            AppleYellowDark.copy(alpha = 0.15f),
            Color(0xFFE89A3C).copy(alpha = 0.2f)
        )
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF24231E) else Color(0xFFFFF9EE),
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable(pressedScale = 0.97f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, AppleYellow.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(gradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AppleYellow,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Notism Asistanı",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = "Sabah Brifingi · Global AI Sohbeti · Not Sentezi",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "Keşfet ❯",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppleYellow
            )
        }
    }
}

/**
 * Sonner-Style Floating Toast Pill (Emil Kowalski Pattern)
 * An elegant, non-intrusive floating feedback pill with spring scale & fade.
 */
@Composable
fun SonnerFloatingToast(
    message: String?,
    onDismiss: () -> Unit,
    durationMs: Long = 2800L,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val isDark = isAppDarkTheme()
    val effectiveDuration = if (actionLabel != null && onAction != null) 4500L else durationMs

    LaunchedEffect(message) {
        if (message != null) {
            delay(effectiveDuration)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        ) + fadeIn() + scaleIn(initialScale = 0.95f),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        ) + fadeOut() + scaleOut(targetScale = 0.95f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xFF2C2C2E).copy(alpha = 0.92f) else Color(0xFF1C1C1E).copy(alpha = 0.92f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                    if (actionLabel != null && onAction != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = actionLabel,
                            color = AppleYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    onAction()
                                    onDismiss()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
