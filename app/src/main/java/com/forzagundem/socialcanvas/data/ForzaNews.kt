package com.forzagundem.socialcanvas.data

import android.content.Context
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class ForzaArticle(
    val id: String,
    val title: String,
    val summary: String,
    val fullContent: String,
    val link: String,
    val source: String,
    val category: String,
    val imageUrl: String?,
    val publishedAt: String
)

object ForzaNewsRepository {
    private const val PREFS = "forzax_news"
    private const val ITEMS = "articles"
    private const val UPDATED = "updated_at"
    private const val MAX_FULL_FETCH = 18
    private const val MIN_SUMMARY_FOR_SKIP = 400

    private val feeds = listOf(
        Feed("A Spor", "https://www.aspor.com.tr/rss/news.xml", "Gündem"),
        Feed("A Spor · Galatasaray", "https://www.aspor.com.tr/rss/galatasaray.xml", "Galatasaray"),
        Feed("A Spor · Süper Lig", "https://www.aspor.com.tr/rss/spor-toto-super-lig.xml", "Süper Lig"),
        Feed("A Spor · Milli Takım", "https://www.aspor.com.tr/rss/millitakim.xml", "Milli Takım"),
        Feed("A Spor · Şampiyonlar Ligi", "https://www.aspor.com.tr/rss/sampiyonlar-ligi.xml", "Avrupa"),
        Feed("A Spor · Avrupa Ligi", "https://www.aspor.com.tr/rss/avrupa-ligi.xml", "Avrupa"),
        Feed("TRT Spor", "https://www.trthaber.com/spor_articles.rss", "Spor"),
        Feed("Habertürk", "https://www.haberturk.com/rss/spor.xml", "Spor"),
        Feed("Sky Sports", "https://www.skysports.com/rss/11095", "Dünya")
    )
    private data class Feed(val source: String, val url: String, val category: String)

    private val fullCache = ConcurrentHashMap<String, String>()

    fun cached(context: Context): List<ForzaArticle> = runCatching {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ITEMS, "[]") ?: "[]"
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            val summary = o.optString("summary")
            val full = o.optString("fullContent").ifBlank { summary }
            ForzaArticle(
                id = o.getString("id"),
                title = o.getString("title"),
                summary = summary,
                fullContent = full,
                link = o.getString("link"),
                source = o.getString("source"),
                category = o.getString("category"),
                imageUrl = o.optString("image").takeIf { it.isNotBlank() },
                publishedAt = o.optString("publishedAt")
            )
        }
    }.getOrDefault(emptyList())

    fun lastUpdated(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(UPDATED, 0L)

    suspend fun refresh(context: Context): List<ForzaArticle> = withContext(Dispatchers.IO) {
        val collected = mutableListOf<ForzaArticle>()
        feeds.forEach { feed ->
            runCatching { collected += readFeed(feed) }
        }

        val previous = cached(context).associateBy { it.link }
        var merged = (collected + previous.values)
            .distinctBy { it.link }
            .sortedByDescending { publishedEpoch(it.publishedAt) }
            .take(250)

        // Prefer previous fullContent when new RSS item is still short
        merged = merged.map { item ->
            val old = previous[item.link]
            if (old != null && old.fullContent.length > item.fullContent.length) {
                item.copy(fullContent = old.fullContent, summary = if (item.summary.length < 80) old.summary else item.summary)
            } else item
        }

        val needFull = merged
            .filter { it.fullContent.length < MIN_SUMMARY_FOR_SKIP }
            .take(MAX_FULL_FETCH)

        if (needFull.isNotEmpty()) {
            val enriched = fetchFullContents(needFull)
            val map = enriched.associateBy { it.link }
            merged = merged.map { map[it.link] ?: it }
        }

        if (merged.isNotEmpty()) {
            val json = JSONArray()
            merged.forEach { a ->
                json.put(
                    JSONObject()
                        .put("id", a.id)
                        .put("title", a.title)
                        .put("summary", a.summary)
                        .put("fullContent", a.fullContent)
                        .put("link", a.link)
                        .put("source", a.source)
                        .put("category", a.category)
                        .put("image", a.imageUrl ?: "")
                        .put("publishedAt", a.publishedAt)
                )
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(ITEMS, json.toString())
                .putLong(UPDATED, System.currentTimeMillis())
                .apply()
        }
        merged
    }

    private suspend fun fetchFullContents(articles: List<ForzaArticle>): List<ForzaArticle> = coroutineScope {
        articles.map { article ->
            async(Dispatchers.IO) {
                val cachedText = fullCache[article.link]
                val full = cachedText ?: runCatching { extractFullArticle(article.link) }.getOrNull()
                    ?.takeIf { it.length > 120 }

                if (!full.isNullOrBlank()) {
                    fullCache[article.link] = full
                    val editorText = toEditorStyle(full)
                    article.copy(
                        fullContent = editorText,
                        summary = editorText.take(280).let { if (editorText.length > 280) "$it…" else it }
                    )
                } else article
            }
        }.awaitAll()
    }

    private fun extractFullArticle(url: String): String? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 14000
            instanceFollowRedirects = true
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36 ForzaX/1.2"
            )
            setRequestProperty("Accept", "text/html,application/xhtml+xml")
            setRequestProperty("Accept-Language", "tr-TR,tr;q=0.9,en;q=0.8")
        }
        return try {
            val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseArticleBody(html)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun parseArticleBody(html: String): String? {
        var clean = html
            .replace(Regex("(?is)<script[^>]*>.*?</script>"), " ")
            .replace(Regex("(?is)<style[^>]*>.*?</style>"), " ")
            .replace(Regex("(?is)<noscript[^>]*>.*?</noscript>"), " ")
            .replace(Regex("(?is)<!--.*?-->"), " ")

        val candidates = listOf(
            Regex("(?is)<article[^>]*>(.*?)</article>"),
            Regex("(?is)<div[^>]*class=[\"'][^\"']*(?:article-body|news-content|haber-metni|detail-content|content-body|story-body|entry-content|post-content|news-detail|haberDetay)[^\"']*[\"'][^>]*>(.*?)</div>"),
            Regex("(?is)<div[^>]*id=[\"'](?:content|article|haber|news|news-body)[\"'][^>]*>(.*?)</div>")
        )

        var bodyHtml: String? = null
        for (rx in candidates) {
            val m = rx.find(clean)
            if (m != null && m.groupValues.last().length > 200) {
                bodyHtml = m.groupValues.last()
                break
            }
        }
        if (bodyHtml == null) bodyHtml = clean

        val paragraphs = Regex("(?is)<p[^>]*>(.*?)</p>")
            .findAll(bodyHtml)
            .map { it.groupValues[1].stripHtml() }
            .filter { it.length > 40 }
            .filterNot { isNoise(it) }
            .toList()

        if (paragraphs.isEmpty()) {
            val plain = bodyHtml.stripHtml()
            return if (plain.length > 180) plain else null
        }
        return paragraphs.joinToString("\n\n")
    }

    private fun isNoise(text: String): Boolean {
        val lower = text.lowercase(Locale("tr"))
        val noise = listOf(
            "cookie", "çerez", "abone ol", "reklam", "advertisement",
            "tıklayın", "detaylı bilgi", "whatsapp", "telegram kanal",
            "uygulamamızı indir", "bizi takip edin", "gizlilik politikası",
            "iletişim formu", "e-bülten", "newsletter"
        )
        return noise.any { lower.contains(it) } || text.length < 35
    }

    private fun toEditorStyle(raw: String): String {
        val cleaned = raw
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()

        return if (cleaned.length > 2200) {
            val cut = cleaned.take(1800)
            val lastDot = cut.lastIndexOf('.')
            if (lastDot > 1200) cut.take(lastDot + 1) else "$cut…"
        } else cleaned
    }

    private fun readFeed(feed: Feed): List<ForzaArticle> {
        val connection = (URL(feed.url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 9000
            readTimeout = 12000
            setRequestProperty("User-Agent", "ForzaX/1.2 (Android news reader)")
            setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml")
        }
        return try {
            connection.inputStream.use { input ->
                val parser = Xml.newPullParser().apply {
                    setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
                    setInput(input, null)
                }
                val result = mutableListOf<ForzaArticle>()
                var event = parser.eventType
                var inItem = false
                var title = ""
                var description = ""
                var link = ""
                var image: String? = null
                var date = ""

                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            val tag = parser.name.lowercase(Locale.ROOT)
                            if (tag == "item" || tag == "entry") {
                                inItem = true
                                title = ""; description = ""; link = ""; image = null; date = ""
                            } else if (inItem) {
                                when (tag) {
                                    "title" -> title = parser.nextText().stripHtml()
                                    "description", "summary", "encoded" -> {
                                        if (description.isBlank()) description = parser.nextText().stripHtml()
                                    }
                                    "link" -> {
                                        val href = parser.getAttributeValue(null, "href")
                                        link = href ?: parser.nextText()
                                    }
                                    "pubdate", "published", "updated" -> date = parser.nextText()
                                    "thumbnail", "content", "enclosure" -> {
                                        val u = parser.getAttributeValue(null, "url")
                                            ?: parser.getAttributeValue(null, "href")
                                        if (!u.isNullOrBlank() &&
                                            (tag != "content" || parser.getAttributeValue(null, "medium") == "image")
                                        ) image = u
                                    }
                                    "image" -> image = parser.nextText().trim()
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name.equals("item", true) || parser.name.equals("entry", true)) {
                                inItem = false
                                if (title.isNotBlank() && link.startsWith("http")) {
                                    val short = description.take(260)
                                    result += ForzaArticle(
                                        id = link,
                                        title = title,
                                        summary = short,
                                        fullContent = short,
                                        link = link,
                                        source = feed.source,
                                        category = feed.category,
                                        imageUrl = image,
                                        publishedAt = date
                                    )
                                }
                            }
                        }
                    }
                    event = parser.next()
                }
                result
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun publishedEpoch(raw: String): Long {
        if (raw.isBlank()) return 0L
        val patterns = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        )
        return patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                java.text.SimpleDateFormat(pattern, Locale.US).parse(raw)?.time
            }.getOrNull()
        } ?: 0L
    }

    private fun String.stripHtml(): String = replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&", "&")
        .replace(""", "\"")
        .replace("&#39;", "'")
        .replace("<", "<")
        .replace(">", ">")
        .replace(Regex("\\s+"), " ")
        .trim()
}
