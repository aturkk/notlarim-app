package com.applenotes.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.applenotes.ai.AppleNotesApp
import com.applenotes.ai.presentation.note_editor.NoteEditorScreen
import com.applenotes.ai.presentation.note_editor.NoteEditorViewModel
import com.applenotes.ai.presentation.note_editor.components.EditorTabItem
import com.applenotes.ai.presentation.notes_list.NotesListScreen
import com.applenotes.ai.presentation.notes_list.NotesListViewModel
import com.applenotes.ai.presentation.settings.SettingsScreen
import com.applenotes.ai.presentation.settings.SettingsViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    appContainer: AppleNotesApp,
    initialNoteId: Long? = null,
    autoRecordAudio: Boolean = false,
    onConsumedNavigation: () -> Unit = {}
) {
    var activeAutoRecordAudio by remember { mutableStateOf(false) }
    var recentTabs by remember { mutableStateOf<List<EditorTabItem>>(emptyList()) }

    LaunchedEffect(initialNoteId) {
        if (initialNoteId != null) {
            activeAutoRecordAudio = autoRecordAudio
            navController.navigate(Screen.NoteEditor.createRoute(initialNoteId))
            onConsumedNavigation()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.NotesList.route
    ) {
        composable(Screen.NotesList.route) {
            val viewModel = remember {
                NotesListViewModel(
                    repository = appContainer.noteRepository,
                    updateService = appContainer.updateService,
                    prefs = appContainer.securePreferences,
                    aiServiceManager = appContainer.aiServiceManager
                )
            }
            NotesListScreen(
                viewModel = viewModel,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                },
                onNewNoteClick = {
                    navController.navigate(Screen.NoteEditor.createRoute(0L))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.NoteEditor.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val viewModel = remember(noteId) {
                NoteEditorViewModel(
                    noteId = noteId,
                    repository = appContainer.noteRepository,
                    aiServiceManager = appContainer.aiServiceManager
                )
            }
            NoteEditorScreen(
                viewModel = viewModel,
                autoRecordAudio = activeAutoRecordAudio,
                onConsumedAutoRecord = { activeAutoRecordAudio = false },
                recentTabs = recentTabs,
                onUpdateRecentTabs = { recentTabs = it },
                onBack = {
                    activeAutoRecordAudio = false
                    navController.popBackStack()
                },
                onNavigateToNote = { targetId ->
                    navController.navigate(Screen.NoteEditor.createRoute(targetId))
                }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel = remember {
                SettingsViewModel(
                    prefs = appContainer.securePreferences,
                    updateService = appContainer.updateService,
                    aiServiceManager = appContainer.aiServiceManager,
                    repository = appContainer.noteRepository
                )
            }
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
