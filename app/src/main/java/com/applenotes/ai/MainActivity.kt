package com.applenotes.ai

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.applenotes.ai.core.security.BiometricAuthHelper
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.core.web.WebClipperHelper
import com.applenotes.ai.core.widget.QuickNotesWidgetProvider
import com.applenotes.ai.presentation.navigation.AppNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : FragmentActivity() {

    private var pendingNavigationNoteId by mutableStateOf<Long?>(null)
    private var pendingAutoRecordAudio by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = application as AppleNotesApp
        handleIntent(intent, appContainer)

        setContent {
            val themeMode by appContainer.securePreferences.themeModeFlow.collectAsState()
            val accentColor by appContainer.securePreferences.accentColorFlow.collectAsState()
            val fontFamily by appContainer.securePreferences.fontFamilyFlow.collectAsState()
            val appLockEnabled by appContainer.securePreferences.appLockEnabledFlow.collectAsState()
            val privacyScreenEnabled by appContainer.securePreferences.privacyScreenEnabledFlow.collectAsState()

            LaunchedEffect(privacyScreenEnabled) {
                if (privacyScreenEnabled) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            var isAppUnlocked by rememberSaveable {
                mutableStateOf(!appContainer.securePreferences.appLockEnabled)
            }
            var lockErrorMessage by remember { mutableStateOf<String?>(null) }

            val triggerAppUnlock = {
                if (!BiometricAuthHelper.isBiometricAvailable(this@MainActivity)) {
                    isAppUnlocked = true
                } else {
                    BiometricAuthHelper.authenticate(
                        activity = this@MainActivity,
                        title = "Notism Kilitli",
                        subtitle = "Uygulamaya erişmek için kimliğinizi doğrulayın",
                        onSuccess = {
                            isAppUnlocked = true
                            lockErrorMessage = null
                        },
                        onError = { err ->
                            lockErrorMessage = err
                        }
                    )
                }
            }

            LaunchedEffect(appLockEnabled) {
                if (!appLockEnabled) {
                    isAppUnlocked = true
                } else if (!isAppUnlocked) {
                    triggerAppUnlock()
                }
            }

            AppleNotesTheme(
                themeMode = themeMode,
                accentColor = accentColor,
                fontFamily = fontFamily
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (appLockEnabled && !isAppUnlocked) {
                        val isDark = isAppDarkTheme()
                        val bg = if (isDark) iOSBackgroundDark else iOSBackgroundLight
                        val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
                        val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                        val tint = rememberAccentColor()

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(bg)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(CircleShape)
                                        .background(tint.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Uygulama Kilitli",
                                        tint = tint,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Notism Kilitli",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Notlarınızı görüntülemek için biyometrik kimlik doğrulaması veya cihaz şifrenizi kullanın.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                if (lockErrorMessage != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = lockErrorMessage!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFF3B30),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.height(28.dp))
                                Button(
                                    onClick = triggerAppUnlock,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = tint,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Kilidi Aç",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    } else {
                        val navController = rememberNavController()
                        AppNavigation(
                            navController = navController,
                            appContainer = appContainer,
                            initialNoteId = pendingNavigationNoteId,
                            autoRecordAudio = pendingAutoRecordAudio,
                            onConsumedNavigation = {
                                pendingNavigationNoteId = null
                                pendingAutoRecordAudio = false
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val appContainer = application as AppleNotesApp
        handleIntent(intent, appContainer)
    }

    private fun handleIntent(intent: Intent?, appContainer: AppleNotesApp) {
        if (intent == null) return
        val reminderNoteId = intent.getLongExtra("note_id", 0L)
        if (reminderNoteId > 0L) {
            pendingNavigationNoteId = reminderNoteId
            pendingAutoRecordAudio = false
            return
        }
        when (intent.action) {
            QuickNotesWidgetProvider.ACTION_NEW_NOTE -> {
                pendingNavigationNoteId = 0L
                pendingAutoRecordAudio = false
            }
            QuickNotesWidgetProvider.ACTION_VOICE_NOTE -> {
                pendingNavigationNoteId = 0L
                pendingAutoRecordAudio = true
            }
            Intent.ACTION_SEND -> {
                val clipped = WebClipperHelper.extractContentFromIntent(intent)
                CoroutineScope(Dispatchers.IO).launch {
                    val newNote = WebClipperHelper.fetchMetadataAndBuildNote(
                        clipped = clipped,
                        aiServiceManager = appContainer.aiServiceManager
                    )
                    val savedId = appContainer.noteRepository.saveNote(newNote)
                    withContext(Dispatchers.Main) {
                        pendingNavigationNoteId = savedId
                        pendingAutoRecordAudio = false
                    }
                    QuickNotesWidgetProvider.notifyDataChanged(this@MainActivity)
                }
            }
        }
    }
}
