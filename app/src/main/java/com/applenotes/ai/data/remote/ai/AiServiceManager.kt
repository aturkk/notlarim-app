package com.applenotes.ai.data.remote.ai

import android.content.Context
import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.domain.model.AiAction
import com.applenotes.ai.domain.model.AiProvider
import com.applenotes.ai.domain.model.ChatMessage
import com.applenotes.ai.domain.model.MessageRole
import com.applenotes.ai.domain.model.TitleAndTagsResult
import com.applenotes.ai.domain.model.Note

class AiServiceManager(
    private val context: Context,
    private val prefs: SecurePreferences
) {
    private val geminiClient = GeminiApiClient()
    private val vertexAiClient = VertexAiApiClient()
    private val openAiClient = OpenAiApiClient()
    private val claudeClient = ClaudeApiClient()
    val onDeviceClient = OnDeviceAiClient(context)

    init {
        // Try to initialize on-device model if a path is already stored
        val savedPath = prefs.onDeviceModelPath
        if (savedPath.isNotBlank()) {
            onDeviceClient.initialize(savedPath)
        }
    }

    /** Call this when user updates the model path in settings */
    fun initializeOnDeviceModel(modelPath: String): Boolean {
        prefs.onDeviceModelPath = modelPath
        return onDeviceClient.initialize(modelPath)
    }

    suspend fun executeAction(action: AiAction, noteContent: String): Result<String> {
        if (prefs.getActiveAiProvider() == AiProvider.GEMINI_NANO) {
            return when (action) {
                AiAction.SUMMARIZE -> onDeviceClient.summarize(noteContent)
                AiAction.REWRITE_PROFESSIONAL -> onDeviceClient.rewrite(noteContent, "professional")
                AiAction.REWRITE_CASUAL -> onDeviceClient.rewrite(noteContent, "casual")
                AiAction.REWRITE_CONCISE -> onDeviceClient.rewrite(noteContent, "concise")
                AiAction.EXTRACT_ACTIONS -> onDeviceClient.extractActions(noteContent)
                AiAction.AUTO_TITLE_TAGS -> onDeviceClient.suggestTitleAndTags(noteContent).map { "${it.title}\nEtiketler: ${it.tags.joinToString(" ") { t -> "#$t" }}" }
                AiAction.FIX_GRAMMAR -> onDeviceClient.fixGrammar(noteContent)
                AiAction.TRANSLATE -> onDeviceClient.translate(noteContent, "İngilizce")
                AiAction.CONTINUE_WRITING -> onDeviceClient.continueWriting(noteContent)
                AiAction.FLASHCARDS -> onDeviceClient.generateFlashcards(noteContent)
                AiAction.MINDMAP -> onDeviceClient.generateMindmap(noteContent)
                AiAction.EXTRACT_REMINDERS -> onDeviceClient.extractReminders(noteContent)
            }
        }

        val (prompt, systemPrompt) = when (action) {
            AiAction.SUMMARIZE -> Pair(
                "Lütfen aşağıdaki notu analiz et ve en önemli noktaları maddeler halinde, açık ve öz bir şekilde özetle:\n\n" + noteContent,
                "Sen kullanıcının notlarını analiz eden akıllı ve titiz bir asistansın. Türkçe, net, anlaşılır ve madde imli (bullet points) özetler sunarsın."
            )
            AiAction.REWRITE_PROFESSIONAL -> Pair(
                "Aşağıdaki notu resmi, kurumsal ve profesyonel bir üslupla yeniden yaz:\n\n" + noteContent,
                "Sen profesyonel bir metin yazarısın. Verilen metnin anlamını koruyarak daha saygın, kurumsal ve akıcı hale getirirsin."
            )
            AiAction.REWRITE_CASUAL -> Pair(
                "Aşağıdaki notu samimi, konuşma diline yakın ve akıcı bir üslupla yeniden yaz:\n\n" + noteContent,
                "Sen samimi ve akıcı dilde uzman bir asistansın. Cümleleri daha dinamik ve sıcak hale getirirsin."
            )
            AiAction.REWRITE_CONCISE -> Pair(
                "Aşağıdaki nottaki gereksiz laf kalabalıklarını temizle, metni mümkün olduğunca kısa, öz ve vurucu hale getir:\n\n" + noteContent,
                "Sen minimalist bir editörsün. Anlam kaybı olmadan en kısa ve öz ifadeyi üretirsin."
            )
            AiAction.EXTRACT_ACTIONS -> Pair(
                "Aşağıdaki metinden yapılması gereken işleri, görevleri ve aksiyon adımlarını tespit et. Her maddeyi Markdown formatında to-do listesi olarak ver (Örn: - [ ] Görev):\n\n" + noteContent,
                "Sen görev ve zaman yönetimi uzmanısın. Metinlerdeki tüm eylemleri eksiksiz tespit edip '- [ ] ' formatında listelersin."
            )
            AiAction.AUTO_TITLE_TAGS -> Pair(
                "Aşağıdaki not için 1 satırda en uygun Başlığı ve altındaki satırda # ile başlayan en fazla 3-4 etiketi yaz:\n\n" + noteContent,
                "Sen not organizasyonu uzmanısın. İlk satıra sadece başlığı, ikinci satıra Etiketler: #etiket1 #etiket2 formatında yanıt ver."
            )
            AiAction.FIX_GRAMMAR -> Pair(
                "Aşağıdaki metindeki yazım hatalarını, dilbilgisi yanlışlarını ve noktalama işareti eksikliklerini düzelt. Sadece düzeltilmiş metni yaz:\n\n" + noteContent,
                "Sen titiz bir Türkçe dil editörüsün. İmla, gramer ve noktalama hatalarını eksiksiz düzeltirsin."
            )
            AiAction.TRANSLATE -> Pair(
                "Aşağıdaki notu İngilizce diline çevir. Sadece çevrilmiş metni yaz, ek açıklama yapma:\n\n" + noteContent,
                "Sen profesyonel bir çeviri asistanısın. Doğal ve akıcı çeviriler yaparsın."
            )
            AiAction.CONTINUE_WRITING -> Pair(
                "Aşağıdaki metnin devamını, aynı üslup ve bağlamda yaz. Yarıda kalmış cümleleri tamamla ve metni doğal bir şekilde genişlet:\n\n" + noteContent,
                "Sen yaratıcı bir yazar asistanısın. Verilen metnin bağlamını ve üslubunu koruyarak anlamlı şekilde devam ettirirsin."
            )
            AiAction.FLASHCARDS -> Pair(
                "Aşağıdaki not içeriğinden çalışma ve tekrar amaçlı 3-5 adet Soru ve Cevap (Flashcard) çıkar. Formatı şu şekilde yap:\nSoru 1: ...\nCevap 1: ...\n\n" + noteContent,
                "Sen bir eğitmen ve çalışma koçusun. Notlardan en kritik bilgileri soru-cevap kartlarına dönüştürürsün."
            )
            AiAction.MINDMAP -> Pair(
                "Aşağıdaki notu hiyerarşik bir Zihin Haritası (Kavram Ağacı) şeklinde düzenle. Ana kavramdan alt dallara doğru girintili (indent) liste olarak sun:\n\n" + noteContent,
                "Sen görsel düşünme ve kavram haritası uzmanısın. Bilgiyi hiyerarşik ve temiz bir zihin haritasına dönüştürürsün."
            )
            AiAction.EXTRACT_REMINDERS -> Pair(
                "Aşağıdaki nottaki tüm randevu, tarih, saat, son teslim tarihi ve eylem planlarını tespit et. Her birini saat/tarih ve yapılacak iş olarak açıkça listele:\n\n" + noteContent,
                "Sen bir kişisel takvim asistanısın. Metinlerdeki zaman belirteçlerini ve görevleri eksiksiz çıkarırsın."
            )
        }

        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun suggestTitleAndTags(noteContent: String): Result<TitleAndTagsResult> {
        if (prefs.getActiveAiProvider() == AiProvider.GEMINI_NANO) {
            return onDeviceClient.suggestTitleAndTags(noteContent)
        }

        val prompt = "Aşağıdaki not içeriğini incele. İlk satıra sadece önerdiğin kısa ve etkili başlığı yaz. İkinci satıra aralarında boşluk olan ve '#' ile başlayan 2 ila 4 etiket yaz. Başka hiçbir açıklama yapma.\n\n" + noteContent
        val systemPrompt = "Format kesinlikle şöyle olmalıdır:\nÖrnek Başlık\n#iş #proje #toplantı"

        val responseResult = sendPrompt(prompt, systemPrompt)
        return responseResult.map { rawText ->
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

    suspend fun chatWithNote(
        noteContent: String,
        userQuestion: String,
        chatHistory: List<ChatMessage>
    ): Result<String> {
        if (prefs.getActiveAiProvider() == AiProvider.GEMINI_NANO) {
            return onDeviceClient.chatWithNote(noteContent, userQuestion)
        }

        val systemPrompt = "Sen kullanıcının notu üzerinde sohbet eden zeki bir not asistanısın.\n" +
            "Kullanıcının üzerinde çalıştığı notun içeriği aşağıdadır:\n" +
            "---\n" + noteContent + "\n---\n" +
            "Yalnızca bu notun içeriğini ve genel zekanı kullanarak kullanıcının sorularını Türkçe, samimi ve son derece yardımcı bir dille yanıtla."

        val historyPairs = chatHistory.map { msg ->
            val role = if (msg.role == MessageRole.USER) "user" else "assistant"
            Pair(role, msg.content)
        }

        return sendPrompt(userQuestion, systemPrompt, historyPairs)
    }

    suspend fun translateNote(noteContent: String, targetLanguage: String): Result<String> {
        if (prefs.getActiveAiProvider() == AiProvider.GEMINI_NANO) {
            return onDeviceClient.translate(noteContent, targetLanguage)
        }
        val prompt = "Aşağıdaki notu " + targetLanguage + " diline çevir. Sadece çevrilmiş metni yaz, ek açıklama yapma:\n\n" + noteContent
        val systemPrompt = "Sen profesyonel bir çeviri asistanısın. Doğal ve akıcı çeviriler yaparsın."
        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun fixGrammar(noteContent: String): Result<String> {
        if (prefs.getActiveAiProvider() == AiProvider.GEMINI_NANO) {
            return onDeviceClient.fixGrammar(noteContent)
        }
        val prompt = "Aşağıdaki metindeki yazım hatalarını, dilbilgisi yanlışlarını ve noktalama işareti eksikliklerini düzelt. Sadece düzeltilmiş metni yaz:\n\n" + noteContent
        val systemPrompt = "Sen titiz bir Türkçe dil editörüsün. İmla, gramer ve noktalama hatalarını eksiksiz düzeltirsin."
        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun continueWriting(noteContent: String): Result<String> {
        if (prefs.getActiveAiProvider() == AiProvider.GEMINI_NANO) {
            return onDeviceClient.continueWriting(noteContent)
        }
        val prompt = "Aşağıdaki metnin devamını, aynı üslup ve bağlamda yaz. Yarıda kalmış cümleleri tamamla ve metni doğal bir şekilde genişlet:\n\n" + noteContent
        val systemPrompt = "Sen yaratıcı bir yazar asistanısın. Verilen metnin bağlamını ve üslubunu koruyarak anlamlı şekilde devam ettirirsin."
        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun chatWithAllNotes(
        allNotes: List<com.applenotes.ai.domain.model.Note>,
        userQuestion: String,
        chatHistory: List<ChatMessage>
    ): Result<String> {
        if (prefs.getActiveAiProvider() == AiProvider.GEMINI_NANO) {
            return onDeviceClient.chatWithAllNotes(allNotes, userQuestion)
        }

        val notesContext = allNotes.take(20).joinToString("\n\n---\n") { note ->
            "Başlık: ${note.title}\nEtiketler: ${note.tags.joinToString(", ")}\nİçerik: ${note.content.take(600)}"
        }

        val systemPrompt = "Sen kullanıcının tüm not arşivini tarayan Genel Kişisel Zeka Asistanısın (Global AI Assistant).\n" +
            "Kullanıcının veritabanındaki kayıtlı notlar aşağıdadır:\n" +
            "====================\n" +
            notesContext + "\n" +
            "====================\n" +
            "Kullanıcının sorusunu bu notlardaki bilgileri çapraz tarayarak, Türkçe, son derece net ve yardımcı bir dille yanıtla. Eğer ilgili not varsa başlığını da belirt."

        val historyPairs = chatHistory.map { msg ->
            val role = if (msg.role == MessageRole.USER) "user" else "assistant"
            Pair(role, msg.content)
        }

        return sendPrompt(userQuestion, systemPrompt, historyPairs)
    }

    private suspend fun sendPrompt(
        prompt: String,
        systemPrompt: String?,
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> {
        return when (prefs.getActiveAiProvider()) {
            AiProvider.GEMINI -> {
                geminiClient.generateContent(
                    apiKey = prefs.geminiApiKey,
                    model = prefs.geminiModel,
                    prompt = prompt,
                    systemPrompt = systemPrompt,
                    history = history
                )
            }
            AiProvider.VERTEX_AI -> {
                vertexAiClient.generateContent(
                    projectId = prefs.vertexProjectId,
                    region = prefs.vertexRegion,
                    apiKeyOrToken = prefs.vertexApiKey,
                    model = prefs.vertexModel,
                    prompt = prompt,
                    systemPrompt = systemPrompt,
                    history = history
                )
            }
            AiProvider.OPENAI -> {
                openAiClient.generateChatCompletion(
                    apiKey = prefs.openAiApiKey,
                    model = prefs.openAiModel,
                    prompt = prompt,
                    systemPrompt = systemPrompt,
                    history = history,
                    baseUrl = "https://api.openai.com/v1/chat/completions"
                )
            }
            AiProvider.CLAUDE -> {
                claudeClient.generateMessage(
                    apiKey = prefs.claudeApiKey,
                    model = prefs.claudeModel,
                    prompt = prompt,
                    systemPrompt = systemPrompt,
                    history = history
                )
            }
            AiProvider.OPENROUTER -> {
                openAiClient.generateChatCompletion(
                    apiKey = prefs.openRouterApiKey,
                    model = prefs.openRouterModel,
                    prompt = prompt,
                    systemPrompt = systemPrompt,
                    history = history,
                    baseUrl = "https://openrouter.ai/api/v1/chat/completions"
                )
            }
            AiProvider.GROQ -> {
                openAiClient.generateChatCompletion(
                    apiKey = prefs.groqApiKey,
                    model = prefs.groqModel,
                    prompt = prompt,
                    systemPrompt = systemPrompt,
                    history = history,
                    baseUrl = "https://api.groq.com/openai/v1/chat/completions"
                )
            }
            AiProvider.GEMINI_NANO -> {
                val fullPrompt = if (systemPrompt != null) "$systemPrompt\n\n$prompt" else prompt
                onDeviceClient.generateText(fullPrompt)
            }
        }
    }

    suspend fun transcribeAudio(audioFile: java.io.File): Result<String> {
        val activeProvider = prefs.getActiveAiProvider()
        return when {
            activeProvider == AiProvider.GROQ || (activeProvider != AiProvider.OPENAI && activeProvider != AiProvider.GEMINI && prefs.groqApiKey.isNotBlank()) -> {
                openAiClient.transcribeAudio(
                    apiKey = prefs.groqApiKey,
                    audioFile = audioFile,
                    baseUrl = "https://api.groq.com/openai/v1/audio/transcriptions",
                    model = "whisper-large-v3"
                )
            }
            activeProvider == AiProvider.OPENAI || prefs.openAiApiKey.isNotBlank() -> {
                openAiClient.transcribeAudio(
                    apiKey = prefs.openAiApiKey,
                    audioFile = audioFile,
                    baseUrl = "https://api.openai.com/v1/audio/transcriptions",
                    model = "whisper-1"
                )
            }
            activeProvider == AiProvider.GEMINI || prefs.geminiApiKey.isNotBlank() -> {
                geminiClient.transcribeAudio(
                    apiKey = prefs.geminiApiKey,
                    audioBytes = audioFile.readBytes(),
                    mimeType = "audio/mp4"
                )
            }
            else -> Result.failure(IllegalStateException("Ses transkripsiyonu için Gemini, Groq veya OpenAI API anahtarı gereklidir."))
        }
    }

    suspend fun extractTextFromImage(imageBytes: ByteArray): Result<String> {
        val activeProvider = prefs.getActiveAiProvider()
        return when {
            activeProvider == AiProvider.GEMINI || prefs.geminiApiKey.isNotBlank() -> {
                geminiClient.extractTextFromImage(
                    apiKey = prefs.geminiApiKey,
                    imageBytes = imageBytes,
                    mimeType = "image/jpeg"
                )
            }
            else -> Result.failure(IllegalStateException("Belgeden/Görselden metin çıkarma (OCR) için Google Gemini API anahtarı gereklidir. Lütfen Ayarlar bölümünden anahtarınızı kaydedin."))
        }
    }

    suspend fun generateMorningDigest(notes: List<Note>): Result<String> {
        val notesContext = notes.take(25).joinToString("\n---\n") { note ->
            val icon = note.icon?.let { "$it " } ?: ""
            "Başlık: $icon${note.title}\nİçerik: ${note.content.take(200)}\nEtiketler: ${note.tags.joinToString(", ")}"
        }

        val prompt = "Aşağıdaki notlar ve yapılacaklar listesini inceleyerek kullanıcıya enerjik, şık ve son derece motive edici bir 'Günlük Sabah Brifingi' hazırla.\n\n" +
            "Notlar Arşivi:\n" + notesContext + "\n\n" +
            "Brifing şu bölümleri içersin:\n" +
            "1. ☀️ Günün Özeti & Selamlama\n" +
            "2. 🎯 Bugünün En Kritik 3 Hedefi\n" +
            "3. 📋 Bekleyen Yapılacaklar & Hatırlatıcılar\n" +
            "4. 💡 Günün İlham Verici Sözü"

        val systemPrompt = "Sen kullanıcının kişisel Notism asistanısın. Türkçe, samimi, profesyonel, ilham verici ve net bir sabah brifingi üretirsin."
        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun synthesizeNotes(notes: List<Note>): Result<String> {
        if (notes.isEmpty()) return Result.failure(IllegalArgumentException("Sentezlemek için en az bir not seçilmelidir."))

        val notesContext = notes.joinToString("\n\n====================\n\n") { note ->
            "BAŞLIK: ${note.title}\nİÇERİK:\n${note.content}"
        }

        val prompt = "Aşağıda kullanıcının seçtiği ${notes.size} adet not yer almaktadır. Bu notları derinlemesine analiz et ve kapsamlı bir 'Sentez & Yönetici Master Raporu' oluştur.\n\n" +
            notesContext + "\n\n" +
            "Format:\n" +
            "# 📑 Çoklu Not Sentez Raporu\n" +
            "## 📌 Ortak Temalar & Ana Fikirler\n" +
            "## 💡 Alınan Kritik Kararlar\n" +
            "## 🚀 Birleşik Aksiyon & Görev Listesi (- [ ] formatında)\n" +
            "## 🔍 Riskler ve Sonraki Adımlar"

        val systemPrompt = "Sen üst düzey bir strateji ve not sentezi uzmanısın. Birden fazla nottaki dağınık bilgileri birleştirip kusursuz bir master rapora dönüştürürsün."
        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun semanticSearch(notes: List<Note>, query: String): Result<String> {
        if (notes.isEmpty() || query.isBlank()) return Result.success("Aranacak not bulunamadı.")

        val notesContext = notes.take(30).joinToString("\n---\n") { note ->
            "ID: ${note.id} | Başlık: ${note.title}\nİçerik: ${note.content.take(250)}"
        }

        val prompt = "Kullanıcının doğal dille sorduğu soru şudur:\n\"$query\"\n\n" +
            "Aşağıdaki not arşivini incele. Kullanıcının sorusuna doğrudan cevap ver ve cevabın hangi nottan (başlığını belirterek) alındığını açıkla:\n\n" +
            notesContext

        val systemPrompt = "Sen anlamsal zeka arama motorusun (Semantic Search Engine). Kullanıcının sorusunu en alakalı notlarla eşleştirir, cevabı özetler ve kaynak notları gösterirsin."
        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun generateMeetingMinutes(transcription: String): Result<String> {
        val prompt = "Aşağıdaki ses/konuşma/ders transkripsiyonunu analiz et ve son derece profesyonel, şık ve yapılandırılmış bir 'Toplantı & Ders Tutanağı' hazırla.\n\n" +
            "TRANSKRİPSİYON:\n$transcription\n\n" +
            "Tutanağı kesinlikle şu Markdown başlıklarıyla sun:\n" +
            "# 📋 Toplantı / Ders Tutanağı\n" +
            "## 🎯 Konu & Amaç\n" +
            "## 👥 Katılımcılar & Önemli Görüşler\n" +
            "## 📝 Yönetici Özeti (Executive Summary)\n" +
            "## 💡 Alınan Temel Kararlar\n" +
            "## ✅ Aksiyon Planı & Görev Dağılımı (- [ ] formatında)\n" +
            "## 📅 Bir Sonraki Adımlar / Takvim"

        val systemPrompt = "Sen üst düzey bir kurumsal yönetici asistanı ve akademik raportörsün. Dağınık konuşma kayıtlarını kusursuz, organize ve eyleme dönüştürülebilir tutanaklara dönüştürürsün."
        return sendPrompt(prompt, systemPrompt)
    }
}

