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

        if (!editorial) {
            paint.color = Color.argb((state.darken.coerceIn(0f, 0.9f) * 255).toInt(), 0, 0, 0)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
        // Match the preview's cinematic top/bottom contrast treatment.
        if (!editorial) paint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(Color.argb(72, 0, 0, 0), Color.TRANSPARENT, Color.argb(24, 0, 0, 0), Color.argb(158, 0, 0, 0)),
            floatArrayOf(0f, 0.42f, 0.72f, 1f), Shader.TileMode.CLAMP
        )
        if (!editorial) canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
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
            TemplateType.TRANSFER, TemplateType.HABER, TemplateType.THREADS, TemplateType.ISTATISTIK -> 0.48f
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
        drawWrapped(canvas, state.headline, anchorX, textY, paint, textWidth)

        if (state.subtitle.isNotBlank()) {
            paint.typeface = Typeface.create(family, Typeface.NORMAL)
            paint.textSize = state.subtitleSize * 4.3f
            paint.color = state.subtitleColor.toInt()
            drawWrapped(canvas, state.subtitle, anchorX, textY + paint.textSize * 1.7f, paint, textWidth)
        }
        paint.clearShadowLayer()

        if (state.subjectOnTop) drawSubject()

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
