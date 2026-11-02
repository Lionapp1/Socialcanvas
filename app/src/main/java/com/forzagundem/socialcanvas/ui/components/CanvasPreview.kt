package com.forzagundem.socialcanvas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forzagundem.socialcanvas.data.AspectRatio
import com.forzagundem.socialcanvas.data.EditorState
import com.forzagundem.socialcanvas.ui.theme.Gold
import com.forzagundem.socialcanvas.ui.theme.TextPrimary
import com.forzagundem.socialcanvas.ui.theme.TextSecondary

@Composable
fun CanvasPreview(
    state: EditorState,
    modifier: Modifier = Modifier
) {
    val darken by animateFloatAsState(targetValue = state.darken, label = "darken")
    val personAlpha by animateFloatAsState(
        targetValue = if (state.focusPerson) 1f else 0.5f,
        label = "person"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (state.textBehind) 0.4f else 1f,
        label = "text"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val maxWpx = with(density) { maxWidth.toPx() }
        val maxHpx = with(density) { maxHeight.toPx() }
        val targetAspect = state.ratio.aspect

        val (wPx, hPx) = if (maxWpx / maxHpx > targetAspect) {
            val h = maxHpx * 0.96f
            val w = h * targetAspect
            w to h
        } else {
            val w = maxWpx * 0.96f
            val h = w / targetAspect
            w to h
        }

        val wDp = with(density) { wPx.toDp() }
        val hDp = with(density) { hPx.toDp() }

        Box(
            modifier = Modifier
                .width(wDp)
                .height(hDp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A1F2E),
                            Color(0xFF0D1018),
                            Color(0xFF080A10)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (state.ratio == AspectRatio.STORY) 12.dp else 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.52f)
                        .aspectRatio(0.72f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF4A5168).copy(alpha = personAlpha),
                                    Color(0xFF2A3040).copy(alpha = personAlpha)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.focusPerson) "👤" else "🖼️",
                        fontSize = 40.sp,
                        color = Color.White.copy(alpha = personAlpha)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = darken))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.textBehind) {
                    Spacer(Modifier.height(28.dp))
                } else {
                    Spacer(Modifier.weight(1f))
                }

                Text(
                    text = state.headline,
                    color = Gold.copy(alpha = textAlpha),
                    fontSize = when (state.ratio) {
                        AspectRatio.STORY -> 26.sp
                        AspectRatio.SQUARE -> 24.sp
                        AspectRatio.THREADS -> 20.sp
                        else -> 22.sp
                    },
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.subtitle,
                    color = TextPrimary.copy(alpha = textAlpha * 0.92f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                if (state.textBehind) {
                    Spacer(Modifier.weight(1f))
                } else {
                    Spacer(Modifier.height(18.dp))
                }
            }

            if (state.showSafeArea) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .border(1.dp, Gold.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                )
            }

            Text(
                text = state.ratio.label,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
