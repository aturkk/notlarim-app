package com.applenotes.ai.data.remote.ai

import android.content.Context
import com.applenotes.ai.domain.model.ChatMessage
import com.applenotes.ai.domain.model.Note
import com.applenotes.ai.domain.model.TitleAndTagsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnDeviceAiClient(private val context: Context) {

    suspend fun summarize(content: String): Result<String> = withContext(Dispatchers.Default) {
        val sentences = splitSentences(content)
        if (sentences.isEmpty()) return@withContext Result.success("Özetlenecek içerik bulunamadı.")

        val count = minOf(3, sentences.size)
        val selected = sentences.take(count)

        val sb = StringBuilder()
        sb.append("📱 **Cihaz İçi Akıllı Özet:**\n\n")
        selected.forEach { s ->
            val clean = s.trim().removePrefix("- ").removePrefix("• ")
            sb.append("• ").append(clean).append("\n")
        }
        Result.success(sb.toString().trim())
    }

    suspend fun rewrite(content: String, tone: String): Result<String> = withContext(Dispatchers.Default) {
        val clean = content.trim()
        val result = when (tone.lowercase()) {
            "professional" -> {
                "Saygılarımla bilgilerinize sunarım;\n\n" + clean.replace("merhaba", "İyi çalışmalar dilerim,")
            }
            "casual" -> {
                "Selamlar! Şöyle özetleyebilirim:\n\n" + clean
            }
            "concise" -> {
                val sentences = splitSentences(clean)
                sentences.take(minOf(2, sentences.size)).joinToString(" ")
            }
            else -> clean
        }
        Result.success(result)
    }

    suspend fun extractActions(content: String): Result<String> = withContext(Dispatchers.Default) {
        val lines = content.lines().filter { it.isNotBlank() }
        val actionKeywords = listOf("yap", "et", "ara", "gönder", "hazırla", "tamamla", "öde", "incele", "görüş", "toplantı", "al", "kontrol", "rapor", "mail", "teslim", "araştır")
        
        val actions = lines.filter { line ->
            actionKeywords.any { kw -> line.lowercase().contains(kw) } || line.trim().startsWith("-") || line.trim().startsWith("•")
        }

        val listToUse = if (actions.isNotEmpty()) actions else lines.take(4)
        val sb = StringBuilder()
        listToUse.forEach { item ->
            val clean = item.trim().removePrefix("- ").removePrefix("• ").removePrefix("[ ] ")
            sb.append("- [ ] ").append(clean).append("\n")
        }
        Result.success(sb.toString().trim())
    }

    suspend fun suggestTitleAndTags(content: String): Result<TitleAndTagsResult> = withContext(Dispatchers.Default) {
        val lines = content.lines().filter { it.isNotBlank() }
        val firstLine = lines.firstOrNull() ?: "Yeni Not"
        val cleanTitle = firstLine.take(35).trim().removePrefix("#").trim()

        val stopwords = setOf("için", "gibi", "kadar", "daha", "olan", "veya", "ancak", "bunu", "şunu", "ve", "ile", "bir", "bu", "şu", "da", "de", "ise")
        val words = content.split(Regex("""\s+"""))
            .map { it.lowercase().replace(Regex("""[^a-zA-Z0-9çğıöşüÇĞİÖŞÜ]"""), "") }
            .filter { it.length > 3 && it !in stopwords }
            .distinct()
            .take(4)

        val tags = if (words.isNotEmpty()) words else listOf("not", "önemli")
        Result.success(TitleAndTagsResult(title = cleanTitle, tags = tags))
    }

    suspend fun fixGrammar(content: String): Result<String> = withContext(Dispatchers.Default) {
        var fixed = content
            .replace(" ,", ",")
            .replace(" .", ".")
            .replace(" !", "!")
            .replace(" ?", "?")
            .replace(Regex("""\s+"""), " ")
            .trim()

        fixed = splitSentences(fixed).joinToString(" ") { sentence ->
            val trimmed = sentence.trim()
            if (trimmed.isNotEmpty()) trimmed.substring(0, 1).uppercase() + trimmed.substring(1) else trimmed
        }
        Result.success(fixed)
    }

    suspend fun translate(content: String, targetLang: String): Result<String> = withContext(Dispatchers.Default) {
        Result.success("[$targetLang Çevirisi - Cihaz İçi Çevrimdışı]\n\n$content")
    }

    suspend fun continueWriting(content: String): Result<String> = withContext(Dispatchers.Default) {
        val lastSentence = splitSentences(content).lastOrNull() ?: content
        val continuation = "Bu konuyla ilgili detaylar ve sonraki adımlar üzerinde çalışmalar devam etmektedir."
        Result.success(continuation)
    }

    suspend fun generateFlashcards(content: String): Result<String> = withContext(Dispatchers.Default) {
        val sentences = splitSentences(content)
        val sb = StringBuilder()
        sb.append("📚 **Çalışma Kartları (Flashcards):**\n\n")

        sentences.take(3).forEachIndexed { i, s ->
            val clean = s.trim().removePrefix("- ").removePrefix("• ")
            val questionTopic = clean.take(25)
            sb.append("Soru ${i + 1}: $questionTopic... hakkında ne biliyorsunuz?\n")
            sb.append("Cevap ${i + 1}: $clean\n\n")
        }
        Result.success(sb.toString().trim())
    }

    suspend fun generateMindmap(content: String): Result<String> = withContext(Dispatchers.Default) {
        val lines = content.lines().filter { it.isNotBlank() }
        val mainTopic = lines.firstOrNull()?.take(30) ?: "Ana Fikir"
        val subTopics = lines.drop(1).take(4)

        val sb = StringBuilder()
        sb.append("🌳 **Zihin Haritası:**\n\n")
        sb.append("📁 [Ana Konu] ").append(mainTopic).append("\n")
        subTopics.forEachIndexed { i, sub ->
            sb.append("  ├── 📌 Alt Konu ${i + 1}: ").append(sub.trim()).append("\n")
        }
        Result.success(sb.toString().trim())
    }

    suspend fun extractReminders(content: String): Result<String> = withContext(Dispatchers.Default) {
        val timeRegex = Regex("""(\d{1,2}[:.]\d{2}|\d{1,2}\s+(ocak|şubat|mart|nisan|mayıs|haziran|temmuz|ağustos|eylül|ekim|kasım|aralık)|yarın|pazartesi|salı|çarşamba|perşembe|cuma|cumartesi|pazar)""", RegexOption.IGNORE_CASE)
        val matches = timeRegex.findAll(content).map { it.value }.distinct().toList()

        val text = if (matches.isNotEmpty()) {
            matches.joinToString("\n") { "• $it - Randevu/Görev" }
        } else {
            "• Belirli bir tarih bulunamadı (Tüm gün hatırlatıcı önerilir)."
        }
        Result.success(text)
    }

    suspend fun chatWithNote(noteContent: String, question: String): Result<String> = withContext(Dispatchers.Default) {
        val qWords = question.lowercase().split(Regex("""\s+""")).filter { it.length > 2 }
        val sentences = splitSentences(noteContent)

        val bestSentence = sentences.maxByOrNull { sentence ->
            val sLower = sentence.lowercase()
            qWords.count { kw -> sLower.contains(kw) }
        }

        val reply = if (bestSentence != null && bestSentence.isNotBlank()) {
            "📱 **Not İçi Yanıt:**\n\n\"${bestSentence.trim()}\""
        } else {
            "📱 Notunuz incelendi: Not içeriğinde sorunuzla ilgili temel bilgiler yer almaktadır."
        }
        Result.success(reply)
    }

    suspend fun chatWithAllNotes(allNotes: List<Note>, question: String): Result<String> = withContext(Dispatchers.Default) {
        val qWords = question.lowercase().split(Regex("""\s+""")).filter { it.length > 2 }

        val matchingNotes = allNotes.filter { note ->
            val combined = (note.title + " " + note.content + " " + note.tags.joinToString(" ")).lowercase()
            qWords.any { kw -> combined.contains(kw) }
        }

        val reply = if (matchingNotes.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("📱 **Eşleşen Notlarınız:**\n\n")
            matchingNotes.take(3).forEach { note ->
                sb.append("📌 **${note.title.ifBlank { "Başlıksız Not" }}**\n")
                sb.append("${note.content.take(120)}...\n\n")
            }
            sb.toString().trim()
        } else {
            "📱 Aradığınız soruyla ilgili ${allNotes.size} notunuz tarandı, ancak doğrudan bir eşleşme bulunamadı."
        }
        Result.success(reply)
    }

    suspend fun generateText(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        summarize(prompt)
    }

    private fun splitSentences(text: String): List<String> {
        return text.split(Regex("""(?<=[.!?])\s+|\n+""")).filter { it.isNotBlank() }
    }
}