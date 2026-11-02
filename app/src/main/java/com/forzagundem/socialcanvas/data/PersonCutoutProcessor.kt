package com.forzagundem.socialcanvas.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import kotlin.math.max

object PersonCutoutProcessor {
    suspend fun createTransparentCutout(context: Context, sourceUri: Uri): Uri = withContext(Dispatchers.IO) {
        val original = context.contentResolver.openInputStream(sourceUri)?.use {
            BitmapFactory.decodeStream(it)
        } ?: error("Fotoğraf açılamadı.")

        val maxDimension = 2048
        val scale = minOf(1f, maxDimension.toFloat() / max(original.width, original.height))
        val input = if (scale < 1f) Bitmap.createScaledBitmap(
            original,
            (original.width * scale).toInt().coerceAtLeast(1),
            (original.height * scale).toInt().coerceAtLeast(1),
            true
        ) else original

        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .build()
        val segmenter = Segmentation.getClient(options)
        val mask = try {
            Tasks.await(segmenter.process(InputImage.fromBitmap(input, 0)))
        } finally {
            segmenter.close()
        }

        val buffer = mask.buffer
        buffer.rewind()
        buffer.order(ByteOrder.nativeOrder())
        val maskWidth = mask.width
        val maskHeight = mask.height
        val pixels = IntArray(input.width * input.height)
        val row = FloatArray(maskWidth * maskHeight)
        for (i in row.indices) row[i] = buffer.float

        for (y in 0 until input.height) {
            val my = (y.toFloat() * maskHeight / input.height).toInt().coerceIn(0, maskHeight - 1)
            for (x in 0 until input.width) {
                val mx = (x.toFloat() * maskWidth / input.width).toInt().coerceIn(0, maskWidth - 1)
                val confidence = row[my * maskWidth + mx].coerceIn(0f, 1f)
                val alpha = ((confidence - 0.12f) / 0.76f).coerceIn(0f, 1f)
                val source = input.getPixel(x, y)
                pixels[y * input.width + x] = Color.argb(
                    (alpha * 255).toInt(),
                    Color.red(source), Color.green(source), Color.blue(source)
                )
            }
        }

        val cutout = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        cutout.setPixels(pixels, 0, input.width, 0, 0, input.width, input.height)
        if (input !== original) input.recycle()
        original.recycle()

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "SocialCanvas_Person_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SocialCanvas")
            }
        }
        val output = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: run { cutout.recycle(); error("Kişi görseli kaydedilemedi.") }
        try {
            context.contentResolver.openOutputStream(output)?.use { stream ->
                check(cutout.compress(Bitmap.CompressFormat.PNG, 100, stream)) { "Şeffaf PNG oluşturulamadı." }
            } ?: error("Kayıt akışı açılamadı.")
        } catch (error: Exception) {
            context.contentResolver.delete(output, null, null)
            throw error
        } finally {
            cutout.recycle()
        }
        output
    }
}
