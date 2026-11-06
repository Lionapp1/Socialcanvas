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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Tune
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
private const val PREFS = "forzax_reader"
private const val SAVED_IDS = "saved_article_ids"

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var articles by remember { mutableStateOf(ForzaNewsRepository.cached(context)) }
    var selected by remember { mutableStateOf("Tümü") }
    var tab by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var updated by remember { mutableLongStateOf(ForzaNewsRepository.lastUpdated(context)) }
    var searchMode by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var openedArticle by remember { mutableStateOf<ForzaArticle?>(null) }
    var savedIds by remember {
        mutableStateOf(context.getSharedPreferences(PREFS, 0).getStringSet(SAVED_IDS, emptySet())?.toSet() ?: emptySet())
    }

    fun refresh() {
        if (loading) return
        scope.launch {
            loading = true
            runCatching { ForzaNewsRepository.refresh(context) }
                .onSuccess { articles = it; updated = ForzaNewsRepository.lastUpdated(context) }
            loading = false
        }
    }
    fun toggleSaved(article: ForzaArticle) {
        val next = if (article.id in savedIds) savedIds - article.id else savedIds + article.id
        savedIds = next
        context.getSharedPreferences(PREFS, 0).edit().putStringSet(SAVED_IDS, next).apply()
    }
    LaunchedEffect(Unit) { if (articles.isEmpty()) refresh() }

    val filtered = articles.filter { article ->
        val text = "${article.title} ${article.summary}"
        val categoryMatch = when (selected) {
            "Galatasaray" -> listOf("galatasaray", "okan buruk", "sarı-kırmızı", "sarı kırmızı").any { text.contains(it, true) }
            "Süper Lig" -> listOf("süper lig", "fenerbahçe", "beşiktaş", "trabzonspor", "başakşehir").any { text.contains(it, true) }
            "Transfer" -> listOf("transfer", "imza", "anlaşma", "kiralık", "bonservis").any { text.contains(it, true) }
            "Avrupa" -> article.category == "Dünya" || listOf("şampiyonlar ligi", "premier league", "la liga", "serie a").any { text.contains(it, true) }
            "Son Dakika" -> listOf("son dakika", "resmi açıklama", "acil").any { text.contains(it, true) }
            else -> true
        }
        categoryMatch && (query.isBlank() || text.contains(query, true))
    }
    val visibleArticles = if (tab == 1) articles.filter { it.id in savedIds } else filtered

    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        if (openedArticle != null) {
            ReaderScreen(
                article = openedArticle!!,
                saved = openedArticle!!.id in savedIds,
                onBack = { openedArticle = null },
                onSave = { toggleSaved(openedArticle!!) }
            )
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(Color(0xFFE11D36), Color(0xFF8D1028)))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.SportsSoccer, contentDescription = "ForzaX", tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("ForzaX", color = TextPrimary, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = (-.8).sp)
                    Text(if (tab == 1) "OKUMA LİSTEM" else "SPORUN NABZI", color = Color(0xFFE34A5C), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.1.sp)
                }
                IconButton(onClick = { searchMode = !searchMode; tab = 0 }) {
                    Icon(Icons.Outlined.Search, contentDescription = "Haber ara", tint = TextPrimary)
                }
                IconButton(onClick = { refresh() }, enabled = !loading) {
                    if (loading) CircularProgressIndicator(Modifier.size(19.dp), color = Gold, strokeWidth = 2.dp)
                    else Icon(Icons.Outlined.Refresh, contentDescription = "Haberleri yenile", tint = TextPrimary)
                }
            }

            if (searchMode && tab == 0) {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    placeholder = { Text("Haber, takım veya konu ara") }, singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Border, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                Spacer(Modifier.height(10.dp))
            }

            if (tab == 0) {
                Row(Modifier.fillMaxWidth().padding(start = 18.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        val active = selected == category
                        Box(
                            Modifier.clip(CircleShape).background(if (active) Color(0xFFE11D36) else Card)
                                .border(1.dp, if (active) Color(0xFFE11D36) else Border, CircleShape)
                                .clickable { selected = category }.padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(category, color = if (active) Color.White else TextSecondary, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(4.dp).height(17.dp).clip(CircleShape).background(Color(0xFFE11D36)))
                        Spacer(Modifier.width(8.dp))
                        Text(if (tab == 1) "KAYDETTİKLERİM" else if (selected == "Tümü") "GÜNDEM" else selected.uppercase(Locale("tr")), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                        Spacer(Modifier.weight(1f))
                        Text(if (updated > 0) "Son kontrol · ${timeLabel(updated)}" else "Haber akışı", color = TextMuted, fontSize = 10.sp)
                    }
                }
                if (visibleArticles.isEmpty()) {
                    item {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Card).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(if (tab == 1) Icons.Outlined.BookmarkBorder else Icons.Outlined.Explore, null, tint = Gold, modifier = Modifier.size(30.dp))
                            Spacer(Modifier.height(10.dp))
                            Text(if (tab == 1) "Henüz kaydedilmiş haber yok" else if (loading) "Haberler alınıyor…" else "Bu başlıkta haber bulunamadı", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(if (tab == 1) "Daha sonra okumak istediğin haberleri yer imiyle saklayabilirsin." else "Bağlantını kontrol edip haberleri yenileyebilirsin.", color = TextMuted, fontSize = 12.sp)
                            if (tab == 0) TextButton(onClick = { refresh() }) { Text("Yeniden dene", color = Gold) }
                        }
                    }
                } else {
                    if (tab == 0) {
                        val lead = visibleArticles.first()
                        item { LeadStory(lead, saved = lead.id in savedIds, onClick = { openedArticle = lead }, onSave = { toggleSaved(lead) }) }
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("SON HABERLER", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                                Spacer(Modifier.weight(1f))
                                Text("${visibleArticles.size} haber", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                        items(visibleArticles.drop(1), key = { it.id }) { article ->
                            NewsRow(article, saved = article.id in savedIds, onClick = { openedArticle = article }, onSave = { toggleSaved(article) })
                        }
                    } else {
                        items(visibleArticles, key = { it.id }) { article ->
                            NewsRow(article, saved = true, onClick = { openedArticle = article }, onSave = { toggleSaved(article) })
                        }
                    }
                }
                item {
                    Text("Haber özeti ve görseller, yayıncıların RSS akışlarından sağlanır. Tam haber için kaynak bağlantısı haber detayında yer alır.",
                        color = TextMuted, fontSize = 10.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 7.dp, bottom = 10.dp))
                }
            }

            NavigationBar(containerColor = Surface, contentColor = TextPrimary, tonalElevation = 0.dp) {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("Akış") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1; searchMode = false }, icon = { Icon(Icons.Outlined.BookmarkBorder, null) }, label = { Text("Kaydedilen") })
                NavigationBarItem(selected = false, onClick = { tab = 0; selected = "Galatasaray"; searchMode = false }, icon = { Icon(Icons.Outlined.SportsSoccer, null) }, label = { Text("Takımlar") })
                NavigationBarItem(selected = false, onClick = { tab = 0; searchMode = true }, icon = { Icon(Icons.Outlined.Explore, null) }, label = { Text("Keşfet") })
            }
        }
    }
}

@Composable
private fun ReaderScreen(article: ForzaArticle, saved: Boolean, onBack: () -> Unit, onSave: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Geri", tint = TextPrimary) }
            Text("HABER DETAYI", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onSave) {
                Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = if (saved) "Kaydedildi" else "Haberi kaydet", tint = if (saved) Gold else TextPrimary)
            }
            IconButton(onClick = {
                val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.link}") }
                context.startActivity(Intent.createChooser(send, "Haberi paylaş"))
            }) { Icon(Icons.Outlined.Share, contentDescription = "Paylaş", tint = TextPrimary) }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                if (!article.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = article.imageUrl, contentDescription = article.title, modifier = Modifier.fillMaxWidth().height(235.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxWidth().height(170.dp).background(Brush.linearGradient(listOf(Color(0xFF451321), Color(0xFF141923)))), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White.copy(alpha = .55f), modifier = Modifier.size(54.dp))
                    }
                }
                Column(Modifier.padding(horizontal = 22.dp, vertical = 22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.clip(CircleShape).background(Color(0x33E11D36)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(article.source.uppercase(Locale("tr")), color = Color(0xFFFF7786), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(timeLabel(article.publishedAt), color = TextMuted, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(article.title, color = TextPrimary, fontSize = 27.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = Border)
                    Spacer(Modifier.height(18.dp))
                    Text("HABER ÖZETİ", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        article.summary.ifBlank { "Bu yayıncı RSS akışında haberin ayrıntılı metni paylaşılmamış. Haberin tamamını orijinal kaynağından okuyabilirsin." },
                        color = TextPrimary, fontSize = 17.sp, lineHeight = 29.sp
                    )
                    Spacer(Modifier.height(22.dp))
                    Text("Bu içerik ${article.source} tarafından yayımlanmıştır. ForzaX haber başlığını ve mevcut kısa özeti kaynak göstererek sunar.", color = TextMuted, fontSize = 12.sp, lineHeight = 19.sp)
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(article.link))) } },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D36), contentColor = Color.White)
                    ) {
                        Text("Kaynağın tamamını oku", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.OpenInNew, null, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LeadStory(article: ForzaArticle, saved: Boolean, onClick: () -> Unit, onSave: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card)) {
        Box {
            if (!article.imageUrl.isNullOrBlank()) {
                AsyncImage(model = article.imageUrl, contentDescription = article.title, modifier = Modifier.fillMaxWidth().height(205.dp).clickable(onClick = onClick), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxWidth().height(160.dp).background(Brush.linearGradient(listOf(Color(0xFF3B101B), Color(0xFF111722)))).clickable(onClick = onClick), contentAlignment = Alignment.CenterStart) {
                    Text("FORZAX  /  SPOR GÜNDEMİ", color = Color.White.copy(alpha = .13f), fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(18.dp))
                }
            }
        }
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ÖNE ÇIKAN", color = Color(0xFFFF6475), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                Spacer(Modifier.width(8.dp))
                Text("·  ${article.source}", color = TextMuted, fontSize = 10.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSave, modifier = Modifier.size(30.dp)) {
                    Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, null, tint = if (saved) Gold else TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
                Spacer(Modifier.height(4.dp))
                Text(article.title, color = TextPrimary, fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.ExtraBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                if (article.summary.isNotBlank()) {
                    Spacer(Modifier.height(7.dp))
                    Text(article.summary, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
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
}

@Composable
private fun NewsRow(article: ForzaArticle, saved: Boolean, onClick: () -> Unit, onSave: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Card).padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(104.dp, 88.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)) {
            if (!article.imageUrl.isNullOrBlank()) {
                AsyncImage(model = article.imageUrl, contentDescription = article.title, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF40121F), CardElevated))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(30.dp))
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).clickable(onClick = onClick)) {
            Text(article.title, color = TextPrimary, fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(7.dp))
            Text("${article.source}  ·  ${timeLabel(article.publishedAt)}", color = TextMuted, fontSize = 10.sp, maxLines = 1)
        }
        IconButton(onClick = onSave, modifier = Modifier.size(34.dp)) {
            Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Kaydet", tint = if (saved) Gold else TextMuted, modifier = Modifier.size(18.dp))
        }
    }
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
