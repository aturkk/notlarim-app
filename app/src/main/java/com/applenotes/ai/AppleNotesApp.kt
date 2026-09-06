package com.applenotes.ai

import android.app.Application
import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.data.local.AppDatabase
import com.applenotes.ai.data.remote.ai.AiServiceManager
import com.applenotes.ai.data.remote.github.GitHubUpdateService
import com.applenotes.ai.data.repository.NoteRepositoryImpl
import com.applenotes.ai.domain.repository.NoteRepository

class AppleNotesApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var securePreferences: SecurePreferences
        private set

    lateinit var noteRepository: NoteRepository
        private set

    lateinit var aiServiceManager: AiServiceManager
        private set

    lateinit var updateService: GitHubUpdateService
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        securePreferences = SecurePreferences(this)
        noteRepository = NoteRepositoryImpl(database)
        aiServiceManager = AiServiceManager(this, securePreferences)
        updateService = GitHubUpdateService(this, securePreferences)

        // Initialize background periodic backup scheduler
        com.applenotes.ai.core.backup.AutoBackupScheduler.schedule(this)
    }
}
