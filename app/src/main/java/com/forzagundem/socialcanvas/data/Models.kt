package com.forzagundem.socialcanvas.data

enum class AspectRatio(val label: String, val widthRatio: Float, val heightRatio: Float) {
    INSTAGRAM_45("4:5 Instagram", 4f, 5f),
    SQUARE("1:1 Post", 1f, 1f),
    STORY("9:16 Story", 9f, 16f),
    THREADS("16:9 Threads / X", 16f, 9f);

    val aspect: Float get() = widthRatio / heightRatio
}

enum class TemplateType(
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val defaultHeadline: String,
    val defaultSub: String
) {
    HABER("Haber", "Son dakika ve haber görseli", "📰", "SON DAKİKA", "Önemli gelişme yaşandı"),
    TRANSFER("Transfer", "Futbol transfer duyurusu", "🔄", "TRANSFER GELİŞMESİ", "Kulüp görüşmelere başladı"),
    SKOR("Skor", "Maç sonucu ve skor kartı", "⚽", "3 - 1", "MAÇ SONU"),
    ALINTI("Alıntı", "Söz ve alıntı kartı", "💬", "\"Başarı tesadüf değildir.\"", "— Anonim"),
    THREADS("Threads / X", "Sosyal medya paylaşımı", "𝕏", "Bugün neler oldu?", "Kısa ve net"),
    BOS("Boş Tuval", "Sıfırdan tasarım", "✨", "BAŞLIK", "Alt metin buraya")
}

data class EditorState(
    val template: TemplateType = TemplateType.TRANSFER,
    val ratio: AspectRatio = AspectRatio.INSTAGRAM_45,
    val headline: String = TemplateType.TRANSFER.defaultHeadline,
    val subtitle: String = TemplateType.TRANSFER.defaultSub,
    val darken: Float = 0.40f,
    val focusPerson: Boolean = false,
    val textBehind: Boolean = false,
    val showSafeArea: Boolean = true,
    val imageUri: String? = null
)
