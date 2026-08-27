package com.applenotes.ai.domain.model

data class AppUpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val changelog: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean,
    val publishedAt: String = ""
)
