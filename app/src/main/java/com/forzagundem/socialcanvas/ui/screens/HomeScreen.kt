package com.forzagundem.socialcanvas.ui.screens

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.forzagundem.socialcanvas.data.ForzaArticle
import com.forzagundem.socialcanvas.data.ForzaNewsRepository
import com.forzagundem.socialcanvas.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private val categories = listOf("Tumu", "Galatasaray", "Super Lig", "Transfer", "Avrupa", "Son Dakika")
private const val PREFS = "forzax_reader"
private const val SAVED_IDS = "saved_article_ids"

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var articles by remember { mutableStateOf(ForzaNewsRepository.cached(context)) }
    var selected by remember { mutableStateOf("Tumu") }
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
                .onSuccess {
                    articles = it
                    updated = ForzaNewsRepository.lastUpdated(context)
                }
            loading = false
        }
    }

    fun toggleSaved(article: ForzaArticle) {
        val next = if (article.id in savedIds) savedIds - article.id else savedIds + article.id
        savedIds = next
        context.getSharedPreferences(PREFS, 0).edit().putStringSet(SAVED_IDS, next).apply()
    }

    fun openArticle(article: ForzaArticle) {
        openedArticle = article
        scope.launch {
            val full = runCatching { ForzaNewsRepository.ensureFull(context, article) }.getOrDefault(article)
            if (openedArticle?.link == full.link) openedArticle = full
            articles = articles.map { if (it.link == full.link) full else it }
        }
    }

    LaunchedEffect(Unit) { if (articles.isEmpty()) refresh() }

    BackHandler(enabled = openedArticle != null) {
        openedArticle = null
    }

    val filtered = articles.filter { article ->
        val text = "${article.title} ${article.summary} ${article.fullContent}"
        val categoryMatch = when (selected) {
            "Galatasaray" -> listOf("galatasaray", "okan buruk").any { text.contains(it, true) }
            "Super Lig" -> listOf("super lig", "fenerbahce", "besiktas", "trabzonspor", "basaksehir", "super lig").any { text.contains(it, true) }
            "Transfer" -> listOf("transfer", "imza", "anlasma", "kiralik", "bonservis").any { text.contains(it, true) }
            "Avrupa" -> article.category == "Dunya" || article.category == "Avrupa" ||
                listOf("sampiyonlar", "premier league", "la liga", "serie a").any { text.contains(it, true) }
            "Son Dakika" -> listOf("son dakika", "resmi aciklama", "acil").any { text.contains(it, true) }
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
                onSave = { toggleSaved(openedArticle!!) },
                onArticleUpdated = { updatedArticle ->
                    openedArticle = updatedArticle
                    articles = articles.map { if (it.link == updatedArticle.link) updatedArticle else it }
                }
            )
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE11D36), Color(0xFF8D1028)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.SportsSoccer, contentDescription = "ForzaX", tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("ForzaX", color = TextPrimary, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = (-.8).sp)
                    Text(
                        if (tab == 1) "OKUMA LISTEM" else "SPORUN NABZI",
                        color = Color(0xFFE34A5C), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.1.sp
                    )
                }
                IconButton(onClick = { searchMode = !searchMode; tab = 0 }) {
                    Icon(Icons.Outlined.Search, contentDescription = "Ara", tint = TextPrimary)
                }
                IconButton(onClick = { refresh() }, enabled = !loading) {
                    if (loading) CircularProgressIndicator(Modifier.size(19.dp), color = Gold, strokeWidth = 2.dp)
                    else Icon(Icons.Outlined.Refresh, contentDescription = "Yenile", tint = TextPrimary)
                }
            }

            if (searchMode && tab == 0) {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    placeholder = { Text("Haber, takim veya konu ara") }, singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold, unfocusedBorderColor = Border,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(Modifier.height(10.dp))
            }

            if (tab == 0) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .padding(start = 18.dp, end = 18.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val active = selected == category
                        Box(
                            Modifier.clip(CircleShape).background(if (active) Color(0xFFE11D36) else Card)
                                .border(1.dp, if (active) Color(0xFFE11D36) else Border, CircleShape)
                                .clickable { selected = category }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                category,
                                color = if (active) Color.White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            )
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
                        Text(
                            if (tab == 1) "KAYDETTIKLERIM" else if (selected == "Tumu") "GUNDEM" else selected.uppercase(Locale.ROOT),
                            color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            if (updated > 0) "Son · ${timeLabel(updated)}" else "Haber akisi",
                            color = TextMuted, fontSize = 10.sp
                        )
                    }
                }
                if (visibleArticles.isEmpty()) {
                    item {
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Card).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (tab == 1) Icons.Outlined.BookmarkBorder else Icons.Outlined.Explore,
                                null, tint = Gold, modifier = Modifier.size(30.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                if (tab == 1) "Henuz kayitli haber yok"
                                else if (loading) "Haberler aliniyor..."
                                else "Bu baslikta haber yok",
                                color = TextPrimary, fontWeight = FontWeight.Bold
                            )
                            if (tab == 0) TextButton(onClick = { refresh() }) {
                                Text("Yeniden dene", color = Gold)
                            }
                        }
                    }
                } else {
                    if (tab == 0) {
                        val lead = visibleArticles.first()
                        item {
                            LeadStory(
                                lead,
                                saved = lead.id in savedIds,
                                onClick = { openArticle(lead) },
                                onSave = { toggleSaved(lead) }
                            )
                        }
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "SON HABERLER",
                                    color = TextPrimary, fontSize = 12.sp,
                                    fontWeight = FontWeight.Black, letterSpacing = 1.2.sp
                                )
                                Spacer(Modifier.weight(1f))
                                Text("${visibleArticles.size} haber", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                        items(visibleArticles.drop(1), key = { it.id }) { article ->
                            NewsRow(
                                article,
                                saved = article.id in savedIds,
                                onClick = { openArticle(article) },
                                onSave = { toggleSaved(article) }
                            )
                        }
                    } else {
                        items(visibleArticles, key = { it.id }) { article ->
                            NewsRow(
                                article, saved = true,
                                onClick = { openArticle(article) },
                                onSave = { toggleSaved(article) }
                            )
                        }
                    }
                }
            }

            NavigationBar(containerColor = Surface, contentColor = TextPrimary, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("Akis") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1; searchMode = false },
                    icon = { Icon(Icons.Outlined.BookmarkBorder, null) }, label = { Text("Kaydedilen") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { tab = 0; selected = "Galatasaray"; searchMode = false },
                    icon = { Icon(Icons.Outlined.SportsSoccer, null) }, label = { Text("Takimlar") }
                )
                NavigationBarItem(
                    selected = false, onClick = { tab = 0; searchMode = true },
                    icon = { Icon(Icons.Outlined.Explore, null) }, label = { Text("Kesfet") }
                )
            }
        }
    }
}

@Composable
private fun ReaderScreen(
    article: ForzaArticle,
    saved: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onArticleUpdated: (ForzaArticle) -> Unit
) {
    val context = LocalContext.current
    var showSourcePage by remember(article.link) { mutableStateOf(false) }
    var pageLoading by remember(article.link) { mutableStateOf(false) }
    var loadingFull by remember(article.link) { mutableStateOf(article.fullContent.length < 320) }

    BackHandler(enabled = showSourcePage) { showSourcePage = false }

    LaunchedEffect(article.link) {
        if (article.fullContent.length < 320) {
            loadingFull = true
            val full = runCatching { ForzaNewsRepository.ensureFull(context, article) }.getOrDefault(article)
            onArticleUpdated(full)
            loadingFull = false
        } else {
            loadingFull = false
        }
    }

    val body = article.fullContent.ifBlank { article.summary }.ifBlank {
        "Bu haberin metni henuz yuklenemedi. Kaynak sayfadan okuyabilirsin."
    }
    val paragraphs = body
        .split(Regex("\\n\\s*\\n|\\n"))
        .map { it.trim() }
        .filter { it.isNotBlank() }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (showSourcePage) showSourcePage = false else onBack() }) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
            }
            Text(
                if (showSourcePage) article.source.uppercase(Locale.ROOT) else "HABER",
                color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp,
                modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onSave) {
                Icon(
                    if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Kaydet",
                    tint = if (saved) Gold else TextPrimary
                )
            }
            IconButton(onClick = {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.link}")
                }
                context.startActivity(Intent.createChooser(send, "Paylas"))
            }) {
                Icon(Icons.Outlined.Share, contentDescription = "Paylas", tint = TextPrimary)
            }
            IconButton(onClick = {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(article.link)))
                }
            }) {
                Icon(Icons.Outlined.OpenInNew, contentDescription = "Tarayici", tint = TextPrimary)
            }
        }

        if (loadingFull && !showSourcePage) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Gold,
                trackColor = Surface
            )
        }

        if (showSourcePage) {
            if (pageLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Gold, trackColor = Surface)
            AndroidView(
                modifier = Modifier.fillMaxSize().background(Color.White),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                pageLoading = true
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                pageLoading = false
                            }
                        }
                        loadUrl(article.link)
                    }
                },
                update = { view -> if (view.url == null) view.loadUrl(article.link) }
            )
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 28.dp)) {
                item {
                    if (!article.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = article.imageUrl,
                            contentDescription = article.title,
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            Modifier.fillMaxWidth().height(150.dp)
                                .background(Brush.linearGradient(listOf(Color(0xFF451321), Color(0xFF141923)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.SportsSoccer, null,
                                tint = Color.White.copy(alpha = .5f), modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                item {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.clip(CircleShape).background(Color(0x33E11D36))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    article.source.uppercase(Locale.ROOT),
                                    color = Color(0xFFFF7786), fontSize = 10.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(timeLabel(article.publishedAt), color = TextMuted, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            article.title,
                            color = TextPrimary,
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Border)
                        Spacer(Modifier.height(8.dp))
                    }
                }
                items(paragraphs) { para ->
                    Text(
                        para,
                        color = TextPrimary,
                        fontSize = 16.5.sp,
                        lineHeight = 28.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Kaynak: ${article.source}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { showSourcePage = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE11D36),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Outlined.OpenInNew, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Kaynak sayfayi ac", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeadStory(article: ForzaArticle, saved: Boolean, onClick: () -> Unit, onSave: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Card)) {
        Box(Modifier.fillMaxWidth().height(200.dp).clickable(onClick = onClick)) {
            if (!article.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = article.imageUrl, contentDescription = article.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF40121F), CardElevated))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(40.dp))
                }
            }
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)))
                )
            )
            IconButton(
                onClick = onSave,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(36.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(
                    if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                    null, tint = if (saved) Gold else Color.White, modifier = Modifier.size(18.dp)
                )
            }
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp).fillMaxWidth()) {
                Text(
                    article.title, color = Color.White, fontSize = 20.sp, lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold, maxLines = 3, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(timeLabel(article.publishedAt), color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    Spacer(Modifier.weight(1f))
                    Text("OKU", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                AsyncImage(
                    model = article.imageUrl, contentDescription = article.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF40121F), CardElevated))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(28.dp))
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).clickable(onClick = onClick)) {
            Text(
                article.title, color = TextPrimary, fontSize = 14.sp, lineHeight = 19.sp,
                fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${article.source}  ·  ${timeLabel(article.publishedAt)}",
                color = TextMuted, fontSize = 10.sp, maxLines = 1
            )
        }
        IconButton(onClick = onSave, modifier = Modifier.size(34.dp)) {
            Icon(
                if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = "Kaydet",
                tint = if (saved) Gold else TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun timeLabel(timestamp: Long): String =
    runCatching { SimpleDateFormat("HH:mm", Locale("tr")).format(java.util.Date(timestamp)) }
        .getOrDefault("Simdi")

private fun timeLabel(raw: String): String {
    if (raw.isBlank()) return "Yeni"
    return runCatching {
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss"
        )
        val date = formats.firstNotNullOfOrNull { pattern ->
            runCatching { SimpleDateFormat(pattern, Locale.US).parse(raw) }.getOrNull()
        }
        if (date == null) raw.take(16)
        else SimpleDateFormat("dd MMM · HH:mm", Locale("tr")).format(date)
    }.getOrDefault(raw.take(16))
}
