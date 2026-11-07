package com.forzagundem.socialcanvas.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forzagundem.socialcanvas.ui.theme.*

data class GsPlayer(
    val number: Int,
    val name: String,
    val shortName: String,
    val pos: String,
    val role: String
)

object GsSquadData {
    val startingXi = listOf(
        GsPlayer(1, "Uguncan Cakir", "Cakir", "KL", "GK"),
        GsPlayer(7, "Roland Sallai", "Sallai", "SAG B", "DEF"),
        GsPlayer(6, "Davinson Sanchez", "Sanchez", "STP", "DEF"),
        GsPlayer(42, "Abdulkerim Bardakci", "Bardakci", "STP", "DEF"),
        GsPlayer(4, "Ismail Jakobs", "Jakobs", "SOL B", "DEF"),
        GsPlayer(34, "Lucas Torreira", "Torreira", "DOS", "MID"),
        GsPlayer(99, "Mario Lemina", "Lemina", "DOS", "MID"),
        GsPlayer(11, "Yunus Akgun", "Yunus", "SAG K", "MID"),
        GsPlayer(83, "Aleksey Batrakov", "Batrakov", "GO", "MID"),
        GsPlayer(53, "Baris Alper Yilmaz", "Baris A.", "SOL K", "MID"),
        GsPlayer(45, "Victor Osimhen", "Osimhen", "FOR", "FWD")
    )

    val fullSquad = listOf(
        GsPlayer(1, "Uguncan Cakir", "Cakir", "KL", "GK"),
        GsPlayer(19, "Gunay Guvenc", "Guvenc", "KL", "GK"),
        GsPlayer(24, "Jankat Yilmaz", "Jankat", "KL", "GK"),
        GsPlayer(6, "Davinson Sanchez", "Sanchez", "STP", "DEF"),
        GsPlayer(42, "Abdulkerim Bardakci", "Bardakci", "STP", "DEF"),
        GsPlayer(4, "Ismail Jakobs", "Jakobs", "SOL B", "DEF"),
        GsPlayer(7, "Roland Sallai", "Sallai", "SAG B", "DEF"),
        GsPlayer(17, "Eren Elmali", "Elmali", "SOL B", "DEF"),
        GsPlayer(90, "Wilfried Singo", "Singo", "SAG B", "DEF"),
        GsPlayer(23, "Kaan Ayhan", "Ayhan", "STP", "DEF"),
        GsPlayer(3, "El Chadaille Bitshiabu", "Bitshiabu", "STP", "DEF"),
        GsPlayer(91, "Arda Unyay", "Unyay", "STP", "DEF"),
        GsPlayer(34, "Lucas Torreira", "Torreira", "DOS", "MID"),
        GsPlayer(99, "Mario Lemina", "Lemina", "DOS", "MID"),
        GsPlayer(8, "Gabriel Sara", "Sara", "OS", "MID"),
        GsPlayer(20, "Ilkay Gundogan", "Gundogan", "OS", "MID"),
        GsPlayer(83, "Aleksey Batrakov", "Batrakov", "GO", "MID"),
        GsPlayer(18, "Lesley Ugochukwu", "Ugochukwu", "DOS", "MID"),
        GsPlayer(5, "Eyup Aydin", "Eyup", "DOS", "MID"),
        GsPlayer(45, "Victor Osimhen", "Osimhen", "FOR", "FWD"),
        GsPlayer(11, "Yunus Akgun", "Yunus", "SAG K", "FWD"),
        GsPlayer(53, "Baris Alper Yilmaz", "Baris A.", "SOL K", "FWD"),
        GsPlayer(10, "Leroy Sane", "Sane", "SAG K", "FWD"),
        GsPlayer(27, "Rafael Leao", "Leao", "SOL K", "FWD"),
        GsPlayer(21, "Deniz Gul", "Deniz", "FOR", "FWD")
    )
}

@Composable
fun SquadPage() {
    val greenDark = Color(0xFF0B3A1C)
    val greenMid = Color(0xFF146B32)
    val lineWhite = Color.White.copy(alpha = 0.5f)
    val xi = GsSquadData.startingXi
    val gk = xi[0]
    val rb = xi[1]; val cbR = xi[2]; val cbL = xi[3]; val lb = xi[4]
    val dmR = xi[5]; val dmL = xi[6]
    val rw = xi[7]; val cam = xi[8]; val lw = xi[9]
    val st = xi[10]

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE11D36), Color(0xFF9B1224)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("ForzaX", color = TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text("GALATASARAY KADRO", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }
            Text("4-2-3-1", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(greenMid, greenDark, Color(0xFF0A2E16), greenMid)))
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val s = Stroke(width = 2f)
                        val stn = Stroke(width = 1.3f)
                        drawRect(lineWhite, Offset(10f, 10f), Size(w - 20f, h - 20f), style = s)
                        drawLine(lineWhite, Offset(10f, h / 2), Offset(w - 10f, h / 2), strokeWidth = 2f)
                        drawCircle(lineWhite, w * 0.11f, Offset(w / 2, h / 2), style = s)
                        drawCircle(lineWhite, 3.5f, Offset(w / 2, h / 2))
                        val bw = w * 0.52f
                        val bh = h * 0.15f
                        drawRect(lineWhite, Offset((w - bw) / 2, 10f), Size(bw, bh), style = stn)
                        drawRect(lineWhite, Offset((w - bw) / 2, h - 10f - bh), Size(bw, bh), style = stn)
                        val sw = w * 0.26f
                        val sh = h * 0.065f
                        drawRect(lineWhite, Offset((w - sw) / 2, 10f), Size(sw, sh), style = stn)
                        drawRect(lineWhite, Offset((w - sw) / 2, h - 10f - sh), Size(sw, sh), style = stn)
                    }

                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { PitchChip(st) }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            PitchChip(lw); PitchChip(cam); PitchChip(rw)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            PitchChip(dmL); PitchChip(dmR)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            PitchChip(lb); PitchChip(cbL); PitchChip(cbR); PitchChip(rb)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { PitchChip(gk) }
                    }

                    Text(
                        "ForzaX · GS 1905",
                        color = Color.White.copy(alpha = 0.2f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                    )
                }
            }

            item {
                Text("TEKNIK DIREKTOR", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Card).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFE11D36).copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) { Text("OB", color = Gold, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Okan Buruk", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Teknik Direktor · 2022 —", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }

            item { Text("TUM KADRO", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }

            listOf("GK" to "KALECILER", "DEF" to "DEFANS", "MID" to "ORTA SAHA", "FWD" to "FORVET").forEach { (role, title) ->
                item { Text(title, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
                items(GsSquadData.fullSquad.filter { it.role == role }) { p ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Card).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFE11D36), Color(0xFFFFC107)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${p.number}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(p.pos, color = TextMuted, fontSize = 11.sp)
                        }
                        Text(p.role, color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Text(
                    "Kadro 2026/27 sezonuna goredir. Dizilis maclara gore degisebilir.",
                    color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PitchChip(player: GsPlayer) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFE11D36), Color(0xFF8B1020))))
                .border(2.dp, Gold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("${player.number}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        Spacer(Modifier.height(3.dp))
        Text(
            player.shortName,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
