package com.applenotes.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
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

class MainActivity : FragmentActivity() {

    private var initialNoteId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = application as AppleNotesApp
        handleIntent(intent, appContainer)

        setContent {
            AppleNotesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        appContainer = appContainer,
                        initialNoteId = initialNoteId
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
            QuickNotesWidgetProvider.ACTION_NEW_NOTE,
            QuickNotesWidgetProvider.ACTION_VOICE_NOTE -> {
                initialNoteId = 0L
            }
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                    val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
                    CoroutineScope(Dispatchers.IO).launch {
                        val newNote = Note(
                            title = subject.ifBlank { "Web Kırpıntısı" },
                            content = text,
                            tags = listOf("Web"),
                            updatedAt = System.currentTimeMillis()
                        )
                        val id = appContainer.noteRepository.saveNote(newNote)
                        initialNoteId = id
                    }
                }
            }
        }
    }
}
