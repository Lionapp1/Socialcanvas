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
                .background(Brush.verticalGradient(listOf(Color(0xFF191919), Color(0xFF080808))))
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

            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = darken)))
            if (state.subjectUri != null) {
                Box(Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x20FFFFFF), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(
                            with(density) { previewWidth.toPx() } * (0.5f + state.subjectX),
                            with(density) { previewHeight.toPx() } * (0.52f + state.subjectY)
                        ),
                        radius = maxOf(with(density) { previewWidth.toPx() }, with(density) { previewHeight.toPx() }) * 0.62f
                    )
                ))
            }

            if (state.subjectUri != null && !state.subjectOnTop) {
                SubjectLayer(state, onStateChange)
            }

            val textWidth = if (state.template == com.forzagundem.socialcanvas.data.TemplateType.TRANSFER || state.template == com.forzagundem.socialcanvas.data.TemplateType.HABER) 0.48f else 0.90f
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
                    val top = size.height * 0.70f
                    listOf(state.statOne, state.statTwo, state.statThree).forEachIndexed { index, fraction ->
                        val y = top + index * 14.dp.toPx()
                        drawLine(Color.White.copy(alpha = 0.18f), androidx.compose.ui.geometry.Offset(left, y), androidx.compose.ui.geometry.Offset(right, y), 6.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
                        drawLine(Gold, androidx.compose.ui.geometry.Offset(left, y), androidx.compose.ui.geometry.Offset(left + (right - left) * fraction, y), 6.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
                    }
                }
            }

            if (state.template == com.forzagundem.socialcanvas.data.TemplateType.TRANSFER || state.template == com.forzagundem.socialcanvas.data.TemplateType.HABER) {
                Canvas(Modifier.fillMaxSize()) {
                    val step = 34.dp.toPx()
                    val grid = Color.White.copy(alpha = 0.055f)
                    var x = 0f
                    while (x <= size.width) {
                        drawLine(grid, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), 0.7.dp.toPx())
                        x += step
                    }
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(grid, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 0.7.dp.toPx())
                        y += step
                    }
                    drawRect(Gold.copy(alpha = 0.78f), size = androidx.compose.ui.geometry.Size(size.width * 0.48f, 2.dp.toPx()))
                }
            }

            if (state.showSafeArea) {
                Box(Modifier.fillMaxSize().padding(10.dp).border(1.dp, Gold.copy(alpha = 0.28f), RoundedCornerShape(14.dp)))
            }
            Text(
                state.ratio.label,
                Modifier.align(Alignment.TopEnd).padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.62f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
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
