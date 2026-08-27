package com.applenotes.ai.data.remote.ai

import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.domain.model.AiAction
import com.applenotes.ai.domain.model.AiProvider
import com.applenotes.ai.domain.model.ChatMessage
import com.applenotes.ai.domain.model.MessageRole
import com.applenotes.ai.domain.model.TitleAndTagsResult

class AiServiceManager(
    private val prefs: SecurePreferences
) {
    private val geminiClient = GeminiApiClient()
    private val vertexAiClient = VertexAiApiClient()
    private val openAiClient = OpenAiApiClient()
    private val claudeClient = ClaudeApiClient()

    suspend fun executeAction(action: AiAction, noteContent: String): Result<String> {
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
        }

        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun suggestTitleAndTags(noteContent: String): Result<TitleAndTagsResult> {
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
        val prompt = "Aşağıdaki notu " + targetLanguage + " diline çevir. Sadece çevrilmiş metni yaz, ek açıklama yapma:\n\n" + noteContent
        val systemPrompt = "Sen profesyonel bir çeviri asistanısın. Doğal ve akıcı çeviriler yaparsın."
        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun fixGrammar(noteContent: String): Result<String> {
        val prompt = "Aşağıdaki metindeki yazım hatalarını, dilbilgisi yanlışlarını ve noktalama işareti eksikliklerini düzelt. Sadece düzeltilmiş metni yaz:\n\n" + noteContent
        val systemPrompt = "Sen titiz bir Türkçe dil editörüsün. İmla, gramer ve noktalama hatalarını eksiksiz düzeltirsin."
        return sendPrompt(prompt, systemPrompt)
    }

    suspend fun continueWriting(noteContent: String): Result<String> {
        val prompt = "Aşağıdaki metnin devamını, aynı üslup ve bağlamda yaz. Yarıda kalmış cümleleri tamamla ve metni doğal bir şekilde genişlet:\n\n" + noteContent
        val systemPrompt = "Sen yaratıcı bir yazar asistanısın. Verilen metnin bağlamını ve üslubunu koruyarak anlamlı şekilde devam ettirirsin."
        return sendPrompt(prompt, systemPrompt)
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
        }
    }
}
