package com.applenotes.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.applenotes.ai.AppleNotesApp
import com.applenotes.ai.presentation.note_editor.NoteEditorScreen
import com.applenotes.ai.presentation.note_editor.NoteEditorViewModel
import com.applenotes.ai.presentation.notes_list.NotesListScreen
import com.applenotes.ai.presentation.notes_list.NotesListViewModel
import com.applenotes.ai.presentation.settings.SettingsScreen
import com.applenotes.ai.presentation.settings.SettingsViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    appContainer: AppleNotesApp
) {
    NavHost(
        navController = navController,
        startDestination = Screen.NotesList.route
    ) {
        composable(Screen.NotesList.route) {
            val viewModel = NotesListViewModel(
                repository = appContainer.noteRepository,
                updateService = appContainer.updateService,
                prefs = appContainer.securePreferences
            )
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
                },
                onDownloadUpdate = { downloadUrl ->
                    // Trigger download via updater
                    val settingsVm = SettingsViewModel(
                        prefs = appContainer.securePreferences,
                        updateService = appContainer.updateService,
                        aiServiceManager = appContainer.aiServiceManager
                    )
                    settingsVm.downloadAndInstallUpdate(downloadUrl)
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
            val viewModel = NoteEditorViewModel(
                noteId = noteId,
                repository = appContainer.noteRepository,
                aiServiceManager = appContainer.aiServiceManager
            )
            NoteEditorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel = SettingsViewModel(
                prefs = appContainer.securePreferences,
                updateService = appContainer.updateService,
                aiServiceManager = appContainer.aiServiceManager
            )
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
