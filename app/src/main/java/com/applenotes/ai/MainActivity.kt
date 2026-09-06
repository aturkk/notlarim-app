package com.applenotes.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.applenotes.ai.core.theme.AppleNotesTheme
import com.applenotes.ai.core.widget.QuickNotesWidgetProvider
import com.applenotes.ai.domain.model.Note
import com.applenotes.ai.presentation.navigation.AppNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.applenotes.ai.core.web.WebClipperHelper
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
            AppleNotesTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val appContainer = application as AppleNotesApp
        handleIntent(intent, appContainer)
    }

    private fun handleIntent(intent: Intent?, appContainer: AppleNotesApp) {
        if (intent == null) return
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
