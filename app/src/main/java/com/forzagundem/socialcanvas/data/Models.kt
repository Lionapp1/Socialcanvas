package com.forzagundem.socialcanvas.data

enum class DesignStyle { DARK_CINEMATIC, EDITORIAL_LIGHT }

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
    val designStyle: DesignStyle = if (template == TemplateType.ISTATISTIK) DesignStyle.EDITORIAL_LIGHT else DesignStyle.DARK_CINEMATIC,
    val ratio: AspectRatio = AspectRatio.INSTAGRAM_45,
    val headline: String = TemplateType.TRANSFER.defaultHeadline,
    val subtitle: String = TemplateType.TRANSFER.defaultSub,
    val darken: Float = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> 0.58f; TemplateType.SKOR -> 0.42f; TemplateType.ALINTI -> 0.30f; else -> 0.40f },
    val showSafeArea: Boolean = false,
    val imageUri: String? = null,
    val subjectUri: String? = null,
    val subjectX: Float = 0.20f,
    val subjectY: Float = 0.02f,
    val subjectScale: Float = 1.08f,
    val subjectOnTop: Boolean = true,
    val imageX: Float = 0f,
    val imageY: Float = 0f,
    val imageScale: Float = 1f,
    val headlineSize: Float = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> 31f; TemplateType.SKOR, TemplateType.ISTATISTIK -> 52f; else -> 34f },
    val subtitleSize: Float = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> 12f; TemplateType.SKOR, TemplateType.ISTATISTIK -> 14f; else -> 17f },
    val headlineColor: Long = when (template) { TemplateType.TRANSFER, TemplateType.HABER, TemplateType.SKOR -> 0xFFFFFFFF; else -> 0xFFFFB700 },
    val subtitleColor: Long = 0xFFFFFFFF,
    val font: CanvasFont = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> CanvasFont.CONDENSED; TemplateType.ALINTI -> CanvasFont.SERIF; else -> CanvasFont.BOLD },
    val textAlign: TextAlignMode = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> TextAlignMode.LEFT; else -> TextAlignMode.CENTER },
    val textX: Float = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> -0.27f; else -> 0f },
    val textY: Float = when (template) { TemplateType.TRANSFER, TemplateType.HABER -> 0.10f; TemplateType.SKOR -> 0.18f; TemplateType.ALINTI -> 0.20f; else -> 0.28f },
    val textShadow: Boolean = true,
    val statOne: Float = 0.62f,
    val statTwo: Float = 0.78f,
    val statThree: Float = 0.44f,
    val homeTeamName: String = "EV SAHİBİ",
    val awayTeamName: String = "DEPLASMAN",
    val homeTeamLogoUri: String? = null,
    val awayTeamLogoUri: String? = null,
    val matchScore: String = "0 - 0",
    val showMatchScore: Boolean = false,
    val showLineup: Boolean = false,
    val formation: String = "4-3-3",
    val lineupPlayers: String = "Kaleci\\nSağ Bek\\nStoper\\nStoper\\nSol Bek\\nÖn Libero\\nMerkez Orta Saha\\nMerkez Orta Saha\\nSağ Kanat\\nSantrfor\\nSol Kanat",
    val logoScale: Float = 1f,
    val stickerUri: String? = null,
    val stickerScale: Float = 1f,
    val stickerX: Float = 0f,
    val stickerY: Float = 0f
)


/**
 * Applies a complete, template-aware art direction while preserving the user's copy,
 * selected photo, and canvas ratio. This is the one-tap professional layout reset.
 */
fun EditorState.applyProfessionalLayout(): EditorState = when (template) {
    TemplateType.TRANSFER -> copy(
        designStyle = DesignStyle.DARK_CINEMATIC, darken = 0.52f,
        subjectX = 0.24f, subjectY = 0.02f, subjectScale = 1.12f, subjectOnTop = true,
        headlineSize = 34f, subtitleSize = 13f, font = CanvasFont.CONDENSED,
        textAlign = TextAlignMode.LEFT, textX = -0.25f, textY = 0.10f,
        headlineColor = 0xFFFFFFFF, subtitleColor = 0xFFFFD36A, textShadow = true
    )
    TemplateType.HABER -> copy(
        designStyle = DesignStyle.DARK_CINEMATIC, darken = 0.58f,
        subjectX = 0.18f, subjectY = 0.02f, subjectScale = 1.08f, subjectOnTop = true,
        headlineSize = 31f, subtitleSize = 14f, font = CanvasFont.BOLD,
        textAlign = TextAlignMode.LEFT, textX = -0.25f, textY = 0.12f,
        headlineColor = 0xFFFFFFFF, subtitleColor = 0xFFFFC52E, textShadow = true
    )
    TemplateType.SKOR -> copy(
        designStyle = DesignStyle.DARK_CINEMATIC, darken = 0.34f,
        subjectX = 0.20f, subjectY = 0.08f, subjectScale = 1.08f, subjectOnTop = true,
        headlineSize = 58f, subtitleSize = 15f, font = CanvasFont.BOLD,
        textAlign = TextAlignMode.CENTER, textX = 0f, textY = 0.18f,
        headlineColor = 0xFFFFFFFF, subtitleColor = 0xFFFFC52E, textShadow = true
    )
    TemplateType.ALINTI -> copy(
        designStyle = DesignStyle.EDITORIAL_LIGHT, darken = 0f,
        subjectX = 0.12f, subjectY = 0.04f, subjectScale = 1.04f, subjectOnTop = false,
        headlineSize = 35f, subtitleSize = 16f, font = CanvasFont.SERIF,
        textAlign = TextAlignMode.CENTER, textX = 0f, textY = 0.18f,
        headlineColor = 0xFF252329, subtitleColor = 0xFF8F1538, textShadow = false
    )
    TemplateType.THREADS -> copy(
        designStyle = DesignStyle.DARK_CINEMATIC, darken = 0.38f,
        subjectX = 0.18f, subjectY = 0.02f, subjectScale = 1.05f, subjectOnTop = true,
        headlineSize = 32f, subtitleSize = 15f, font = CanvasFont.MODERN,
        textAlign = TextAlignMode.LEFT, textX = -0.18f, textY = 0.12f,
        headlineColor = 0xFFFFFFFF, subtitleColor = 0xFFE7E7E7, textShadow = true
    )
    TemplateType.ISTATISTIK -> copy(
        designStyle = DesignStyle.EDITORIAL_LIGHT, darken = 0f,
        subjectX = 0.24f, subjectY = -0.02f, subjectScale = 1.08f, subjectOnTop = true,
        headlineSize = 48f, subtitleSize = 14f, font = CanvasFont.BOLD,
        textAlign = TextAlignMode.CENTER, textX = 0f, textY = -0.22f,
        headlineColor = 0xFF8F1538, subtitleColor = 0xFF242424, textShadow = false
    )
    TemplateType.BOS -> copy(
        designStyle = DesignStyle.DARK_CINEMATIC, darken = 0.40f,
        subjectX = 0.18f, subjectY = 0.02f, subjectScale = 1f, subjectOnTop = true,
        headlineSize = 36f, subtitleSize = 16f, font = CanvasFont.MODERN,
        textAlign = TextAlignMode.CENTER, textX = 0f, textY = 0.18f,
        headlineColor = 0xFFFFFFFF, subtitleColor = 0xFFFFC52E, textShadow = true
    )
}
