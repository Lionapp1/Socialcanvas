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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.fillMaxWidth(0.52f).aspectRatio(0.72f).clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF454545), Color(0xFF171717))))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("＋", fontSize = 42.sp, color = Gold, fontWeight = FontWeight.Light)
                            Text("Fotoğrafını ekle", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Canlı önizleme burada görünecek", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                }
            }

            // Editorial infographic canvas: restrained club-colour geometry replaces the empty background.
            if (state.designStyle == DesignStyle.EDITORIAL_LIGHT && state.imageUri == null) {
                Canvas(Modifier.fillMaxSize()) {
                    val red = Color(0xFF8F1538)
                    val gold = Color(0xFFF2B51B)
                    drawCircle(gold.copy(alpha = 0.14f), size.minDimension * 0.20f, androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.08f))
                    drawCircle(red.copy(alpha = 0.08f), size.minDimension * 0.13f, androidx.compose.ui.geometry.Offset(size.width * 0.06f, size.height * 0.90f))
                    drawLine(red.copy(alpha = 0.38f), androidx.compose.ui.geometry.Offset(size.width * 0.08f, size.height * 0.12f), androidx.compose.ui.geometry.Offset(size.width * 0.34f, size.height * 0.12f), 2.dp.toPx())
                    drawLine(gold, androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.12f), androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.12f), 3.dp.toPx())
                    drawLine(Color(0xFFDDDDDD), androidx.compose.ui.geometry.Offset(size.width * 0.08f, size.height * 0.88f), androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.88f), 1.dp.toPx())
                }
            } else {
                // Cinematic grade is reserved for photo-led templates.
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = darken)))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.28f), 0.42f to Color.Transparent,
                    0.72f to Color.Black.copy(alpha = 0.10f), 1f to Color.Black.copy(alpha = 0.62f)
                )))
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

            if (state.designStyle == DesignStyle.EDITORIAL_LIGHT && state.imageUri == null) {
                Column(Modifier.align(Alignment.TopCenter).padding(top = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚽", fontSize = 27.sp)
                    Text("GALATASARAY  •  İSTATİSTİK", color = Color(0xFF8F1538), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }

            val textWidth = when (state.template) {
                com.forzagundem.socialcanvas.data.TemplateType.TRANSFER,
                com.forzagundem.socialcanvas.data.TemplateType.HABER,
                com.forzagundem.socialcanvas.data.TemplateType.THREADS,
                com.forzagundem.socialcanvas.data.TemplateType.ISTATISTIK -> if (state.designStyle == DesignStyle.EDITORIAL_LIGHT) 0.84f else 0.48f
                com.forzagundem.socialcanvas.data.TemplateType.ALINTI -> 0.82f
                else -> 0.90f
            }
            Column(
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
