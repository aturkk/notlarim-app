package com.applenotes.ai.data.remote.ai

import android.content.Context
import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.domain.model.AiAction
import com.applenotes.ai.domain.model.AiProvider
import com.applenotes.ai.domain.model.ChatMessage
import com.applenotes.ai.domain.model.MessageRole
import com.applenotes.ai.domain.model.TitleAndTagsResult

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
}
