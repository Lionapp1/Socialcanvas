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
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

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
            runCatching { context.contentResolver.openInputStream(Uri.parse(raw))?.use { BitmapFactory.decodeStream(it) } }
                .getOrNull()?.let { source ->
                    val scale = max(width.toFloat() / source.width, height.toFloat() / source.height)
                    val sw = width / scale
                    val sh = height / scale
                    val src = Rect(((source.width - sw) / 2).toInt(), ((source.height - sh) / 2).toInt(),
                        ((source.width + sw) / 2).toInt(), ((source.height + sh) / 2).toInt())
                    canvas.drawBitmap(source, src, Rect(0, 0, width, height), paint)
                    source.recycle()
                }
        }
        paint.shader = null
        paint.color = Color.argb((state.darken.coerceIn(0f, 0.9f) * 255).toInt(), 0, 0, 0)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.color = Color.rgb(255, 183, 0)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
        paint.textSize = when (state.ratio) { AspectRatio.STORY -> 78f; AspectRatio.THREADS -> 66f; else -> 72f }
        val centerY = if (state.textBehind) height * 0.22f else height * 0.70f
        drawWrapped(canvas, state.headline, width / 2f, centerY, paint, width * 0.84f)
        paint.color = Color.WHITE
        paint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        paint.textSize = 38f
        drawWrapped(canvas, state.subtitle, width / 2f, centerY + 68f, paint, width * 0.84f)

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
        } finally { bitmap.recycle() }
        uri
    }

    private fun drawWrapped(canvas: Canvas, text: String, centerX: Float, startY: Float, paint: Paint, maxWidth: Float) {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val lines = mutableListOf<String>()
        var line = ""
        for (word in words) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) { lines.add(line); line = word }
            else line = candidate
        }
        if (line.isNotEmpty()) lines.add(line)
        val lineHeight = paint.textSize * 1.12f
        val first = startY - (lines.size - 1) * lineHeight / 2f
        lines.forEachIndexed { index, value -> canvas.drawText(value, centerX, first + index * lineHeight, paint) }
    }
}
