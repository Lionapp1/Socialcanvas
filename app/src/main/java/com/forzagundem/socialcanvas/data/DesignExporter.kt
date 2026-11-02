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
        val width = 1080
        val height = (width / state.ratio.aspect).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        paint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), Color.rgb(31, 39, 57), Color.rgb(7, 9, 15), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

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

        paint.color = Color.argb((state.darken.coerceIn(0f, 0.9f) * 255).toInt(), 0, 0, 0)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.color = Color.WHITE

        if (state.template == TemplateType.TRANSFER || state.template == TemplateType.HABER) {
            val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(14, 255, 255, 255)
                strokeWidth = 1.5f
            }
            val step = 108f
            var gx = 0f
            while (gx <= width) { canvas.drawLine(gx, 0f, gx, height.toFloat(), gridPaint); gx += step }
            var gy = 0f
            while (gy <= height) { canvas.drawLine(0f, gy, width.toFloat(), gy, gridPaint); gy += step }
            gridPaint.color = Color.rgb(85, 230, 224)
            gridPaint.alpha = 185
            canvas.drawRect(0f, 0f, width * 0.48f, 5f, gridPaint)
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

        val typefaceStyle = if (state.font == CanvasFont.BOLD) Typeface.BOLD else Typeface.NORMAL
        val family = when (state.font) {
            CanvasFont.SERIF -> "serif"
            CanvasFont.MONO -> "monospace"
            CanvasFont.CONDENSED -> "sans-serif-condensed"
            else -> "sans-serif"
        }
        paint.typeface = Typeface.create(family, typefaceStyle)
        paint.textSize = state.headlineSize * 2.15f
        paint.color = state.headlineColor.toInt()
        paint.textAlign = when (state.textAlign) {
            TextAlignMode.LEFT -> Paint.Align.LEFT
            TextAlignMode.CENTER -> Paint.Align.CENTER
            TextAlignMode.RIGHT -> Paint.Align.RIGHT
        }
        if (state.textShadow) paint.setShadowLayer(8f, 0f, 4f, Color.BLACK) else paint.clearShadowLayer()
        val textWidth = width * if (state.template == TemplateType.TRANSFER || state.template == TemplateType.HABER) 0.48f else 0.90f
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
            paint.textSize = state.subtitleSize * 2.15f
            paint.color = state.subtitleColor.toInt()
            drawWrapped(canvas, state.subtitle, anchorX, textY + paint.textSize * 1.7f, paint, textWidth)
        }
        paint.clearShadowLayer()

        if (state.subjectOnTop) drawSubject()

        if (state.showSafeArea) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.argb(100, 255, 183, 0)
            canvas.drawRoundRect(RectF(28f, 28f, width - 28f, height - 28f), 28f, 28f, paint)
            paint.style = Paint.Style.FILL
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
