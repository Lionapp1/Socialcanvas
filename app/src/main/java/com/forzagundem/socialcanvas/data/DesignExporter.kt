package com.forzagundem.socialcanvas.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

object DesignExporter {
    suspend fun save(context: Context, state: EditorState): Uri = withContext(Dispatchers.IO) {
        val width = 2160
        val height = (width / state.ratio.aspect).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val editorial = (state.template == TemplateType.ISTATISTIK || state.template == TemplateType.ALINTI) && state.imageUri == null
        if (state.imageUri == null) {
            val colors = when (state.template) {
                TemplateType.ALINTI -> intArrayOf(Color.rgb(250, 247, 240), Color.rgb(240, 233, 220))
                TemplateType.ISTATISTIK -> intArrayOf(Color.rgb(250, 250, 248), Color.rgb(242, 241, 237))
                TemplateType.HABER -> intArrayOf(Color.rgb(23, 25, 31), Color.rgb(48, 50, 58), Color.rgb(17, 18, 22))
                TemplateType.SKOR -> intArrayOf(Color.rgb(17, 24, 32), Color.rgb(38, 49, 58), Color.rgb(17, 21, 27))
                TemplateType.TRANSFER -> intArrayOf(Color.rgb(53, 16, 31), Color.rgb(16, 13, 19), Color.rgb(87, 13, 42))
                TemplateType.THREADS -> intArrayOf(Color.rgb(32, 33, 42), Color.rgb(17, 18, 23))
                TemplateType.BOS -> intArrayOf(Color.rgb(34, 33, 42), Color.rgb(17, 17, 22))
            }
            paint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colors, null, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null
            paint.style = Paint.Style.FILL
            if (editorial) {
                paint.color = Color.argb(34, 143, 21, 56)
                canvas.drawCircle(width * 0.96f, height * 0.08f, min(width, height) * 0.34f, paint)
                paint.color = Color.argb(20, 242, 181, 27)
                canvas.drawCircle(width * 0.02f, height * 0.92f, min(width, height) * 0.22f, paint)
                paint.color = Color.rgb(143, 21, 56)
                paint.strokeWidth = 5f
                canvas.drawLine(width * 0.08f, height * 0.13f, width * 0.34f, height * 0.13f, paint)
                paint.color = Color.rgb(242, 181, 27)
                canvas.drawLine(width * 0.66f, height * 0.13f, width * 0.92f, height * 0.13f, paint)
            } else {
                paint.color = Color.argb(45, 255, 255, 255)
                paint.strokeWidth = 3f
                when (state.template) {
                    TemplateType.TRANSFER -> {
                        for (i in 0..5) canvas.drawLine(width * (0.48f + i * .10f), 0f, width * (0.18f + i * .10f), height.toFloat(), paint)
                        paint.color = Color.rgb(143, 21, 56)
                        canvas.drawRect(width * .07f, height * .12f, width * .09f, height * .74f, paint)
                    }
                    TemplateType.HABER -> {
                        paint.color = Color.rgb(143, 21, 56)
                        canvas.drawRect(0f, 0f, width.toFloat(), height * .018f, paint)
                        paint.color = Color.rgb(242, 181, 27)
                        canvas.drawRect(width * .08f, height * .17f, width * .92f, height * .174f, paint)
                    }
                    TemplateType.SKOR -> {
                        paint.color = Color.argb(24, 255, 255, 255)
                        canvas.drawRoundRect(RectF(width * .08f, height * .37f, width * .92f, height * .64f), 36f, 36f, paint)
                    }
                    TemplateType.THREADS -> {
                        paint.color = Color.argb(18, 255, 255, 255)
                        canvas.drawRoundRect(RectF(width * .07f, height * .14f, width * .93f, height * .84f), 42f, 42f, paint)
                    }
                    else -> Unit
                }
            }
        } else {
            paint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), Color.rgb(25, 25, 25), Color.rgb(8, 8, 8), Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null
        }

        if (state.imageUri == null) {
            val lion = android.graphics.Path().apply {
                moveTo(width * .50f, height * .20f)
                cubicTo(width * .40f, height * .12f, width * .31f, height * .17f, width * .28f, height * .28f)
                cubicTo(width * .19f, height * .24f, width * .16f, height * .34f, width * .23f, height * .43f)
                cubicTo(width * .16f, height * .52f, width * .22f, height * .63f, width * .32f, height * .65f)
                cubicTo(width * .35f, height * .77f, width * .44f, height * .82f, width * .50f, height * .84f)
                cubicTo(width * .56f, height * .82f, width * .65f, height * .77f, width * .68f, height * .65f)
                cubicTo(width * .78f, height * .63f, width * .84f, height * .52f, width * .77f, height * .43f)
                cubicTo(width * .84f, height * .34f, width * .81f, height * .24f, width * .72f, height * .28f)
                cubicTo(width * .69f, height * .17f, width * .60f, height * .12f, width * .50f, height * .20f)
                close()
            }
            paint.style = Paint.Style.FILL
            paint.color = if (editorial) Color.argb(14, 143, 21, 56) else Color.argb(19, 242, 181, 27)
            canvas.drawPath(lion, paint)
            canvas.drawOval(RectF(width * .37f, height * .31f, width * .63f, height * .65f), paint)
            paint.strokeWidth = max(4f, width * .006f)
            canvas.drawCircle(width * .435f, height * .455f, width * .009f, paint)
            canvas.drawCircle(width * .565f, height * .455f, width * .009f, paint)
            canvas.drawLine(width * .455f, height * .535f, width * .50f, height * .565f, paint)
            canvas.drawLine(width * .50f, height * .565f, width * .545f, height * .535f, paint)
        }

        state.imageUri?.let { raw ->
            decode(context, raw)?.let { source ->
                val baseScale = max(width.toFloat() / source.width, height.toFloat() / source.height)
                val visibleWidth = min(source.width.toFloat(), width / (baseScale * state.imageScale.coerceAtLeast(0.1f)))
                val visibleHeight = min(source.height.toFloat(), height / (baseScale * state.imageScale.coerceAtLeast(0.1f)))
                val maxLeft = (source.width - visibleWidth).coerceAtLeast(0f)
                val maxTop = (source.height - visibleHeight).coerceAtLeast(0f)
                val left = (maxLeft / 2f - state.imageX * maxLeft / 2f).coerceIn(0f, maxLeft)
                val top = (maxTop / 2f - state.imageY * maxTop / 2f).coerceIn(0f, maxTop)
                val src = Rect(left.toInt(), top.toInt(), (left + visibleWidth).toInt().coerceAtMost(source.width), (top + visibleHeight).toInt().coerceAtMost(source.height))
                canvas.drawBitmap(source, src, Rect(0, 0, width, height), paint)
                source.recycle()
            }
        }

        if (state.imageUri != null) {
            paint.color = Color.argb((state.darken.coerceIn(0f, 0.9f) * 255).toInt(), 0, 0, 0)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
        // Match the preview's cinematic top/bottom contrast treatment.
        if (state.imageUri != null) paint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(Color.argb(72, 0, 0, 0), Color.TRANSPARENT, Color.argb(24, 0, 0, 0), Color.argb(158, 0, 0, 0)),
            floatArrayOf(0f, 0.42f, 0.72f, 1f), Shader.TileMode.CLAMP
        )
        if (state.imageUri != null) canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        paint.color = Color.WHITE

        if (state.subjectUri != null) {
            val focusX = width * (0.5f + state.subjectX)
            val focusY = height * (0.50f + state.subjectY)
            val glowRadius = max(width, height) * 0.66f
            val focusGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = android.graphics.RadialGradient(
                    focusX, focusY, glowRadius,
                    intArrayOf(Color.argb(85, 255, 240, 194), Color.argb(34, 255, 181, 46), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.48f, 1f), Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), focusGlow)
            focusGlow.shader = android.graphics.RadialGradient(
                focusX, focusY, glowRadius * 1.18f,
                intArrayOf(Color.TRANSPARENT, Color.argb(24, 230, 161, 42), Color.TRANSPARENT),
                floatArrayOf(0f, 0.72f, 1f), Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), focusGlow)
        }
        if (state.template == TemplateType.ISTATISTIK) {
            val left = width * 0.12f
            val right = width * 0.88f
            val top = height * 0.70f
            val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = if (editorial) Color.rgb(225, 225, 225) else Color.argb(46, 255, 255, 255); strokeWidth = 18f; strokeCap = Paint.Cap.ROUND }
            val bar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(143, 21, 56); strokeWidth = 18f; strokeCap = Paint.Cap.ROUND }
            listOf(state.statOne, state.statTwo, state.statThree).forEachIndexed { index, fraction ->
                val y = top + index * 42f
                canvas.drawLine(left, y, right, y, track)
                bar.color = if (index % 2 == 0) Color.rgb(143, 21, 56) else Color.rgb(242, 181, 27)
                canvas.drawLine(left, y, left + (right - left) * fraction, y, bar)
            }
        }

        if (state.showMatchScore && !state.showLineup) {
            val card = RectF(width * .08f, height * .25f, width * .92f, height * .49f)
            paint.color = Color.argb(178, 8, 10, 14)
            canvas.drawRoundRect(card, 42f, 42f, paint)
            val logoSize = min(width, height) * .105f * state.logoScale.coerceIn(.55f, 1.7f)
            fun drawTeamLogo(raw: String?, cx: Float) {
                if (raw.isNullOrBlank()) return
                decode(context, raw)?.let { logo ->
                    val dest = RectF(cx - logoSize / 2f, height * .285f, cx + logoSize / 2f, height * .285f + logoSize)
                    canvas.drawBitmap(logo, null, dest, paint)
                    logo.recycle()
                }
            }
            drawTeamLogo(state.homeTeamLogoUri, width * .27f)
            drawTeamLogo(state.awayTeamLogoUri, width * .73f)
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            paint.textSize = height * .047f
            canvas.drawText(state.matchScore.take(18), width * .5f, height * .395f, paint)
            paint.textSize = height * .018f
            canvas.drawText(state.homeTeamName.take(22), width * .27f, height * .455f, paint)
            canvas.drawText(state.awayTeamName.take(22), width * .73f, height * .455f, paint)
        }

        if (state.showLineup) {
            val field = RectF(width * .07f, height * .18f, width * .93f, height * .87f)
            paint.color = Color.rgb(16, 65, 43)
            canvas.drawRoundRect(field, 34f, 34f, paint)
            paint.color = Color.argb(210, 255, 255, 255)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            canvas.drawRoundRect(field, 34f, 34f, paint)
            canvas.drawLine(width * .07f, height * .525f, width * .93f, height * .525f, paint)
            canvas.drawCircle(width * .5f, height * .525f, width * .13f, paint)
            canvas.drawRect(width * .27f, height * .18f, width * .73f, height * .31f, paint)
            canvas.drawRect(width * .27f, height * .74f, width * .73f, height * .87f, paint)
            paint.style = Paint.Style.FILL
            val formationLines = state.formation.split("-").mapNotNull { it.toIntOrNull() }
            val playerNames = state.lineupPlayers.lines().map { it.trim() }.filter { it.isNotBlank() }.take(11)
            val points = mutableListOf(0.88f to 0.50f)
            var lineY = 0.72f
            formationLines.forEach { count ->
                repeat(count) { index -> points.add(lineY to ((index + 1f) / (count + 1f))) }
                lineY -= 0.20f
            }
            val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(242, 181, 27) }
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE; textSize = height * .014f; textAlign = Paint.Align.CENTER
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                setShadowLayer(4f, 0f, 2f, Color.BLACK)
            }
            points.take(11).forEachIndexed { index, point ->
                val x = width * (field.left / width + point.second * .86f)
                val y = height * (0.18f + point.first * .69f)
                canvas.drawCircle(x, y, width * .019f, markerPaint)
                playerNames.getOrNull(index)?.let { canvas.drawText(it.take(18), x, y + height * .026f, labelPaint) }
            }
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            paint.textSize = height * .026f
            canvas.drawText("${state.homeTeamName}  •  ${state.formation}", width * .5f, height * .145f, paint)
        }

        fun drawSubject() {
            state.subjectUri?.let { raw ->
                decode(context, raw)?.let { subject ->
                    val fit = min(width.toFloat() / subject.width, height.toFloat() / subject.height)
                    val scale = fit * state.subjectScale.coerceIn(0.2f, 3f)
                    val drawWidth = subject.width * scale
                    val drawHeight = subject.height * scale
                    val left = (width - drawWidth) / 2f + state.subjectX * width
                    val top = (height - drawHeight) / 2f + state.subjectY * height
                    canvas.drawBitmap(subject, null, RectF(left, top, left + drawWidth, top + drawHeight), paint)
                    subject.recycle()
                }
            }
        }

        if (!state.subjectOnTop) drawSubject()

        val typefaceStyle = if (state.font == CanvasFont.BOLD || state.font == CanvasFont.CONDENSED) Typeface.BOLD else Typeface.NORMAL
        val family = when (state.font) {
            CanvasFont.SERIF -> "serif"
            CanvasFont.MONO -> "monospace"
            CanvasFont.CONDENSED -> "sans-serif-condensed"
            else -> "sans-serif"
        }
        paint.typeface = Typeface.create(family, typefaceStyle)
        paint.textSize = state.headlineSize * 4.3f
        paint.color = state.headlineColor.toInt()
        paint.textAlign = when (state.textAlign) {
            TextAlignMode.LEFT -> Paint.Align.LEFT
            TextAlignMode.CENTER -> Paint.Align.CENTER
            TextAlignMode.RIGHT -> Paint.Align.RIGHT
        }
        if (state.textShadow) paint.setShadowLayer(8f, 0f, 4f, Color.BLACK) else paint.clearShadowLayer()
        val textWidth = width * when (state.template) {
            TemplateType.TRANSFER, TemplateType.HABER, TemplateType.THREADS -> 0.48f
            TemplateType.ISTATISTIK -> if (editorial) 0.84f else 0.48f
            TemplateType.ALINTI -> 0.82f
            else -> 0.90f
        }
        val textX = width * 0.5f + state.textX * textWidth
        val textY = height * (0.5f + state.textY)
        val anchorX = when (state.textAlign) {
            TextAlignMode.LEFT -> textX - textWidth / 2f
            TextAlignMode.CENTER -> textX
            TextAlignMode.RIGHT -> textX + textWidth / 2f
        }
        if (!state.showLineup) {
            drawWrapped(canvas, state.headline, anchorX, textY, paint, textWidth)
            if (state.subtitle.isNotBlank()) {
                paint.typeface = Typeface.create(family, Typeface.NORMAL)
                paint.textSize = state.subtitleSize * 4.3f
                paint.color = state.subtitleColor.toInt()
                drawWrapped(canvas, state.subtitle, anchorX, textY + paint.textSize * 1.7f, paint, textWidth)
            }
        }
        paint.clearShadowLayer()

        run {
            val light = state.imageUri == null && (state.template == TemplateType.ISTATISTIK || state.template == TemplateType.ALINTI)
            val ink = if (light) Color.rgb(143, 21, 56) else Color.rgb(255, 197, 46)
            paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 34f
            paint.color = ink
            val category = when (state.template) {
                TemplateType.TRANSFER -> "TRANSFER MERKEZİ"
                TemplateType.HABER -> "SON GELİŞMELER"
                TemplateType.SKOR -> "MAÇ GÜNÜ"
                TemplateType.ALINTI -> "SÖZÜN ÖZÜ"
                TemplateType.THREADS -> "GÜNDEM"
                TemplateType.ISTATISTIK -> "GALATASARAY  •  İSTATİSTİK"
                TemplateType.BOS -> "SOCIALCANVAS"
            }
            val cx = width * .5f
            val cy = height * .055f
            val radius = min(width, height) * .025f
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = max(5f, width * .0022f)
            paint.color = ink
            canvas.drawCircle(cx, cy, radius, paint)
            paint.style = Paint.Style.FILL
            when (state.template) {
                TemplateType.TRANSFER -> {
                    paint.strokeWidth = max(5f, width * .0022f)
                    paint.style = Paint.Style.STROKE
                    canvas.drawLine(cx - radius * .55f, cy - radius * .28f, cx + radius * .48f, cy - radius * .28f, paint)
                    canvas.drawLine(cx + radius * .48f, cy - radius * .28f, cx + radius * .20f, cy - radius * .52f, paint)
                    canvas.drawLine(cx + radius * .48f, cy - radius * .28f, cx + radius * .20f, cy - radius * .04f, paint)
                    canvas.drawLine(cx + radius * .55f, cy + radius * .30f, cx - radius * .48f, cy + radius * .30f, paint)
                    canvas.drawLine(cx - radius * .48f, cy + radius * .30f, cx - radius * .20f, cy + radius * .06f, paint)
                    canvas.drawLine(cx - radius * .48f, cy + radius * .30f, cx - radius * .20f, cy + radius * .54f, paint)
                }
                TemplateType.HABER -> {
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(cx - radius * .48f, cy - radius * .52f, cx + radius * .48f, cy + radius * .52f, paint)
                    canvas.drawLine(cx - radius * .25f, cy - radius * .20f, cx + radius * .25f, cy - radius * .20f, paint)
                    canvas.drawLine(cx - radius * .25f, cy + radius * .02f, cx + radius * .25f, cy + radius * .02f, paint)
                    canvas.drawLine(cx - radius * .25f, cy + radius * .24f, cx + radius * .12f, cy + radius * .24f, paint)
                }
                TemplateType.SKOR -> {
                    paint.style = Paint.Style.STROKE
                    canvas.drawCircle(cx, cy, radius * .58f, paint)
                    canvas.drawLine(cx - radius * .48f, cy - radius * .30f, cx + radius * .48f, cy + radius * .30f, paint)
                    canvas.drawLine(cx + radius * .48f, cy - radius * .30f, cx - radius * .48f, cy + radius * .30f, paint)
                }
                TemplateType.ALINTI -> {
                    paint.textSize = radius * 1.45f
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("“", cx, cy + radius * .52f, paint)
                }
                TemplateType.THREADS -> {
                    paint.style = Paint.Style.STROKE
                    canvas.drawRoundRect(RectF(cx - radius * .52f, cy - radius * .34f, cx + radius * .52f, cy + radius * .34f), radius * .15f, radius * .15f, paint)
                    canvas.drawLine(cx - radius * .18f, cy + radius * .34f, cx - radius * .34f, cy + radius * .55f, paint)
                }
                TemplateType.ISTATISTIK -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(cx - radius * .48f, cy + radius * .08f, cx - radius * .22f, cy + radius * .48f, paint)
                    canvas.drawRect(cx - radius * .10f, cy - radius * .18f, cx + radius * .16f, cy + radius * .48f, paint)
                    canvas.drawRect(cx + radius * .28f, cy - radius * .48f, cx + radius * .52f, cy + radius * .48f, paint)
                }
                TemplateType.BOS -> canvas.drawCircle(cx, cy, radius * .25f, paint)
            }
            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 34f
            paint.color = ink
            canvas.drawText(category, cx, height * .105f, paint)
            paint.typeface = Typeface.create("sans-serif", Typeface.BOLD_ITALIC)
            paint.textSize = 30f
            paint.color = if (light) Color.rgb(143, 21, 56) else Color.argb(220, 255, 255, 255)
            canvas.drawText("forzagundem", cx, height * .965f, paint)
        }

        if (state.subjectOnTop) drawSubject()

        state.stickerUri?.let { raw ->
            decode(context, raw)?.let { sticker ->
                val fit = min(width.toFloat() / sticker.width, height.toFloat() / sticker.height)
                val base = min(width, height) * .22f / max(sticker.width, sticker.height)
                val scale = max(base, fit * .22f) * state.stickerScale.coerceIn(.15f, 2.5f)
                val drawWidth = sticker.width * scale
                val drawHeight = sticker.height * scale
                val left = (width - drawWidth) / 2f + state.stickerX * width
                val top = (height - drawHeight) / 2f + state.stickerY * height
                canvas.drawBitmap(sticker, null, RectF(left, top, left + drawWidth, top + drawHeight), paint)
                sticker.recycle()
            }
        }

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "SocialCanvas_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SocialCanvas")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Galeriye kayıt konumu oluşturulamadı.")
        try {
            context.contentResolver.openOutputStream(uri)?.use {
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) throw IllegalStateException("PNG oluşturulamadı.")
            } ?: throw IllegalStateException("Dosya akışı açılamadı.")
        } catch (error: Exception) {
            context.contentResolver.delete(uri, null, null)
            throw error
        } finally {
            bitmap.recycle()
        }
        uri
    }

    private fun decode(context: Context, raw: String): Bitmap? =
        runCatching { context.contentResolver.openInputStream(Uri.parse(raw))?.use { BitmapFactory.decodeStream(it) } }.getOrNull()

    private fun drawWrapped(canvas: Canvas, text: String, anchorX: Float, startY: Float, paint: Paint, maxWidth: Float) {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val lines = mutableListOf<String>()
        var line = ""
        for (word in words) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                lines.add(line)
                line = word
            } else {
                line = candidate
            }
        }
        if (line.isNotEmpty()) lines.add(line)
        val lineHeight = paint.textSize * 1.12f
        val first = startY - (lines.size - 1) * lineHeight / 2f
        lines.forEachIndexed { index, value -> canvas.drawText(value, anchorX, first + index * lineHeight, paint) }
    }
}
