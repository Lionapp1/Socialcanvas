package com.forzagundem.socialcanvas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.vector.ImageVector
import com.forzagundem.socialcanvas.data.TemplateType
import com.forzagundem.socialcanvas.ui.theme.Bg
import com.forzagundem.socialcanvas.ui.theme.Border
import com.forzagundem.socialcanvas.ui.theme.Card
import com.forzagundem.socialcanvas.ui.theme.CardElevated
import com.forzagundem.socialcanvas.ui.theme.Gold
import com.forzagundem.socialcanvas.ui.theme.TextMuted
import com.forzagundem.socialcanvas.ui.theme.TextPrimary
import com.forzagundem.socialcanvas.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onTemplateSelected: (TemplateType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .padding(top = 18.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(Gold, Gold.copy(alpha = 0.7f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text(
                    "SOCIAL CANVAS",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    "Mobil sosyal medya tasarım stüdyosu",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Şablon seç",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Başlamak için bir tasarım tipi seçin. Sonra oran, metin ve odak ayarlarını düzenleyebilirsiniz.",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(TemplateType.entries.toList()) { template ->
                TemplateCard(template = template, onClick = { onTemplateSelected(template) })
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: TemplateType,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .background(Card)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardElevated),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = templateIcon(template), contentDescription = template.title, tint = Gold, modifier = Modifier.size(30.dp))
                Spacer(Modifier.height(5.dp))
                Text("SOCIAL CANVAS  /  " + template.title.uppercase(), color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            template.title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            template.subtitle,
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}


@Composable
private fun templateIcon(template: TemplateType): ImageVector = when (template) {
    TemplateType.HABER -> androidx.compose.material.icons.Icons.Outlined.Article
    TemplateType.TRANSFER -> androidx.compose.material.icons.Icons.Outlined.SwapHoriz
    TemplateType.SKOR -> androidx.compose.material.icons.Icons.Outlined.SportsSoccer
    TemplateType.ALINTI -> androidx.compose.material.icons.Icons.Outlined.FormatQuote
    TemplateType.THREADS -> androidx.compose.material.icons.Icons.Outlined.AlternateEmail
    TemplateType.ISTATISTIK -> androidx.compose.material.icons.Icons.Outlined.InsertChart
    TemplateType.BOS -> androidx.compose.material.icons.Icons.Outlined.AutoAwesome
}
