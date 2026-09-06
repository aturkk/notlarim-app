package com.applenotes.ai.core.templates

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@Serializable
data class CustomTemplate(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val icon: String = "📝",
    val content: String,
    val defaultTags: List<String> = emptyList()
)

class CustomTemplateManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val file: File
        get() = File(context.filesDir, "custom_templates.json")

    private val _templatesFlow = MutableStateFlow<List<CustomTemplate>>(emptyList())
    val templatesFlow: StateFlow<List<CustomTemplate>> = _templatesFlow.asStateFlow()

    init {
        loadTemplates()
    }

    fun loadTemplates(): List<CustomTemplate> {
        val list = try {
            if (file.exists()) {
                val content = file.readText()
                json.decodeFromString<List<CustomTemplate>>(content)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
        _templatesFlow.value = list
        return list
    }

    suspend fun saveTemplate(template: CustomTemplate) = withContext(Dispatchers.IO) {
        val current = loadTemplates().toMutableList()
        val index = current.indexOfFirst { it.id == template.id }
        if (index >= 0) {
            current[index] = template
        } else {
            current.add(0, template)
        }
        try {
            file.writeText(json.encodeToString(current))
            _templatesFlow.value = current
        } catch (e: Exception) {
            // Ignore write errors
        }
    }

    suspend fun deleteTemplate(templateId: String) = withContext(Dispatchers.IO) {
        val current = loadTemplates().filterNot { it.id == templateId }
        try {
            file.writeText(json.encodeToString(current))
            _templatesFlow.value = current
        } catch (e: Exception) {
            // Ignore write errors
        }
    }
}
