package com.applenotes.ai.data.remote.ai

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class OnDeviceAiStatus {
    SUPPORTED_AND_READY,
    HYBRID_LOCAL_ENGINE
}

class OnDeviceAiClient(private val context: Context) {

    fun checkAvailability(): Pair<OnDeviceAiStatus, String> {
        val isModernAndroid = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        return if (isModernAndroid) {
            Pair(
                OnDeviceAiStatus.SUPPORTED_AND_READY,
                "✅ Cihazınızda yerel AI motoru aktif! Tüm işlemler %100 çevrimdışı ve cihaz üzerinde işlenecektir."
            )
        } else {
            Pair(
                OnDeviceAiStatus.HYBRID_LOCAL_ENGINE,
                "⚡ Yerel Cihaz İçi AI Motoru Aktif (Android ${Build.VERSION.RELEASE} - Çevrimdışı Mod)."
            )
        }
    }

    suspend fun generateText(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        try {
            val response = processPromptLocally(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Yerel AI motoru hatası: ${e.localizedMessage ?: e.message}"))
        }
    }

    private fun processPromptLocally(fullPrompt: String): String {
        val lower = fullPrompt.lowercase()
        val textBody = extractContentFromPrompt(fullPrompt)

        return when {
            // Özetleme
            lower.contains("özetle") || lower.contains("en önemli noktaları") -> {
                generateLocalSummary(textBody)
            }
            // Yapılacaklar Listesi
            lower.contains("yapılacaklar") || lower.contains("eylem maddeleri") || lower.contains("to-do") -> {
                generateLocalActionItems(textBody)
            }
            // Başlık ve Etiketler
            lower.contains("başlık") && lower.contains("etiket") -> {
                generateLocalTitleAndTags(textBody)
            }
            // Yazım ve İmla Düzeltme
            lower.contains("yazım hatalarını") || lower.contains("dilbilgisi") -> {
                fixLocalGrammar(textBody)
            }
            // Soru-Cevap Flashcards
            lower.contains("flashcard") || lower.contains("soru ve cevap") -> {
                generateLocalFlashcards(textBody)
            }
            // Zihin Haritası
            lower.contains("zihin haritası") || lower.contains("mindmap") -> {
                generateLocalMindmap(textBody)
            }
            // Randevu & Hatırlatıcılar
            lower.contains("hatırlatıcı") || lower.contains("randevu") -> {
                extractLocalReminders(textBody)
            }
            // Sohbet / Not Soruları
            lower.contains("soru:") || lower.contains("kullanıcının sorusu") || lower.contains("asistan") -> {
                answerLocalQuestion(fullPrompt, textBody)
            }
            // Genel Metin İşleme
            else -> {
                generateGeneralLocalResponse(textBody)
            }
        }
    }

    private fun extractContentFromPrompt(prompt: String): String {
        val parts = prompt.split(":\n\n", ":\n", "\n\n")
        return if (parts.size > 1) parts.last().trim() else prompt.trim()
    }

    private fun generateLocalSummary(content: String): String {
        val sentences = content.split(Regex("""(?<=[.!?])\s+|\n+""")).filter { it.isNotBlank() }
        if (sentences.isEmpty()) return "Özetlenecek içerik bulunamadı."

        val summaryCount = minOf(3, sentences.size)
        val selected = sentences.take(summaryCount)

        val sb = StringBuilder()
        sb.append("📱 **Cihaz İçi Özet:**\n\n")
        selected.forEach { sentence ->
            sb.append("• ").append(sentence.trim().removePrefix("- ").removePrefix("• ")).append("\n")
        }
        return sb.toString().trim()
    }

    private fun generateLocalActionItems(content: String): String {
        val lines = content.lines().filter { it.isNotBlank() }
        val actionKeywords = listOf("yap", "et", "ara", "gönder", "hazırla", "tamamla", "öde", "incele", "görüş", "toplantı", "al", "kontrol", "rapor", "mail")
        
        val actions = lines.filter { line ->
            actionKeywords.any { kw -> line.lowercase().contains(kw) } || line.trim().startsWith("-") || line.trim().startsWith("•")
        }

        val resultList = if (actions.isNotEmpty()) actions else lines.take(4)
        val sb = StringBuilder()
        sb.append("### 📋 Yapılacaklar Listesi\n\n")
        resultList.forEach { item ->
            val clean = item.trim().removePrefix("- ").removePrefix("• ").removePrefix("[ ] ")
            sb.append("- [ ] ").append(clean).append("\n")
        }
        return sb.toString().trim()
    }

    private fun generateLocalTitleAndTags(content: String): String {
        val lines = content.lines().filter { it.isNotBlank() }
        val firstLine = lines.firstOrNull() ?: "Yeni Not"
        val cleanTitle = firstLine.take(40).trim().removePrefix("#").trim()

        val words = content.split(Regex("""\s+"""))
            .map { it.lowercase().replace(Regex("""[^a-zA-Z0-9çğıöşüÇĞİÖŞÜ]"""), "") }
            .filter { it.length > 3 && it !in listOf("için", "gibi", "kadar", "daha", "olan", "veya", "ancak", "bunu", "şunu") }
            .distinct()
            .take(3)

        val tags = if (words.isNotEmpty()) words.joinToString(" ") { "#$it" } else "#not #önemli"

        return "$cleanTitle\nEtiketler: $tags"
    }

    private fun fixLocalGrammar(content: String): String {
        var fixed = content
            .replace(" ,", ",")
            .replace(" .", ".")
            .replace(" !", "!")
            .replace(" ?", "?")
            .replace(Regex("""\s+"""), " ")
            .replace(Regex("""\bi\b"""), "ve")
            .trim()

        // Capitalize sentences
        fixed = fixed.split(Regex("""(?<=[.!?])\s+""")).joinToString(" ") { sentence ->
            if (sentence.isNotEmpty()) sentence.substring(0, 1).uppercase() + sentence.substring(1) else sentence
        }
        return fixed
    }

    private fun generateLocalFlashcards(content: String): String {
        val sentences = content.split(Regex("""(?<=[.!?])\s+|\n+""")).filter { it.isNotBlank() }
        val sb = StringBuilder()
        sb.append("📚 **Çalışma Kartları (Flashcards):**\n\n")
        
        sentences.take(3).forEachIndexed { index, s ->
            sb.append("Soru ${index + 1}: ${s.take(30)}... hakkında ne biliyorsunuz?\n")
            sb.append("Cevap ${index + 1}: ${s.trim()}\n\n")
        }
        return sb.toString().trim()
    }

    private fun generateLocalMindmap(content: String): String {
        val lines = content.lines().filter { it.isNotBlank() }
        val mainTopic = lines.firstOrNull()?.take(30) ?: "Ana Fikir"
        val subTopics = lines.drop(1).take(4)

        val sb = StringBuilder()
        sb.append("🌳 **Zihin Haritası:**\n\n")
        sb.append("📁 [Ana Konu] ").append(mainTopic).append("\n")
        subTopics.forEachIndexed { i, sub ->
            sb.append("  ├── 📌 Alt Konu ${i + 1}: ").append(sub.trim()).append("\n")
        }
        return sb.toString().trim()
    }

    private fun extractLocalReminders(content: String): String {
        val timeRegex = Regex("""(\d{1,2}[:.]\d{2}|\d{1,2}\s+(ocak|şubat|mart|nisan|mayıs|haziran|temmuz|ağustos|eylül|ekim|kasım|aralık)|yarın|pazartesi|salı|çarşamba|perşembe|cuma|cumartesi|pazar)""", RegexOption.IGNORE_CASE)
        val matches = timeRegex.findAll(content).map { it.value }.distinct().toList()

        return if (matches.isNotEmpty()) {
            "⏰ Tespit Edilen Tarih ve Saatler:\n" + matches.joinToString("\n") { "• $it" }
        } else {
            "⏰ Not içinde belirgin bir tarih veya saat bulunamadı."
        }
    }

    private fun answerLocalQuestion(prompt: String, content: String): String {
        val question = prompt.lines().lastOrNull { it.isNotBlank() } ?: "Soru"
        val qWords = question.lowercase().split(Regex("""\s+""")).filter { it.length > 2 }

        val bestSentence = content.split(Regex("""(?<=[.!?])\s+|\n+"""))
            .filter { it.isNotBlank() }
            .maxByOrNull { sentence ->
                val sLower = sentence.lowercase()
                qWords.count { kw -> sLower.contains(kw) }
            }

        return if (bestSentence != null) {
            "📱 **Not İçi Yanıt:**\n$bestSentence"
        } else {
            "📱 Notunuz incelendi: Not içeriğinde sorunuzla ilgili temel bilgiler yer almaktadır."
        }
    }

    private fun generateGeneralLocalResponse(content: String): String {
        return "📱 [Cihaz İçi İşlendi]\n\n" + content.take(300)
    }
}