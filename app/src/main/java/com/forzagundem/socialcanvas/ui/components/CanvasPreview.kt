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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.forzagundem.socialcanvas.data.AspectRatio
import com.forzagundem.socialcanvas.data.EditorState
import com.forzagundem.socialcanvas.ui.theme.Gold
import com.forzagundem.socialcanvas.ui.theme.TextPrimary
import com.forzagundem.socialcanvas.ui.theme.TextSecondary

@Composable
fun CanvasPreview(state: EditorState, modifier: Modifier = Modifier) {
    val darken by animateFloatAsState(state.darken, label = "darken")
    val personAlpha by animateFloatAsState(if (state.focusPerson) 1f else 0.78f, label = "person")

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val targetAspect = state.ratio.aspect
        val width = if (maxWidth / maxHeight > targetAspect) maxHeight * targetAspect * 0.96f else maxWidth * 0.96f
        val height = width / targetAspect
        Box(
            modifier = Modifier.width(width).height(height).clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF252C3D), Color(0xFF080A10))))
        ) {
            if (state.imageUri != null) {
                AsyncImage(
                    model = state.imageUri,
                    contentDescription = "Tasarım arka plan fotoğrafı",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.fillMaxWidth(0.52f).aspectRatio(0.72f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF59647D), Color(0xFF20283A))))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(if (state.focusPerson) "👤" else "＋", fontSize = 42.sp) }
                }
            }
            if (state.focusPerson) {\n                Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.38f)))))\n            }\n            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = darken)))
            Column(
                Modifier.fillMaxSize().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.textBehind) Spacer(Modifier.height(24.dp)) else Spacer(Modifier.weight(1f))
                Text(
                    state.headline,
                    color = Gold,
                    fontSize = when (state.ratio) {
                        AspectRatio.STORY -> 27.sp
                        AspectRatio.SQUARE -> 25.sp
                        AspectRatio.THREADS -> 23.sp
                        else -> 24.sp
                    },
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(state.subtitle, color = TextPrimary.copy(alpha = 0.96f), fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, lineHeight = 20.sp)
                if (state.textBehind) Spacer(Modifier.weight(1f)) else Spacer(Modifier.height(18.dp))
            }
            if (state.showSafeArea) {
                Box(Modifier.fillMaxSize().padding(10.dp).border(1.dp, Gold.copy(alpha = 0.28f), RoundedCornerShape(14.dp)))
            }
            Text(
                state.ratio.label,
                Modifier.align(Alignment.TopEnd).padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.58f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}
