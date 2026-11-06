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
        val firstLine = lines.firstOrNull().orEmpty()
        val sentences = firstLine.split(Regex("(?<=[.!?;])\\\\s+|\\\\s+[•|]\\\\s+"))
            .map { it.trim().trimEnd('.', ';') }.filter { it.isNotBlank() }
        val score = Regex("\\\\b([0-9]{1,2})\\\\s*[-:]\\\\s*([0-9]{1,2})\\\\b").find(source)
        val scoreTail = if (score != null) firstLine.substring(score.range.last + 1).trim().trimStart(',', ':', ' ') else ""
        val scoreTailParts = scoreTail.split(',').map { it.trim() }.filter { it.isNotBlank() }
        val title = when {
            score != null && template == TemplateType.SKOR -> {
                val prefix = firstLine.substring(0, score.range.first).trim().trimEnd(',', ':')
                val scoreSuffix = scoreTailParts.firstOrNull().orEmpty()
                when {
                    prefix.isNotBlank() && scoreSuffix.isNotBlank() -> "$prefix ${score.value} $scoreSuffix".take(92)
                    prefix.isNotBlank() -> "$prefix ${score.value}".take(92)
                    else -> score.value
                }
            }
            sentences.isNotEmpty() -> sentences.first().take(92)
            else -> source.take(92)
        }
        val remainder = when {
            score != null && template == TemplateType.SKOR && scoreTailParts.size > 1 -> scoreTailParts.drop(1).joinToString(", ")
            lines.size > 1 -> lines.drop(1).joinToString(" ")
            firstLine.length > title.length -> firstLine.removePrefix(title).trim().trimStart('-', '—', ':', '.', ',', ' ')
            sentences.size > 1 -> sentences.drop(1).joinToString(" ")
            else -> ""
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
        val percentages = Regex("(\\d{1,3})\\s*%").findAll(source)
            .mapNotNull { it.groupValues.getOrNull(1)?.toFloatOrNull()?.div(100f) }
            .map { it.coerceIn(0.02f, 1f) }.toList()
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
            subjectScale = current.subjectScale,
            statOne = percentages.getOrNull(0) ?: current.statOne,
            statTwo = percentages.getOrNull(1) ?: current.statTwo,
            statThree = percentages.getOrNull(2) ?: current.statThree
        ).applyProfessionalLayout().let { layout ->
            val titleSize = when (template) {
                TemplateType.ISTATISTIK -> when { title.length <= 14 -> 48f; title.length <= 30 -> 38f; else -> 29f }
                TemplateType.SKOR -> if (title.length <= 8) 58f else 42f
                TemplateType.ALINTI -> when { title.length <= 55 -> 35f; title.length <= 100 -> 29f; else -> 23f }
                else -> when { title.length <= 34 -> layout.headlineSize; title.length <= 62 -> layout.headlineSize - 5f; else -> layout.headlineSize - 9f }
            }
            val subSize = when { subtitle.length > 150 -> 10f; subtitle.length > 90 -> 11f; subtitle.length > 45 -> 12f; else -> layout.subtitleSize }
            layout.copy(headlineSize = titleSize, subtitleSize = subSize)
        }

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
