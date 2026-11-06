package com.forzagundem.socialcanvas.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.forzagundem.socialcanvas.data.ForzaArticle
import com.forzagundem.socialcanvas.data.ForzaNewsRepository
import com.forzagundem.socialcanvas.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private val categories = listOf("Tümü", "Galatasaray", "Süper Lig", "Transfer", "Avrupa", "Son Dakika")

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var articles by remember { mutableStateOf(ForzaNewsRepository.cached(context)) }
    var selected by remember { mutableStateOf("Tümü") }
    var loading by remember { mutableStateOf(false) }
    var updated by remember { mutableLongStateOf(ForzaNewsRepository.lastUpdated(context)) }
    var searchMode by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    fun refresh() {
        if (loading) return
        scope.launch {
            loading = true
            runCatching { ForzaNewsRepository.refresh(context) }
                .onSuccess { articles = it; updated = ForzaNewsRepository.lastUpdated(context) }
            loading = false
        }
    }
    LaunchedEffect(Unit) { if (articles.isEmpty()) refresh() }

    val filtered = articles.filter { article ->
        val categoryMatch = when (selected) {
            "Galatasaray" -> listOf("galatasaray", "okan buruk", "sarı-kırmızı", "sarı kırmızı").any { article.title.contains(it, true) || article.summary.contains(it, true) }
            "Süper Lig" -> listOf("süper lig", "fenerbahçe", "beşiktaş", "trabzonspor", "başakşehir").any { article.title.contains(it, true) || article.summary.contains(it, true) }
            "Transfer" -> listOf("transfer", "imza", "anlaşma", "kiralık", "bonservis").any { article.title.contains(it, true) || article.summary.contains(it, true) }
            "Avrupa" -> article.category == "Dünya" || listOf("şampiyonlar ligi", "premier league", "la liga", "serie a").any { article.title.contains(it, true) }
            "Son Dakika" -> listOf("son dakika", "resmi açıklama", "acil").any { article.title.contains(it, true) }
            else -> true
        }
        categoryMatch && (query.isBlank() || article.title.contains(query, true) || article.summary.contains(query, true))
    }

    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(Color(0xFFE11D36), Color(0xFF8D1028)))), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.SportsSoccer, contentDescription = "ForzaX", tint = Color.White, modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text("ForzaX", color = TextPrimary, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = (-.8).sp)
                Text("SPORUN NABZI", color = Color(0xFFE34A5C), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.1.sp)
            }
            IconButton(onClick = { searchMode = !searchMode }) {
                Icon(Icons.Outlined.Search, contentDescription = "Ara", tint = TextPrimary)
            }
            IconButton(onClick = { refresh() }, enabled = !loading) {
                if (loading) CircularProgressIndicator(Modifier.size(19.dp), color = Gold, strokeWidth = 2.dp)
                else Icon(Icons.Outlined.Refresh, contentDescription = "Yenile", tint = TextPrimary)
            }
        }

        if (searchMode) {
            OutlinedTextField(
                value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                placeholder = { Text("Haber, takım veya konu ara") }, singleLine = true,
                shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold, unfocusedBorderColor = Border, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                )
            )
            Spacer(Modifier.height(10.dp))
        }

        Row(Modifier.fillMaxWidth().padding(start = 18.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                val active = selected == category
                Box(
                    Modifier.clip(CircleShape).background(if (active) Color(0xFFE11D36) else Card)
                        .border(1.dp, if (active) Color(0xFFE11D36) else Border, CircleShape)
                        .clickable { selected = category }.padding(horizontal = 15.dp, vertical = 9.dp)
                ) {
                    Text(category, color = if (active) Color.White else TextSecondary, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(4.dp).height(17.dp).clip(CircleShape).background(Color(0xFFE11D36)))
                    Spacer(Modifier.width(8.dp))
                    Text("GÜNDEM", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                    Spacer(Modifier.weight(1f))
                    Text(if (updated > 0) "Güncellendi · ${timeLabel(updated)}" else "Canlı haber akışı", color = TextMuted, fontSize = 10.sp)
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Card).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (loading) "Haberler alınıyor…" else "Henüz haber bulunamadı", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("Bağlantını kontrol edip yenile düğmesine dokunabilirsin.", color = TextMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { refresh() }) { Text("Yeniden dene", color = Gold) }
                    }
                }
            } else {
                val lead = filtered.first()
                item { LeadStory(lead) { openArticle(context, lead) } }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SON HABERLER", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                        Spacer(Modifier.weight(1f))
                        Text("${filtered.size} haber", color = TextMuted, fontSize = 10.sp)
                    }
                }
                items(filtered.drop(1), key = { it.id }) { article ->
                    NewsRow(article) { openArticle(context, article) }
                }
            }
            item {
                Text("Haber başlıkları ve kısa açıklamalar kaynak akışlarından alınır. Haberin tamamı yayıncı sitesinde açılır.",
                    color = TextMuted, fontSize = 10.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
            }
        }

        NavigationBar(containerColor = Surface, contentColor = TextPrimary, tonalElevation = 0.dp) {
            NavigationBarItem(selected = true, onClick = { selected = "Tümü" }, icon = { Icon(Icons.Outlined.SportsSoccer, null) }, label = { Text("Akış") })
            NavigationBarItem(selected = false, onClick = { selected = "Son Dakika" }, icon = { Icon(Icons.Outlined.Refresh, null) }, label = { Text("Son dakika") })
            NavigationBarItem(selected = false, onClick = { selected = "Galatasaray" }, icon = { Text("GS", fontWeight = FontWeight.Black, color = Gold) }, label = { Text("Takımlar") })
            NavigationBarItem(selected = false, onClick = { searchMode = true }, icon = { Icon(Icons.Outlined.Search, null) }, label = { Text("Keşfet") })
        }
    }
}

@Composable
private fun LeadStory(article: ForzaArticle, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card).clickable(onClick = onClick)) {
        if (!article.imageUrl.isNullOrBlank()) {
            AsyncImage(model = article.imageUrl, contentDescription = article.title, modifier = Modifier.fillMaxWidth().height(205.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        } else {
            Box(Modifier.fillMaxWidth().height(160.dp).background(Brush.linearGradient(listOf(Color(0xFF3B101B), Color(0xFF111722)))), contentAlignment = Alignment.CenterStart) {
                Text("FORZAX  /  SPOR GÜNDEMİ", color = Color.White.copy(alpha = .13f), fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(18.dp))
            }
        }
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ÖNE ÇIKAN", color = Color(0xFFFF6475), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                Spacer(Modifier.width(8.dp))
                Text("·  ${article.source}", color = TextMuted, fontSize = 10.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(article.title, color = TextPrimary, fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.ExtraBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (article.summary.isNotBlank()) {
                Spacer(Modifier.height(7.dp))
                Text(article.summary, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeLabel(article.publishedAt), color = TextMuted, fontSize = 10.sp)
                Spacer(Modifier.weight(1f))
                Text("HABERİ OKU", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Outlined.ChevronRight, null, tint = Gold, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun NewsRow(article: ForzaArticle, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Card).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!article.imageUrl.isNullOrBlank()) {
            AsyncImage(model = article.imageUrl, contentDescription = null, modifier = Modifier.size(104.dp, 86.dp).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        } else {
            Box(Modifier.size(104.dp, 86.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Color(0xFF40121F), CardElevated))), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(30.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(article.title, color = TextPrimary, fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(7.dp))
            Text("${article.source}  ·  ${timeLabel(article.publishedAt)}", color = TextMuted, fontSize = 10.sp, maxLines = 1)
        }
    }
}

private fun openArticle(context: android.content.Context, article: ForzaArticle) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(article.link))) }
}

private fun timeLabel(timestamp: Long): String =
    runCatching { SimpleDateFormat("HH:mm", Locale("tr")).format(java.util.Date(timestamp)) }.getOrDefault("Şimdi")

private fun timeLabel(raw: String): String {
    if (raw.isBlank()) return "Yeni"
    return runCatching {
        val formats = listOf("EEE, dd MMM yyyy HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd HH:mm:ss")
        val date = formats.firstNotNullOfOrNull { pattern -> runCatching { SimpleDateFormat(pattern, Locale.US).parse(raw) }.getOrNull() }
        if (date == null) raw.take(16) else SimpleDateFormat("dd MMM · HH:mm", Locale("tr")).format(date)
    }.getOrDefault(raw.take(16))
}
