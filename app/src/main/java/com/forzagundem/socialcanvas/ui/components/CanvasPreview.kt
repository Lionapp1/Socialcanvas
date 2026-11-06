package com.forzagundem.socialcanvas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.forzagundem.socialcanvas.data.AspectRatio
import com.forzagundem.socialcanvas.data.CanvasFont
import com.forzagundem.socialcanvas.data.EditorState
import com.forzagundem.socialcanvas.data.DesignStyle
import com.forzagundem.socialcanvas.data.TemplateType
import com.forzagundem.socialcanvas.data.TextAlignMode
import com.forzagundem.socialcanvas.ui.theme.Gold
import com.forzagundem.socialcanvas.ui.theme.TextPrimary
import com.forzagundem.socialcanvas.ui.theme.TextSecondary

@Composable
fun CanvasPreview(
    state: EditorState,
    modifier: Modifier = Modifier,
    onStateChange: (EditorState) -> Unit = {}
) {
    val darken by animateFloatAsState(state.darken, label = "darken")
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val targetAspect = state.ratio.aspect
        val previewWidth = if (maxWidth / maxHeight > targetAspect) maxHeight * targetAspect * 0.96f else maxWidth * 0.96f
        val previewHeight = previewWidth / targetAspect
        Box(
            Modifier.width(previewWidth).height(previewHeight).clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                .background(if (state.designStyle == DesignStyle.EDITORIAL_LIGHT && state.imageUri == null) Color(0xFFFAFAF8) else Color(0xFF111111))
        ) {
            if (state.imageUri != null) {
                AsyncImage(
                    model = state.imageUri,
                    contentDescription = "Arka plan fotoğrafı",
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        scaleX = state.imageScale
                        scaleY = state.imageScale
                        translationX = state.imageX * size.width
                        translationY = state.imageY * size.height
                    }.pointerInput(state.imageUri) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            onStateChange(state.copy(
                                imageX = (state.imageX + drag.x / size.width).coerceIn(-1f, 1f),
                                imageY = (state.imageY + drag.y / size.height).coerceIn(-1f, 1f)
                            ))
                        }
                    },
                    contentScale = ContentScale.Crop
                )
            } else {
                // Each template owns a real art-directed background; no generic empty placeholder.
                Box(Modifier.fillMaxSize().background(templateBrush(state)))
            }

            if (state.imageUri != null) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = darken)))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.28f), 0.42f to Color.Transparent,
                    0.72f to Color.Black.copy(alpha = 0.10f), 1f to Color.Black.copy(alpha = 0.62f)
                )))
            } else {
                Canvas(Modifier.fillMaxSize()) {
                    val red = Color(0xFF8F1538)
                    val gold = Color(0xFFF2B51B)
                    val white = Color.White
                    val w = size.width
                    val h = size.height
                    when (state.template) {
                        com.forzagundem.socialcanvas.data.TemplateType.TRANSFER -> {
                            drawRect(Brush.verticalGradient(listOf(Color(0xFF35101F), Color(0xFF100D13), Color(0xFF570D2A))))
                            drawCircle(gold.copy(alpha = .13f), w * .42f, androidx.compose.ui.geometry.Offset(w * .90f, h * .22f))
                            for (i in 0..5) drawLine(white.copy(alpha = .07f), androidx.compose.ui.geometry.Offset(w * (.48f + i * .10f), 0f), androidx.compose.ui.geometry.Offset(w * (.18f + i * .10f), h), 1.dp.toPx())
                            drawRoundRect(red, androidx.compose.ui.geometry.Offset(w * .07f, h * .12f), androidx.compose.ui.geometry.Size(w * .018f, h * .62f))
                            drawCircle(gold, w * .035f, androidx.compose.ui.geometry.Offset(w * .86f, h * .19f))
                        }
                        com.forzagundem.socialcanvas.data.TemplateType.HABER -> {
                            drawRect(Brush.verticalGradient(listOf(Color(0xFF17191F), Color(0xFF30323A), Color(0xFF111216))))
                            drawRect(red, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Size(w, h * .018f))
                            drawRect(gold, androidx.compose.ui.geometry.Offset(w * .08f, h * .17f), androidx.compose.ui.geometry.Size(w * .84f, h * .0025f))
                            drawCircle(red.copy(alpha = .20f), w * .38f, androidx.compose.ui.geometry.Offset(w * .92f, h * .82f))
                        }
                        com.forzagundem.socialcanvas.data.TemplateType.SKOR -> {
                            drawRect(Brush.verticalGradient(listOf(Color(0xFF111820), Color(0xFF26313A), Color(0xFF11151B))))
                            drawCircle(red.copy(alpha = .28f), w * .40f, androidx.compose.ui.geometry.Offset(w * .02f, h * .46f))
                            drawCircle(gold.copy(alpha = .18f), w * .40f, androidx.compose.ui.geometry.Offset(w * .98f, h * .46f))
                            for (i in 0..8) drawLine(white.copy(alpha = .045f), androidx.compose.ui.geometry.Offset(w * i / 8f, h * .25f), androidx.compose.ui.geometry.Offset(w * i / 8f, h * .78f), 1.dp.toPx())
                            drawRoundRect(white.copy(alpha = .10f), androidx.compose.ui.geometry.Offset(w * .08f, h * .37f), androidx.compose.ui.geometry.Size(w * .84f, h * .27f), androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()))
                        }
                        com.forzagundem.socialcanvas.data.TemplateType.ALINTI -> {
                            drawRect(Color(0xFFFAF7F0))
                            drawCircle(gold.copy(alpha = .22f), w * .48f, androidx.compose.ui.geometry.Offset(w, 0f))
                            drawCircle(red.copy(alpha = .08f), w * .34f, androidx.compose.ui.geometry.Offset(0f, h))
                            drawRoundRect(red, androidx.compose.ui.geometry.Offset(w * .09f, h * .18f), androidx.compose.ui.geometry.Size(w * .018f, h * .60f))
                            drawLine(Color(0xFFDED5C8), androidx.compose.ui.geometry.Offset(w * .16f, h * .82f), androidx.compose.ui.geometry.Offset(w * .84f, h * .82f), 1.dp.toPx())
                        }
                        com.forzagundem.socialcanvas.data.TemplateType.THREADS -> {
                            drawRect(Brush.verticalGradient(listOf(Color(0xFF20212A), Color(0xFF111217))))
                            drawCircle(red.copy(alpha = .18f), w * .48f, androidx.compose.ui.geometry.Offset(w, h))
                            drawCircle(gold.copy(alpha = .12f), w * .30f, androidx.compose.ui.geometry.Offset(0f, 0f))
                            drawRoundRect(white.copy(alpha = .09f), androidx.compose.ui.geometry.Offset(w * .07f, h * .14f), androidx.compose.ui.geometry.Size(w * .86f, h * .70f), androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()))
                        }
                        com.forzagundem.socialcanvas.data.TemplateType.ISTATISTIK -> {
                            drawRect(Color(0xFFFAFAF8))
                            drawCircle(gold.copy(alpha = .15f), w * .34f, androidx.compose.ui.geometry.Offset(w * .96f, h * .08f))
                            drawCircle(red.copy(alpha = .08f), w * .22f, androidx.compose.ui.geometry.Offset(w * .02f, h * .92f))
                            drawLine(red.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(w * .08f, h * .13f), androidx.compose.ui.geometry.Offset(w * .34f, h * .13f), 2.dp.toPx())
                            drawLine(gold, androidx.compose.ui.geometry.Offset(w * .66f, h * .13f), androidx.compose.ui.geometry.Offset(w * .92f, h * .13f), 3.dp.toPx())
                        }
                        com.forzagundem.socialcanvas.data.TemplateType.BOS -> {
                            drawRect(Brush.verticalGradient(listOf(Color(0xFF22212A), Color(0xFF111116))))
                            drawCircle(gold.copy(alpha = .12f), w * .42f, androidx.compose.ui.geometry.Offset(w, 0f))
                            drawCircle(red.copy(alpha = .14f), w * .38f, androidx.compose.ui.geometry.Offset(0f, h))
                        }
                    }
                    // Subtle, original lion-head watermark: a brand texture, never a competing logo.
                    val watermark = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * .50f, h * .20f)
                        cubicTo(w * .40f, h * .12f, w * .31f, h * .17f, w * .28f, h * .28f)
                        cubicTo(w * .19f, h * .24f, w * .16f, h * .34f, w * .23f, h * .43f)
                        cubicTo(w * .16f, h * .52f, w * .22f, h * .63f, w * .32f, h * .65f)
                        cubicTo(w * .35f, h * .77f, w * .44f, h * .82f, w * .50f, h * .84f)
                        cubicTo(w * .56f, h * .82f, w * .65f, h * .77f, w * .68f, h * .65f)
                        cubicTo(w * .78f, h * .63f, w * .84f, h * .52f, w * .77f, h * .43f)
                        cubicTo(w * .84f, h * .34f, w * .81f, h * .24f, w * .72f, h * .28f)
                        cubicTo(w * .69f, h * .17f, w * .60f, h * .12f, w * .50f, h * .20f)
                        close()
                    }
                    val watermarkColor = if (state.template == TemplateType.ALINTI || state.template == TemplateType.ISTATISTIK) red.copy(alpha = .055f) else gold.copy(alpha = .075f)
                    drawPath(watermark, watermarkColor)
                    drawOval(
                        watermarkColor.copy(alpha = .75f),
                        topLeft = androidx.compose.ui.geometry.Offset(w * .37f, h * .31f),
                        size = androidx.compose.ui.geometry.Size(w * .26f, h * .34f)
                    )
                    val facialMark = watermarkColor.copy(alpha = .95f)
                    drawCircle(facialMark, w * .009f, androidx.compose.ui.geometry.Offset(w * .435f, h * .455f))
                    drawCircle(facialMark, w * .009f, androidx.compose.ui.geometry.Offset(w * .565f, h * .455f))
                    drawLine(facialMark, androidx.compose.ui.geometry.Offset(w * .455f, h * .535f), androidx.compose.ui.geometry.Offset(w * .50f, h * .565f), w * .006f)
                    drawLine(facialMark, androidx.compose.ui.geometry.Offset(w * .50f, h * .565f), androidx.compose.ui.geometry.Offset(w * .545f, h * .535f), w * .006f)
                }
            }
            if (state.subjectUri != null) {
                val focusX = with(density) { previewWidth.toPx() } * (0.5f + state.subjectX)
                val focusY = with(density) { previewHeight.toPx() } * (0.50f + state.subjectY)
                val glowRadius = maxOf(with(density) { previewWidth.toPx() }, with(density) { previewHeight.toPx() }) * 0.66f
                Box(Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x55FFF0C2), Color(0x22FFB52E), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(focusX, focusY),
                        radius = glowRadius
                    )
                ))
                // A restrained warm rim light separates the player from a dark stadium background.
                Box(Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0x18E6A12A), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(focusX, focusY),
                        radius = glowRadius * 1.18f
                    )
                ))
            }

            if (state.subjectUri != null && !state.subjectOnTop) {
                SubjectLayer(state, onStateChange)
            }

            val light = state.imageUri == null && (state.template == TemplateType.ISTATISTIK || state.template == TemplateType.ALINTI)
                val ink = if (light) Color(0xFF8F1538) else Color(0xFFFFC52E)
                val category = when (state.template) {
                    TemplateType.TRANSFER -> "TRANSFER MERKEZİ"
                    TemplateType.HABER -> "SON GELİŞMELER"
                    TemplateType.SKOR -> "MAÇ GÜNÜ"
                    TemplateType.ALINTI -> "SÖZÜN ÖZÜ"
                    TemplateType.THREADS -> "GÜNDEM"
                    TemplateType.ISTATISTIK -> "GALATASARAY  •  İSTATİSTİK"
                    TemplateType.BOS -> "SOCIALCANVAS"
                }
                val mark = when (state.template) {
                    TemplateType.TRANSFER -> Icons.Default.SwapHoriz
                    TemplateType.HABER -> Icons.Default.Article
                    TemplateType.SKOR -> Icons.Default.SportsSoccer
                    TemplateType.ALINTI -> Icons.Default.FormatQuote
                    TemplateType.THREADS -> Icons.Default.Forum
                    TemplateType.ISTATISTIK -> Icons.Default.BarChart
                    TemplateType.BOS -> Icons.Default.AutoAwesome
                }
                Column(
                    Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (state.imageUri != null) Color.Black.copy(alpha = .34f) else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(mark, contentDescription = category, tint = ink, modifier = Modifier.width(27.dp).height(27.dp))
                    Text(category, color = ink, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            Text(
                "forzagundem",
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp),
                color = if (light) Color(0xFF8F1538).copy(alpha = .72f) else Color.White.copy(alpha = .72f),
                fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
            )

            if (state.showMatchScore && !state.showLineup) {
                Row(
                    Modifier.align(Alignment.TopCenter).padding(top = previewHeight * 0.24f)
                        .fillMaxWidth(0.90f).clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = if (state.imageUri != null) .48f else .28f))
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state.homeTeamLogoUri != null) AsyncImage(
                            model = state.homeTeamLogoUri, contentDescription = "${state.homeTeamName} logosu",
                            modifier = Modifier.width((42f * state.logoScale).dp).height((42f * state.logoScale).dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(state.homeTeamName, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
                    }
                    Text(state.matchScore, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state.awayTeamLogoUri != null) AsyncImage(
                            model = state.awayTeamLogoUri, contentDescription = "${state.awayTeamName} logosu",
                            modifier = Modifier.width((42f * state.logoScale).dp).height((42f * state.logoScale).dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(state.awayTeamName, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
                    }
                }
            }

            if (state.showLineup) {
                val names = state.lineupPlayers.lines().map { it.trim() }.filter { it.isNotBlank() }.take(11)
                val lines = state.formation.split("-").mapNotNull { it.toIntOrNull() }
                val positions = buildList {
                    add(0.88f to 0.50f)
                    var y = 0.72f
                    lines.forEach { count ->
                        repeat(count) { index ->
                            add((y - lines.indexOf(count) * 0.0f) to ((index + 1f) / (count + 1f)))
                        }
                        y -= 0.20f
                    }
                }.take(11)
                Canvas(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 52.dp)) {
                    val pitch = Color.White.copy(alpha = .82f)
                    val stroke = 1.dp.toPx()
                    drawRoundRect(Color(0xFF123E2C).copy(alpha = .96f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()))
                    drawRoundRect(pitch, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()))
                    drawLine(pitch, androidx.compose.ui.geometry.Offset(0f, size.height / 2), androidx.compose.ui.geometry.Offset(size.width, size.height / 2), stroke)
                    drawCircle(pitch, size.width * .12f, androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                    drawRect(pitch, androidx.compose.ui.geometry.Offset(size.width * .22f, 0f), androidx.compose.ui.geometry.Size(size.width * .56f, size.height * .14f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                    drawRect(pitch, androidx.compose.ui.geometry.Offset(size.width * .22f, size.height * .86f), androidx.compose.ui.geometry.Size(size.width * .56f, size.height * .14f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                    val labelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.WHITE
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                        setShadowLayer(3f, 0f, 1f, android.graphics.Color.BLACK)
                    }
                    positions.forEachIndexed { index, pos ->
                        val x = size.width * pos.second
                        val y = size.height * pos.first
                        drawCircle(Color(0xFFF2B51B), size.width * .026f, androidx.compose.ui.geometry.Offset(x, y))
                        names.getOrNull(index)?.let { drawContext.canvas.nativeCanvas.drawText(it.take(15), x, y + size.width * .055f, labelPaint) }
                    }
                }
                Text("${state.homeTeamName}  ·  ${state.formation}", modifier = Modifier.align(Alignment.TopCenter).padding(top = 34.dp),
                    color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }

            val textWidth = when (state.template) {
                com.forzagundem.socialcanvas.data.TemplateType.TRANSFER,
                com.forzagundem.socialcanvas.data.TemplateType.HABER,
                com.forzagundem.socialcanvas.data.TemplateType.THREADS,
                com.forzagundem.socialcanvas.data.TemplateType.ISTATISTIK -> if (state.designStyle == DesignStyle.EDITORIAL_LIGHT) 0.84f else 0.48f
                com.forzagundem.socialcanvas.data.TemplateType.ALINTI -> 0.82f
                else -> 0.90f
            }
            if (!state.showLineup) Column(
                Modifier.fillMaxWidth(textWidth).align(Alignment.Center)
                    .graphicsLayer {
                        translationX = state.textX * size.width
                        translationY = state.textY * size.height
                    }
                    .pointerInput(state.textX, state.textY) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            onStateChange(state.copy(
                                textX = (state.textX + drag.x / size.width).coerceIn(-0.45f, 0.45f),
                                textY = (state.textY + drag.y / size.height).coerceIn(-0.45f, 0.45f)
                            ))
                        }
                    },
                horizontalAlignment = when (state.textAlign) {
                    TextAlignMode.LEFT -> Alignment.Start
                    TextAlignMode.CENTER -> Alignment.CenterHorizontally
                    TextAlignMode.RIGHT -> Alignment.End
                }
            ) {
                Text(
                    state.headline,
                    color = Color(state.headlineColor),
                    fontSize = state.headlineSize.sp,
                    fontWeight = if (state.font == CanvasFont.MODERN || state.font == CanvasFont.SERIF) FontWeight.SemiBold else FontWeight.Black,
                    fontFamily = state.font.toFontFamily(),
                    textAlign = state.textAlign.toCompose(),
                    lineHeight = (state.headlineSize * 1.12f).sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = if (state.textShadow) Shadow(Color.Black.copy(alpha = 0.7f), androidx.compose.ui.geometry.Offset(0f, 3f), 6f) else null
                    )
                )
                if (state.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.subtitle,
                        color = Color(state.subtitleColor),
                        fontSize = state.subtitleSize.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = state.font.toFontFamily(),
                        textAlign = state.textAlign.toCompose(),
                        lineHeight = (state.subtitleSize * 1.25f).sp
                    )
                }
            }

            if (state.subjectUri != null && state.subjectOnTop) {
                SubjectLayer(state, onStateChange)
            }

            if (state.template == com.forzagundem.socialcanvas.data.TemplateType.ISTATISTIK) {
                Canvas(Modifier.fillMaxSize()) {
                    val left = size.width * 0.12f
                    val right = size.width * 0.88f
                    val top = size.height * if (state.designStyle == DesignStyle.EDITORIAL_LIGHT) 0.72f else 0.70f
                    listOf(state.statOne, state.statTwo, state.statThree).forEachIndexed { index, fraction ->
                        val y = top + index * 14.dp.toPx()
                        val trackColor = if (state.designStyle == DesignStyle.EDITORIAL_LIGHT) Color(0xFFE3E3E3) else Color.White.copy(alpha = 0.18f)
                        val barColor = if (index % 2 == 0) Color(0xFF8F1538) else Color(0xFFF2B51B)
                        drawLine(trackColor, androidx.compose.ui.geometry.Offset(left, y), androidx.compose.ui.geometry.Offset(right, y), 6.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
                        drawLine(barColor, androidx.compose.ui.geometry.Offset(left, y), androidx.compose.ui.geometry.Offset(left + (right - left) * fraction, y), 6.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
                    }
                }
            }

    }
}
}

@Composable
private fun SubjectLayer(state: EditorState, onStateChange: (EditorState) -> Unit) {
    AsyncImage(
        model = state.subjectUri,
        contentDescription = "Ön plan kişi görseli",
        modifier = Modifier.fillMaxSize().graphicsLayer {
            scaleX = state.subjectScale
            scaleY = state.subjectScale
            translationX = state.subjectX * size.width
            translationY = state.subjectY * size.height
        }.pointerInput(state.subjectUri) {
            detectDragGestures { change, drag ->
                change.consume()
                onStateChange(state.copy(
                    subjectX = (state.subjectX + drag.x / size.width).coerceIn(-0.8f, 0.8f),
                    subjectY = (state.subjectY + drag.y / size.height).coerceIn(-0.8f, 0.8f)
                ))
            }
        },
        contentScale = ContentScale.Fit
    )
}

private fun CanvasFont.toFontFamily(): FontFamily = when (this) {
    CanvasFont.MODERN -> FontFamily.SansSerif
    CanvasFont.BOLD -> FontFamily.SansSerif
    CanvasFont.SERIF -> FontFamily.Serif
    CanvasFont.MONO -> FontFamily.Monospace
    CanvasFont.CONDENSED -> FontFamily.SansSerif
}

private fun TextAlignMode.toCompose(): TextAlign = when (this) {
    TextAlignMode.LEFT -> TextAlign.Start
    TextAlignMode.CENTER -> TextAlign.Center
    TextAlignMode.RIGHT -> TextAlign.End
}

@Composable
private fun templateBrush(state: EditorState): Brush = when (state.template) {
    TemplateType.ALINTI -> Brush.verticalGradient(listOf(Color(0xFFFAF7F0), Color(0xFFF0E9DC)))
    TemplateType.ISTATISTIK -> Brush.verticalGradient(listOf(Color(0xFFFAFAF8), Color(0xFFF2F1ED)))
    TemplateType.HABER -> Brush.verticalGradient(listOf(Color(0xFF17191F), Color(0xFF30323A), Color(0xFF111216)))
    TemplateType.SKOR -> Brush.verticalGradient(listOf(Color(0xFF111820), Color(0xFF26313A), Color(0xFF11151B)))
    TemplateType.TRANSFER -> Brush.verticalGradient(listOf(Color(0xFF35101F), Color(0xFF100D13), Color(0xFF570D2A)))
    TemplateType.THREADS -> Brush.verticalGradient(listOf(Color(0xFF20212A), Color(0xFF111217)))
    TemplateType.BOS -> Brush.verticalGradient(listOf(Color(0xFF22212A), Color(0xFF111116)))
}
