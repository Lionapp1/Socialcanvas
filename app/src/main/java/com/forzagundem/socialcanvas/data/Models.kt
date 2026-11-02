package com.forzagundem.socialcanvas.data

enum class AspectRatio(val label: String, val widthRatio: Float, val heightRatio: Float, val platform: String) {
    INSTAGRAM_45("Instagram gönderi 4:5", 4f, 5f, "Instagram akış gönderisi"),
    SQUARE("Kare 1:1", 1f, 1f, "Instagram / Threads kare"),
    STORY("Story / Reels 9:16", 9f, 16f, "Instagram hikâye ve Reels"),
    THREADS_45("Threads 4:5", 4f, 5f, "Threads dikey görsel"),
    LANDSCAPE("Yatay 16:9", 16f, 9f, "Threads / X / YouTube"),
    PINTEREST("Pinterest 2:3", 2f, 3f, "Pinterest pin");

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
    ISTATISTIK("İnfografik", "İstatistik ve veri görseli", "▥", "62%", "TOPA SAHİP OLMA"),
    BOS("Boş Tuval", "Sıfırdan tasarım", "✨", "BAŞLIK", "Alt metin buraya")
}

enum class CanvasFont(val label: String) {
    MODERN("Modern"),
    BOLD("Kalın"),
    SERIF("Serif"),
    MONO("Mono"),
    CONDENSED("Dar")
}

enum class TextAlignMode(val label: String) {
    LEFT("Sol"),
    CENTER("Orta"),
    RIGHT("Sağ")
}

data class EditorState(
    val template: TemplateType = TemplateType.TRANSFER,
    val ratio: AspectRatio = AspectRatio.INSTAGRAM_45,
    val headline: String = TemplateType.TRANSFER.defaultHeadline,
    val subtitle: String = TemplateType.TRANSFER.defaultSub,
    val darken: Float = 0.40f,
    val showSafeArea: Boolean = true,
    val imageUri: String? = null,
    val subjectUri: String? = null,
    val subjectX: Float = 0f,
    val subjectY: Float = 0.08f,
    val subjectScale: Float = 1f,
    val subjectOnTop: Boolean = true,
    val imageX: Float = 0f,
    val imageY: Float = 0f,
    val imageScale: Float = 1f,
    val headlineSize: Float = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> 31f; TemplateType.SKOR, TemplateType.ISTATISTIK -> 52f; else -> 34f },
    val subtitleSize: Float = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> 12f; TemplateType.SKOR, TemplateType.ISTATISTIK -> 14f; else -> 17f },
    val headlineColor: Long = when (template) { TemplateType.TRANSFER, TemplateType.HABER, TemplateType.SKOR -> 0xFFFFFFFF; else -> 0xFFFFB700 },
    val subtitleColor: Long = 0xFFFFFFFF,
    val font: CanvasFont = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> CanvasFont.CONDENSED; TemplateType.ALINTI -> CanvasFont.SERIF; else -> CanvasFont.BOLD },
    val textAlign: TextAlignMode = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> TextAlignMode.RIGHT; else -> TextAlignMode.CENTER },
    val textX: Float = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> 0.22f; else -> 0f },
    val textY: Float = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> 0.02f; TemplateType.SKOR -> 0.18f; else -> 0.28f },
    val textShadow: Boolean = true,
    val statOne: Float = 0.62f,
    val statTwo: Float = 0.78f,
    val statThree: Float = 0.44f
)
