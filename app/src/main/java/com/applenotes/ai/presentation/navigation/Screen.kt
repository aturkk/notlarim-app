package com.applenotes.ai.presentation.navigation

sealed class Screen(val route: String) {
    data object NotesList : Screen("notes_list")
    data object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: Long) = "note_editor/$noteId"
    }
    data object Settings : Screen("settings")
}
