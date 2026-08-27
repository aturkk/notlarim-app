package com.applenotes.ai.domain.model

enum class AiAction(val title: String, val description: String, val icon: String) {
    SUMMARIZE("Özet Çıkar", "Kilit noktaları ve ana fikri maddelerle özetle", "summary"),
    REWRITE_PROFESSIONAL("Profesyonel Yap", "Resmi ve kurumsal bir dille yeniden yaz", "work"),
    REWRITE_CASUAL("Samimi Yap", "Daha sıcak ve anlaşılır bir dille düzenle", "chat"),
    REWRITE_CONCISE("Sadeleştir & Kısalt", "Gereksiz detayları çıkarıp özünü koru", "compress"),
    EXTRACT_ACTIONS("Yapılacaklar Çıkar", "Eylem maddelerini to-do kontrol listesine dönüştür", "checklist"),
    AUTO_TITLE_TAGS("Başlık ve Etiket Öner", "Notun içeriğine uygun başlık ve #etiketler üret", "tag"),
    FIX_GRAMMAR("Yazım Hatalarını Düzelt", "İmla, dilbilgisi ve noktalama hatalarını düzelt", "spellcheck"),
    TRANSLATE("Çevir", "Notu farklı bir dile çevir", "translate"),
    CONTINUE_WRITING("Yazmaya Devam Et", "Metnin devamını AI ile tamamla", "edit_note")
}

data class ChatMessage(
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

data class TitleAndTagsResult(
    val title: String,
    val tags: List<String>
)
