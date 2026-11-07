package com.forzagundem.socialcanvas.ui.screens

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

private val categories = listOf("Tumu", "Galatasaray", "Milli Takim", "Super Lig", "Transfer", "Avrupa", "Son Dakika")
private const val PREFS = "forzax_reader"
private const val SAVED_IDS = "saved_article_ids"
private const val KEY_SOUND = "click_sound"
private const val KEY_FONT = "font_scale"
private val Red = Color(0xFFE11D36)
private val RedDark = Color(0xFF9B1224)
private val ChipBg = Color(0xFF1A1D28)

private fun playClick(enabled: Boolean) {
    if (!enabled) return
    runCatching {
        val tg = ToneGenerator(AudioManager.STREAM_SYSTEM, 55)
        tg.startTone(ToneGenerator.TONE_PROP_ACK, 55)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            runCatching { tg.release() }
        }, 120)
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences(PREFS, 0) }
    var articles by remember { mutableStateOf(ForzaNewsRepository.cached(context)) }
    var selected by remember { mutableStateOf("Tumu") }
    var tab by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var updated by remember { mutableLongStateOf(ForzaNewsRepository.lastUpdated(context)) }
    var searchMode by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var openedArticle by remember { mutableStateOf<ForzaArticle?>(null) }
    var savedIds by remember {
        mutableStateOf(prefs.getStringSet(SAVED_IDS, emptySet())?.toSet() ?: emptySet())
    }
    var soundOn by remember { mutableStateOf(prefs.getBoolean(KEY_SOUND, true)) }
    var fontScale by remember { mutableFloatStateOf(prefs.getFloat(KEY_FONT, 1f)) }

    fun click() = playClick(soundOn)

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
        click()
        val next = if (article.id in savedIds) savedIds - article.id else savedIds + article.id
        savedIds = next
        prefs.edit().putStringSet(SAVED_IDS, next).apply()
    }

    fun openArticle(article: ForzaArticle) {
        click()
        openedArticle = article
        scope.launch {
            val full = runCatching { ForzaNewsRepository.ensureFull(context, article) }.getOrDefault(article)
            if (openedArticle?.link == full.link) openedArticle = full
            articles = articles.map { if (it.link == full.link) full else it }
        }
    }

    fun shareArticle(article: ForzaArticle) {
        click()
        val text = "${article.title}\n\n${article.summary.take(180)}\n\n${article.link}\n\nForzaX"
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, article.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, "Haberi paylas"))
    }

    LaunchedEffect(Unit) { if (articles.isEmpty()) refresh() }
    BackHandler(enabled = openedArticle != null) { openedArticle = null }

    val filtered = articles.filter { article ->
        val text = "${article.title} ${article.summary} ${article.fullContent} ${article.category} ${article.source}"
        val categoryMatch = when (selected) {
            "Galatasaray" -> listOf("galatasaray", "gala", "okan buruk", "icardi", "muslera").any { text.contains(it, true) } ||
                article.category.contains("Galatasaray", true) || article.source.contains("Galatasaray", true)
            "Milli Takim" -> listOf("milli takim", "milli takım", "a milli", "turkiye", "türkiye", "montella", "uefa").any { text.contains(it, true) } ||
                article.category.contains("Milli", true) || article.source.contains("Milli", true)
            "Super Lig" -> listOf("super lig", "süper lig", "fenerbahce", "besiktas", "trabzonspor", "basaksehir").any { text.contains(it, true) } ||
                article.category.contains("Super", true)
            "Transfer" -> listOf("transfer", "imza", "anlasma", "kiralik", "bonservis", "teklif").any { text.contains(it, true) }
            "Avrupa" -> article.category.contains("Avrupa", true) || article.category.contains("Dunya", true) ||
                listOf("sampiyonlar", "premier", "la liga", "serie a", "bundesliga").any { text.contains(it, true) }
            "Son Dakika" -> listOf("son dakika", "resmi", "acil", "breaking").any { text.contains(it, true) }
            else -> true
        }
        val q = query.trim()
        categoryMatch && (q.isBlank() || text.contains(q, true))
    }

    Box(Modifier.fillMaxSize().background(Bg)) {
        if (openedArticle != null) {
            ReaderScreen(
                article = openedArticle!!,
                saved = openedArticle!!.id in savedIds,
                fontScale = fontScale,
                onBack = { openedArticle = null },
                onSave = { toggleSaved(openedArticle!!) },
                onShare = { shareArticle(openedArticle!!) },
                onArticleUpdated = { a ->
                    openedArticle = a
                    articles = articles.map { if (it.link == a.link) a else it }
                }
            )
        } else {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        0 -> FeedPage(
                            articles = filtered, selected = selected,
                            onSelectCategory = { click(); selected = it },
                            searchMode = searchMode,
                            onToggleSearch = { click(); searchMode = !searchMode; if (!searchMode) query = "" },
                            query = query, onQuery = { query = it },
                            loading = loading, updated = updated, savedIds = savedIds,
                            onRefresh = { click(); refresh() },
                            onOpen = { openArticle(it) }, onSave = { toggleSaved(it) }
                        )
                        1 -> SquadPage()
                        2 -> SavedPage(
                            articles = articles.filter { it.id in savedIds },
                            onOpen = { openArticle(it) }, onSave = { toggleSaved(it) }
                        )
                        3 -> SettingsPage(
                            articleCount = articles.size, savedCount = savedIds.size,
                            updated = updated, loading = loading,
                            soundOn = soundOn, fontScale = fontScale,
                            onSound = {
                                soundOn = it
                                prefs.edit().putBoolean(KEY_SOUND, it).apply()
                                if (it) playClick(true)
                            },
                            onFont = {
                                fontScale = it
                                prefs.edit().putFloat(KEY_FONT, it).apply()
                            },
                            onRefresh = { click(); refresh() },
                            onClear = {
                                click()
                                ForzaNewsRepository.clearCache(context)
                                articles = emptyList()
                                savedIds = emptySet()
                                prefs.edit().remove(SAVED_IDS).apply()
                                updated = 0L
                            }
                        )
                    }
                }
                NavigationBar(
                    containerColor = Surface,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                ) {
                    AnimatedNavItem(tab == 0, if (tab == 0) Icons.Filled.Home else Icons.Outlined.Home, "Akis") { click(); tab = 0 }
                    AnimatedNavItem(tab == 1, Icons.Outlined.SportsSoccer, "Kadro") { click(); tab = 1; searchMode = false }
                    AnimatedNavItem(tab == 2, if (tab == 2) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, "Kayitlar") { click(); tab = 2 }
                    AnimatedNavItem(tab == 3, if (tab == 3) Icons.Filled.Settings else Icons.Outlined.Settings, "Ayarlar") { click(); tab = 3 }
                }
            }
        }
    }
}

@Composable
private fun RowScope.AnimatedNavItem(selected: Boolean, icon: ImageVector, label: String, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "nav"
    )
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label, modifier = Modifier.scale(scale).size(24.dp)) },
        label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Gold,
            selectedTextColor = Gold,
            indicatorColor = Red.copy(alpha = 0.18f),
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted
        )
    )
}

@Composable
private fun PressScale(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "press"
    )
    Box(modifier = modifier.scale(scale).clickable(interactionSource = interaction, indication = null, onClick = onClick)) { content() }
}

@Composable
private fun TopBar(title: String, subtitle: String, loading: Boolean = false, onSearch: (() -> Unit)? = null, onRefresh: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Red, RedDark))),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
            Text(subtitle, color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
        }
        if (onSearch != null) IconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, contentDescription = "Ara", tint = TextPrimary) }
        if (onRefresh != null) IconButton(onClick = onRefresh, enabled = !loading) {
            if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = Gold, strokeWidth = 2.dp)
            else Icon(Icons.Outlined.Refresh, contentDescription = "Yenile", tint = TextPrimary)
        }
    }
}

@Composable
private fun FeedPage(
    articles: List<ForzaArticle>, selected: String, onSelectCategory: (String) -> Unit,
    searchMode: Boolean, onToggleSearch: () -> Unit, query: String, onQuery: (String) -> Unit,
    loading: Boolean, updated: Long, savedIds: Set<String>, onRefresh: () -> Unit,
    onOpen: (ForzaArticle) -> Unit, onSave: (ForzaArticle) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopBar("ForzaX", "SPORUN NABZI", loading, onToggleSearch, onRefresh)
        if (searchMode) {
            OutlinedTextField(
                value = query, onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Galatasaray, milli takim, transfer...") },
                singleLine = true, shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextMuted) },
                trailingIcon = {
                    if (query.isNotBlank()) IconButton(onClick = { onQuery("") }) {
                        Icon(Icons.Outlined.Close, null, tint = TextMuted)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold, unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Gold
                )
            )
            Spacer(Modifier.height(8.dp))
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val active = selected == category
                PressScale(onClick = { onSelectCategory(category) }) {
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (active) Brush.horizontalGradient(listOf(Red, RedDark)) else Brush.horizontalGradient(listOf(ChipBg, ChipBg)))
                            .border(1.dp, if (active) Gold.copy(alpha = 0.5f) else Border, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(category, color = if (active) Color.White else TextSecondary, fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(3.dp).height(16.dp).clip(CircleShape).background(Gold))
                    Spacer(Modifier.width(8.dp))
                    Text(if (selected == "Tumu") "GUNDEM" else selected.uppercase(Locale.ROOT),
                        color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                    Spacer(Modifier.weight(1f))
                    Text(if (updated > 0) "Son · ${timeLabel(updated)}" else "Haber akisi", color = TextMuted, fontSize = 10.sp)
                }
            }
            if (articles.isEmpty()) {
                item { EmptyCard(if (loading) "Haberler aliniyor..." else "Bu baslikta haber yok") { if (!loading) onRefresh() } }
            } else {
                item { LeadStory(articles.first(), articles.first().id in savedIds, { onOpen(articles.first()) }, { onSave(articles.first()) }) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SON HABERLER", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
                        Spacer(Modifier.weight(1f))
                        Text("${articles.size} haber", color = TextMuted, fontSize = 10.sp)
                    }
                }
                items(articles.drop(1), key = { it.id }) { article ->
                    NewsRow(article, article.id in savedIds, { onOpen(article) }, { onSave(article) })
                }
            }
        }
    }
}

@Composable
private fun SavedPage(articles: List<ForzaArticle>, onOpen: (ForzaArticle) -> Unit, onSave: (ForzaArticle) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopBar("ForzaX", "OKUMA LISTEM")
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (articles.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.BookmarkBorder, null, tint = Gold, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Henuz kayitli haber yok", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("Haberlerde yer imine basarak buraya ekle", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                item {
                    Text("${articles.size} kayitli haber", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                items(articles, key = { it.id }) { a -> NewsRow(a, true, { onOpen(a) }, { onSave(a) }) }
            }
        }
    }
}

@Composable
private fun SettingsPage(
    articleCount: Int, savedCount: Int, updated: Long, loading: Boolean,
    soundOn: Boolean, fontScale: Float,
    onSound: (Boolean) -> Unit, onFont: (Float) -> Unit,
    onRefresh: () -> Unit, onClear: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopBar("ForzaX", "AYARLAR")
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("GENEL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            SettingsCard("Haberler", "$articleCount kayit")
            SettingsCard("Okuma listesi", "$savedCount haber")
            SettingsCard("Son senkron", if (updated > 0) timeLabel(updated) else "Henuz yok")
            SettingsCard("Arka plan", "15 dk otomatik")

            Text("GORUNUM", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Card).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.TextFields, null, tint = Gold, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Yazi boyutu", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(when {
                        fontScale < 0.95f -> "Kucuk"
                        fontScale > 1.1f -> "Buyuk"
                        else -> "Normal"
                    }, color = TextMuted, fontSize = 12.sp)
                }
            }
            Slider(
                value = fontScale, onValueChange = onFont, valueRange = 0.85f..1.25f, steps = 3,
                colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Red, inactiveTrackColor = Border)
            )

            Text("SES", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Card).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(if (soundOn) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeOff, null, tint = if (soundOn) Gold else TextMuted)
                Spacer(Modifier.width(12.dp))
                Text("Tiklama sesi", color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Switch(
                    checked = soundOn, onCheckedChange = onSound,
                    colors = SwitchDefaults.colors(checkedThumbColor = Gold, checkedTrackColor = Red.copy(alpha = 0.5f))
                )
            }

            SettingsCard("Surum", "ForzaX 1.8.0")
            SettingsCard("Tema", "Koyu · Kirmizi / Sari")

            Button(
                onClick = onRefresh, enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = Color.White)
            ) {
                Icon(Icons.Outlined.Sync, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (loading) "Guncelleniyor..." else "Simdi guncelle", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onClear, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
            ) {
                Icon(Icons.Outlined.DeleteOutline, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Onbellegi temizle", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, value: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Card).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun EmptyCard(msg: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.Explore, null, tint = Gold, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(10.dp))
        Text(msg, color = TextPrimary, fontWeight = FontWeight.Bold)
        if (msg.contains("yok")) TextButton(onClick = onRetry) { Text("Yeniden dene", color = Gold) }
    }
}

@Composable
private fun ReaderScreen(
    article: ForzaArticle, saved: Boolean, fontScale: Float,
    onBack: () -> Unit, onSave: () -> Unit, onShare: () -> Unit,
    onArticleUpdated: (ForzaArticle) -> Unit
) {
    val context = LocalContext.current
    var showSource by remember(article.link) { mutableStateOf(false) }
    var pageLoading by remember(article.link) { mutableStateOf(false) }
    var loadingFull by remember(article.link) { mutableStateOf(article.fullContent.length < 280) }
    BackHandler(enabled = showSource) { showSource = false }
    LaunchedEffect(article.link) {
        if (article.fullContent.length < 280) {
            loadingFull = true
            val full = runCatching { ForzaNewsRepository.ensureFull(context, article) }.getOrDefault(article)
            onArticleUpdated(full)
            loadingFull = false
        } else loadingFull = false
    }
    val body = article.fullContent.ifBlank { article.summary }.ifBlank {
        "Bu haberin metni henuz yuklenemedi. Ust bardan kaynak sayfayi acabilirsin."
    }
    val paragraphs = body.split(Regex("\\n\\s*\\n|\\n")).map { it.trim() }.filter { it.isNotBlank() }
    val titleSp = (24 * fontScale).sp
    val bodySp = (16.5f * fontScale).sp
    val bodyLh = (28 * fontScale).sp

    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().navigationBarsPadding()) {
        Surface(color = Surface) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (showSource) showSource = false else onBack() }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text(if (showSource) "KAYNAK" else "HABER", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Text(article.source, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onSave) {
                    Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, null, tint = if (saved) Gold else TextPrimary)
                }
                IconButton(onClick = onShare) { Icon(Icons.Outlined.Share, null, tint = TextPrimary) }
                IconButton(onClick = { showSource = true }) { Icon(Icons.Outlined.OpenInNew, null, tint = TextPrimary) }
            }
        }
        if (loadingFull && !showSource) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Gold, trackColor = Surface)
        if (showSource) {
            if (pageLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Gold, trackColor = Surface)
            AndroidView(
                modifier = Modifier.fillMaxSize().background(Color.White),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) { pageLoading = true }
                            override fun onPageFinished(view: WebView?, url: String?) { pageLoading = false }
                        }
                        loadUrl(article.link)
                    }
                },
                update = { v -> if (v.url == null) v.loadUrl(article.link) }
            )
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 28.dp)) {
                item {
                    if (!article.imageUrl.isNullOrBlank()) {
                        AsyncImage(model = article.imageUrl, contentDescription = article.title, modifier = Modifier.fillMaxWidth().height(230.dp), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.fillMaxWidth().height(140.dp).background(Brush.linearGradient(listOf(Color(0xFF451321), Color(0xFF141923)))), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(44.dp))
                        }
                    }
                }
                item {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Red.copy(alpha = 0.18f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text(article.source.uppercase(Locale.ROOT), color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(timeLabel(article.publishedAt), color = TextMuted, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(article.title, color = TextPrimary, fontSize = titleSp, lineHeight = (titleSp.value * 1.3f).sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Border.copy(alpha = 0.55f))
                    }
                }
                items(paragraphs) { para ->
                    Text(para, color = TextPrimary, fontSize = bodySp, lineHeight = bodyLh, letterSpacing = 0.12.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Kaynak: ${article.source}", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
        }
    }
}

@Composable
private fun LeadStory(article: ForzaArticle, saved: Boolean, onClick: () -> Unit, onSave: () -> Unit) {
    PressScale(onClick = onClick) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card)) {
            Box(Modifier.fillMaxWidth().height(200.dp)) {
                if (!article.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = article.imageUrl, contentDescription = article.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF40121F), CardElevated))), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(36.dp))
                    }
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))))
                IconButton(onClick = onSave, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(34.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
                    Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, null, tint = if (saved) Gold else Color.White, modifier = Modifier.size(17.dp))
                }
                Column(Modifier.align(Alignment.BottomStart).padding(14.dp).fillMaxWidth()) {
                    Text(article.title, color = Color.White, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(timeLabel(article.publishedAt), color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Spacer(Modifier.weight(1f))
                        Text("OKU", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Outlined.ChevronRight, null, tint = Gold, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsRow(article: ForzaArticle, saved: Boolean, onClick: () -> Unit, onSave: () -> Unit) {
    PressScale(onClick = onClick) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Card).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(96.dp, 80.dp).clip(RoundedCornerShape(12.dp))) {
                if (!article.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = article.imageUrl, contentDescription = article.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF40121F), CardElevated))), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.SportsSoccer, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                    }
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(article.title, color = TextPrimary, fontSize = 13.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text("${article.source}  ·  ${timeLabel(article.publishedAt)}", color = TextMuted, fontSize = 10.sp, maxLines = 1)
            }
            IconButton(onClick = onSave, modifier = Modifier.size(32.dp)) {
                Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, null, tint = if (saved) Gold else TextMuted, modifier = Modifier.size(17.dp))
            }
        }
    }
}

private fun timeLabel(timestamp: Long): String =
    runCatching { SimpleDateFormat("HH:mm", Locale("tr")).format(java.util.Date(timestamp)) }.getOrDefault("Simdi")

private fun timeLabel(raw: String): String {
    if (raw.isBlank()) return "Yeni"
    return runCatching {
        val formats = listOf("EEE, dd MMM yyyy HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd HH:mm:ss")
        val date = formats.firstNotNullOfOrNull { p -> runCatching { SimpleDateFormat(p, Locale.US).parse(raw) }.getOrNull() }
        if (date == null) raw.take(16) else SimpleDateFormat("dd MMM · HH:mm", Locale("tr")).format(date)
    }.getOrDefault(raw.take(16))
}
