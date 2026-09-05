package com.applenotes.ai.data.remote.ai

import android.content.Context
import android.util.Log
import com.applenotes.ai.domain.model.Note
import com.applenotes.ai.domain.model.TitleAndTagsResult
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

private const val TAG = "OnDeviceAiClient"

/**
 * On-device AI client using MediaPipe LLM Inference API (tasks-genai).
 * Requires a Gemma model file (.bin) to be present on the device.
 *
 * Recommended model: gemma-2b-it-gpu-int4.bin (~1.4 GB) or gemma-2b-it-cpu-int4.bin (~1.4 GB)
 * Download from: https://huggingface.co/google/gemma-2b-it-gpu-int4
 * Push to device: adb push gemma-2b-it-gpu-int4.bin /data/local/tmp/llm/
 */
class OnDeviceAiClient(private val context: Context) {

    private var llmInference: LlmInference? = null
    private var currentModelPath: String = ""

    /**
     * Attempts to initialize the MediaPipe LLM engine with the given model path.
     * Returns true on success, false if model file is not found or initialization fails.
     */
    fun initialize(modelPath: String): Boolean {
        if (modelPath.isBlank()) return false
        if (!File(modelPath).exists()) {
            Log.w(TAG, "Model file not found at: $modelPath")
            return false
        }
        return try {
            if (modelPath == currentModelPath && llmInference != null) return true
            llmInference?.close()
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(512)
                .setMaxTopK(40)
                .build()
            llmInference = LlmInference.createFromOptions(context, options)
            currentModelPath = modelPath
            Log.i(TAG, "MediaPipe LLM initialized with: $modelPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "LLM init failed: ${e.message}", e)
            llmInference = null
            false
        }
    }

    fun isInitialized(): Boolean = llmInference != null

    fun close() {
        llmInference?.close()
        llmInference = null
    }

    // ─── Core inference ───────────────────────────────────────────────────────

    private suspend fun infer(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val engine = llmInference
            ?: return@withContext Result.failure(Exception(
                "Cihaz içi AI modeli yüklü değil. Lütfen Ayarlar > Gemini Nano bölümünden model dosyası yolunu girin."
            ))
        return@withContext try {
            val result = suspendCancellableCoroutine<String> { cont ->
                val sb = StringBuilder()
                engine.generateResponseAsync(prompt) { partialResult, done ->
                    if (partialResult != null) sb.append(partialResult)
                    if (done) cont.resume(sb.toString().trim())
                }
            }
            if (result.isBlank()) {
                Result.failure(Exception("Model boş yanıt döndürdü."))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Cihaz içi AI Hatası: ${e.localizedMessage ?: e.message}"))
        }
    }

    // ─── Public AI methods ────────────────────────────────────────────────────

    suspend fun summarize(content: String): Result<String> {
        val prompt = buildPrompt(
            system = "Sen bir not asistanısın. Türkçe, kısa ve madde imli (• ile) özetler yazarsın.",
            user = "Aşağıdaki notu özetle:\n\n$content"
        )
        return infer(prompt)
    }

    suspend fun rewrite(content: String, tone: String): Result<String> {
        val toneDesc = when (tone.lowercase()) {
            "professional" -> "resmi, kurumsal ve profesyonel"
            "casual" -> "samimi, sıcak ve konuşma diline yakın"
            "concise" -> "kısa, öz ve gereksiz tekrarsız"
            else -> tone
        }
        val prompt = buildPrompt(
            system = "Sen bir metin düzenleme asistanısın. Verilen metni belirtilen üslupta yeniden yazarsın.",
            user = "Aşağıdaki metni $toneDesc üslupta yeniden yaz. Sadece yeniden yazılmış metni ver:\n\n$content"
        )
        return infer(prompt)
    }

    suspend fun extractActions(content: String): Result<String> {
        val prompt = buildPrompt(
            system = "Sen bir görev yöneticisisin. Metinlerden yapılacak işleri Markdown todo listesi (- [ ] Görev) olarak çıkarırsın.",
            user = "Aşağıdaki metinden yapılacak işleri çıkar:\n\n$content"
        )
        return infer(prompt)
    }

    suspend fun suggestTitleAndTags(content: String): Result<TitleAndTagsResult> {
        val prompt = buildPrompt(
            system = "Format kesinlikle şöyle olmalıdır:\nÖrnek Başlık\n#etiket1 #etiket2\nBaşka hiçbir şey yazma.",
            user = "Bu not için 1 başlık ve 2-4 etiket öner:\n\n$content"
        )
        return infer(prompt).map { rawText ->
            val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
            val title = lines.firstOrNull()?.removePrefix("Başlık:")?.trim() ?: "Yeni Not"
            val tagsLine = lines.getOrNull(1)?.removePrefix("Etiketler:")?.trim() ?: ""
            val tags = tagsLine.split(" ", ",")
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }
                .distinct()
            TitleAndTagsResult(title = title, tags = tags)
        }
    }

    suspend fun fixGrammar(content: String): Result<String> {
        val prompt = buildPrompt(
            system = "Sen bir Türkçe dil editörüsün. Yazım, gramer ve noktalama hatalarını düzeltirsin.",
            user = "Aşağıdaki metindeki hataları düzelt. Sadece düzeltilmiş metni ver:\n\n$content"
        )
        return infer(prompt)
    }

    suspend fun translate(content: String, targetLang: String): Result<String> {
        val prompt = buildPrompt(
            system = "Sen profesyonel bir çevirmensin. Doğal ve akıcı çeviriler yaparsın.",
            user = "Aşağıdaki metni $targetLang diline çevir. Sadece çeviriyi ver:\n\n$content"
        )
        return infer(prompt)
    }

    suspend fun continueWriting(content: String): Result<String> {
        val prompt = buildPrompt(
            system = "Sen yaratıcı bir yazar asistanısın. Verilen metnin devamını aynı üslupta yazarsın.",
            user = "Aşağıdaki metnin devamını yaz:\n\n$content"
        )
        return infer(prompt)
    }

    suspend fun generateFlashcards(content: String): Result<String> {
        val prompt = buildPrompt(
            system = "Sen bir öğretmensin. Notlardan 3-5 adet soru-cevap flashcard oluşturursun.",
            user = "Aşağıdaki nottan flashcard oluştur:\n\n$content"
        )
        return infer(prompt)
    }

    suspend fun generateMindmap(content: String): Result<String> {
        val prompt = buildPrompt(
            system = "Sen bir zihin haritası uzmanısın. Hiyerarşik, girintili liste formatında zihin haritası oluşturursun.",
            user = "Aşağıdaki nottan zihin haritası oluştur:\n\n$content"
        )
        return infer(prompt)
    }

    suspend fun extractReminders(content: String): Result<String> {
        val prompt = buildPrompt(
            system = "Sen bir takvim asistanısın. Metinlerdeki tarih, saat ve randevuları listeleyerek çıkarırsın.",
            user = "Aşağıdaki metindeki tarih/saat/randevu bilgilerini çıkar:\n\n$content"
        )
        return infer(prompt)
    }

    suspend fun chatWithNote(noteContent: String, question: String): Result<String> {
        val prompt = buildPrompt(
            system = "Sen bir not asistanısın. Kullanıcının notuna dayanarak soruları Türkçe yanıtlarsın.\n\nNot içeriği:\n---\n$noteContent\n---",
            user = question
        )
        return infer(prompt)
    }

    suspend fun chatWithAllNotes(allNotes: List<Note>, question: String): Result<String> {
        val notesContext = allNotes.take(10).joinToString("\n\n---\n") { note ->
            "Başlık: ${note.title}\nİçerik: ${note.content.take(400)}"
        }
        val prompt = buildPrompt(
            system = "Sen kullanıcının tüm notlarını bilen bir kişisel asistansın. Notlara dayanarak soruları Türkçe yanıtlarsın.\n\nNotlar:\n====================\n$notesContext\n====================",
            user = question
        )
        return infer(prompt)
    }

    suspend fun generateText(prompt: String): Result<String> = infer(prompt)

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Builds a simple system+user prompt in Gemma chat template format:
     * <start_of_turn>system\n...<end_of_turn>\n<start_of_turn>user\n...<end_of_turn>\n<start_of_turn>model\n
     */
    private fun buildPrompt(system: String, user: String): String {
        return "<start_of_turn>system\n$system<end_of_turn>\n<start_of_turn>user\n$user<end_of_turn>\n<start_of_turn>model\n"
    }
}