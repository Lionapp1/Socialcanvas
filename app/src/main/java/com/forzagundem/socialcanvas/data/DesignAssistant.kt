package com.forzagundem.socialcanvas.data

/**
 * Offline-first content director. It classifies the supplied copy and builds a
 * template-aware layout without requiring an API key or sending text anywhere.
 * A remote generative provider can later replace this strategy behind the same API.
 */
data class AssistantPlan(
    val state: EditorState,
    val explanation: String,
    val templateName: String
)

object DesignAssistant {
    fun createPlan(rawText: String, current: EditorState): AssistantPlan {
        val source = rawText.trim()
        require(source.isNotBlank()) { "Önce tasarlanacak metni yaz." }

        val normalized = source.lowercase()
        val template = when {
            listOf("transfer", "imza", "bonservis", "kiralık", "kiralik", "anlaşma", "anlasma").any(normalized::contains) ->
                TemplateType.TRANSFER
            Regex("\\b[0-9]{1,2}\\s*[-:]\\s*[0-9]{1,2}\\b").containsMatchIn(source) ||
                listOf("maç sonucu", "mac sonucu", "skor", "karşılaşma").any(normalized::contains) ->
                TemplateType.SKOR
            source.contains("%") || listOf("istatistik", "topla sahip", "pas isabet", "şut", "sut", "oran").any(normalized::contains) ->
                TemplateType.ISTATISTIK
            listOf("son dakika", "resmi açıklama", "resmi aciklama", "gelişme", "gelisme", "duyurdu", "açıklandı", "aciklandi").any(normalized::contains) ->
                TemplateType.HABER
            (source.length <= 220 && (source.contains('“') || source.contains('"') || normalized.startsWith("alıntı") || normalized.startsWith("soz"))) ->
                TemplateType.ALINTI
            else -> TemplateType.THREADS
        }

        val lines = source.lines().map { it.trim() }.filter { it.isNotBlank() }
        val title = lines.firstOrNull()?.take(92) ?: source.take(92)
        val remainder = if (lines.size > 1) lines.drop(1).joinToString(" ") else {
            source.removePrefix(title).trim().trimStart('-', '—', ':', '.', ' ')
        }
        val subtitle = remainder.take(220).ifBlank {
            when (template) {
                TemplateType.TRANSFER -> "Transfer gelişmesi"
                TemplateType.SKOR -> "MAÇ SONUCU"
                TemplateType.HABER -> "Günün öne çıkan gelişmesi"
                TemplateType.ISTATISTIK -> "İSTATİSTİKLER"
                TemplateType.ALINTI -> "—"
                TemplateType.THREADS -> " "
                TemplateType.BOS -> ""
            }
        }

        val ratio = when (template) {
            TemplateType.THREADS -> AspectRatio.THREADS_45
            else -> AspectRatio.INSTAGRAM_45
        }
        val planned = EditorState(
            template = template,
            ratio = ratio,
            headline = title,
            subtitle = subtitle,
            imageUri = current.imageUri,
            subjectUri = current.subjectUri,
            imageX = current.imageX,
            imageY = current.imageY,
            imageScale = current.imageScale,
            subjectX = current.subjectX,
            subjectY = current.subjectY,
            subjectScale = current.subjectScale
        ).applyProfessionalLayout()

        val explanation = when (template) {
            TemplateType.TRANSFER -> "Transfer dili algılandı. Güçlü başlık, koyu fotoğraf ve futbol duyurusu düzeni seçildi."
            TemplateType.SKOR -> "Skor veya maç sonucu algılandı. Skoru öne çıkaran merkezî maç kartı seçildi."
            TemplateType.ISTATISTIK -> "Sayısal veri algılandı. İnfografik düzeni ve veri çubukları seçildi."
            TemplateType.HABER -> "Haber dili algılandı. Son dakika / haber başlığı için editoryal düzen seçildi."
            TemplateType.ALINTI -> "Alıntı metni algılandı. Sözü öne çıkaran tipografik kart seçildi."
            TemplateType.THREADS -> "Sosyal paylaşım metni algılandı. Threads uyumlu sade ve okunaklı düzen seçildi."
            TemplateType.BOS -> "Boş tuval seçildi."
        }
        return AssistantPlan(planned, explanation, template.title)
    }
}
