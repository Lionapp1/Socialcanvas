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
    private const val MAX_FULL_FETCH = 55
    private const val MIN_SUMMARY_FOR_SKIP = 280

    private val feeds = listOf(
        Feed("A Spor", "https://www.aspor.com.tr/rss/news.xml", "Gundem"),
        Feed("A Spor Galatasaray", "https://www.aspor.com.tr/rss/galatasaray.xml", "Galatasaray"),
        Feed("A Spor Super Lig", "https://www.aspor.com.tr/rss/spor-toto-super-lig.xml", "Super Lig"),
        Feed("A Spor Milli Takim", "https://www.aspor.com.tr/rss/millitakim.xml", "Milli Takim"),
        Feed("A Spor Sampiyonlar Ligi", "https://www.aspor.com.tr/rss/sampiyonlar-ligi.xml", "Avrupa"),
        Feed("A Spor Avrupa Ligi", "https://www.aspor.com.tr/rss/avrupa-ligi.xml", "Avrupa"),
        Feed("TRT Spor", "https://www.trthaber.com/spor_articles.rss", "Spor"),
        Feed("Haberturk", "https://www.haberturk.com/rss/spor.xml", "Spor"),
        Feed("Sky Sports", "https://www.skysports.com/rss/11095", "Dunya")
    )
    private data class Feed(val source: String, val url: String, val category: String)

    private val fullCache = ConcurrentHashMap<String, String>()

    fun sources(): List<String> = feeds.map { it.source }.distinct()

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

    fun clearCache(context: Context) {
        fullCache.clear()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun save(context: Context, list: List<ForzaArticle>) {
        val json = JSONArray()
        list.forEach { a ->
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

    suspend fun refresh(context: Context): List<ForzaArticle> = withContext(Dispatchers.IO) {
        val collected = mutableListOf<ForzaArticle>()
        feeds.forEach { feed -> runCatching { collected += readFeed(feed) } }

        val previous = cached(context).associateBy { it.link }
        var merged = (collected + previous.values)
            .distinctBy { it.link }
            .sortedByDescending { publishedEpoch(it.publishedAt) }
            .take(280)

        merged = merged.map { item ->
            val old = previous[item.link]
            if (old != null && old.fullContent.length > item.fullContent.length) {
                item.copy(
                    fullContent = old.fullContent,
                    summary = if (item.summary.length < 100) old.summary else item.summary
                )
            } else item
        }

        val needFull = merged.filter { it.fullContent.length < MIN_SUMMARY_FOR_SKIP }.take(MAX_FULL_FETCH)
        if (needFull.isNotEmpty()) {
            val enriched = fetchFullContents(needFull)
            val map = enriched.associateBy { it.link }
            merged = merged.map { map[it.link] ?: it }
        }

        if (merged.isNotEmpty()) save(context, merged)
        merged
    }

    suspend fun ensureFull(context: Context, article: ForzaArticle): ForzaArticle = withContext(Dispatchers.IO) {
        if (article.fullContent.length >= MIN_SUMMARY_FOR_SKIP) return@withContext article
        val cachedText = fullCache[article.link]
        val full = cachedText ?: runCatching { extractFullArticle(article.link) }.getOrNull()?.takeIf { it.length > 100 }
        if (full.isNullOrBlank()) return@withContext article
        fullCache[article.link] = full
        val editorText = toEditorStyle(full)
        val updated = article.copy(
            fullContent = editorText,
            summary = editorText.take(300).let { if (editorText.length > 300) it + "..." else it }
        )
        val all = cached(context).map { if (it.link == updated.link) updated else it }
        if (all.none { it.link == updated.link }) {
            save(context, (listOf(updated) + all).distinctBy { it.link }.take(280))
        } else {
            save(context, all)
        }
        updated
    }

    private suspend fun fetchFullContents(articles: List<ForzaArticle>): List<ForzaArticle> = coroutineScope {
        articles.map { article ->
            async(Dispatchers.IO) {
                val cachedText = fullCache[article.link]
                val full = cachedText ?: runCatching { extractFullArticle(article.link) }.getOrNull()?.takeIf { it.length > 100 }
                if (!full.isNullOrBlank()) {
                    fullCache[article.link] = full
                    val editorText = toEditorStyle(full)
                    article.copy(
                        fullContent = editorText,
                        summary = editorText.take(300).let { if (editorText.length > 300) it + "..." else it }
                    )
                } else article
            }
        }.awaitAll()
    }

    private fun extractFullArticle(url: String): String? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 16000
            instanceFollowRedirects = true
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
            )
            setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            setRequestProperty("Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7")
            setRequestProperty("Cache-Control", "no-cache")
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
            .replace(Regex("(?is)<aside[^>]*>.*?</aside>"), " ")
            .replace(Regex("(?is)<nav[^>]*>.*?</nav>"), " ")
            .replace(Regex("(?is)<footer[^>]*>.*?</footer>"), " ")
            .replace(Regex("(?is)<header[^>]*>.*?</header>"), " ")

        Regex("(?is)\"articleBody\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").find(html)?.let { m ->
            val body = m.groupValues[1]
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\/", "/")
                .stripHtml()
            if (body.length > 200) return toEditorStyle(body)
        }

        val candidates = listOf(
            Regex("(?is)<article[^>]*>(.*?)</article>"),
            Regex("(?is)<div[^>]*class=[\"'][^\"']*(?:article-body|news-content|haber-metni|detail-content|content-body|story-body|entry-content|post-content|news-detail|haberDetay|news-text|article__body|content__body|spot-content)[^\"']*[\"'][^>]*>(.*?)</div>"),
            Regex("(?is)<div[^>]*id=[\"'](?:content|article|haber|news|news-body|article-content|haberMetni)[\"'][^>]*>(.*?)</div>"),
            Regex("(?is)<section[^>]*class=[\"'][^\"']*(?:article|content|haber)[^\"']*[\"'][^>]*>(.*?)</section>")
        )

        var bodyHtml: String? = null
        for (rx in candidates) {
            val m = rx.find(clean)
            if (m != null && m.groupValues.last().length > 180) {
                bodyHtml = m.groupValues.last()
                break
            }
        }

        fun parasFrom(src: String): List<String> = Regex("(?is)<p[^>]*>(.*?)</p>")
            .findAll(src)
            .map { it.groupValues[1].stripHtml() }
            .filter { it.length > 45 }
            .filterNot { isNoise(it) }
            .toList()

        val fromBody = bodyHtml?.let { parasFrom(it) }.orEmpty()
        val fromAll = parasFrom(clean)

        val paragraphs = when {
            fromBody.size >= 2 -> fromBody
            fromAll.size >= 2 -> fromAll
            fromBody.isNotEmpty() -> fromBody
            else -> fromAll
        }

        if (paragraphs.isNotEmpty()) return paragraphs.joinToString("\n\n")

        val brText = (bodyHtml ?: clean)
            .replace(Regex("(?is)<br\\s*/?>"), "\n")
            .stripHtml()
        if (brText.length > 200) return toEditorStyle(brText)

        val meta = Regex("(?is)<meta[^>]+(?:name|property)=[\"'](?:description|og:description)[\"'][^>]+content=[\"']([^\"']+)[\"']")
            .find(html)?.groupValues?.get(1)?.stripHtml()
        return if (!meta.isNullOrBlank() && meta.length > 80) meta else null
    }

    private fun isNoise(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        val noise = listOf(
            "cookie", "cerez", "abone ol", "reklam", "advertisement", "subscribe",
            "tiklayin", "detayli bilgi", "whatsapp", "telegram", "facebook", "twitter",
            "uygulamamizi indir", "bizi takip", "gizlilik", "newsletter", "copyright",
            "ilgili haberler", "benzer haberler", "yorum yap", "paylas", "share this"
        )
        return noise.any { lower.contains(it) } || text.length < 35
    }

    private fun toEditorStyle(raw: String): String {
        val cleaned = raw.replace(Regex("[ \\t]+"), " ").replace(Regex("\\n{3,}"), "\n\n").trim()
        return if (cleaned.length > 8000) {
            val cut = cleaned.take(7500)
            val lastDot = cut.lastIndexOf('.')
            if (lastDot > 5000) cut.take(lastDot + 1) else cut + "..."
        } else cleaned
    }

    private fun readFeed(feed: Feed): List<ForzaArticle> {
        val connection = (URL(feed.url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 14000
            setRequestProperty("User-Agent", "ForzaX/1.6 (Android news reader)")
            setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml, */*")
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
                var encoded = ""
                var link = ""
                var image: String? = null
                var date = ""

                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            val tag = parser.name.lowercase(Locale.ROOT)
                            val local = parser.name.substringAfterLast(':').lowercase(Locale.ROOT)
                            if (tag == "item" || tag == "entry") {
                                inItem = true
                                title = ""; description = ""; encoded = ""; link = ""; image = null; date = ""
                            } else if (inItem) {
                                when {
                                    tag == "title" || local == "title" -> title = parser.nextText().stripHtml()
                                    local == "encoded" || tag.endsWith("encoded") -> encoded = parser.nextText()
                                    tag == "description" || tag == "summary" || local == "description" -> {
                                        val t = parser.nextText()
                                        if (description.isBlank()) description = t
                                    }
                                    tag == "link" || local == "link" -> {
                                        val href = parser.getAttributeValue(null, "href")
                                        link = href ?: parser.nextText()
                                    }
                                    tag == "pubdate" || tag == "published" || tag == "updated" || local == "pubdate" ->
                                        date = parser.nextText()
                                    tag == "thumbnail" || tag == "content" || tag == "enclosure" || local == "thumbnail" -> {
                                        val u = parser.getAttributeValue(null, "url") ?: parser.getAttributeValue(null, "href")
                                        if (!u.isNullOrBlank() && (tag != "content" || parser.getAttributeValue(null, "medium") == "image")) image = u
                                    }
                                    tag == "image" -> image = parser.nextText().trim()
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name.equals("item", true) || parser.name.equals("entry", true)) {
                                inItem = false
                                if (title.isNotBlank() && link.startsWith("http")) {
                                    val rich = if (encoded.length > description.length) encoded else description
                                    val plain = rich.stripHtml()
                                    val short = plain.take(300)
                                    val fullSeed = if (plain.length > 400) toEditorStyle(plain) else short
                                    val img = image ?: Regex("(?is)<img[^>]+src=[\"']([^\"']+)[\"']").find(rich)?.groupValues?.get(1)
                                    result += ForzaArticle(
                                        id = link, title = title, summary = short, fullContent = fullSeed,
                                        link = link, source = feed.source, category = feed.category,
                                        imageUrl = img, publishedAt = date
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
            runCatching { java.text.SimpleDateFormat(pattern, Locale.US).parse(raw)?.time }.getOrNull()
        } ?: 0L
    }

    private fun String.stripHtml(): String {
        val amp = 38.toChar().toString()
        return this
            .replace(Regex("<[^>]*>"), " ")
            .replace(amp + "nbsp;", " ")
            .replace(amp + "amp;", amp)
            .replace(amp + "quot;", "\"")
            .replace(amp + "#39;", "'")
            .replace(amp + "apos;", "'")
            .replace(amp + "lt;", "<")
            .replace(amp + "gt;", ">")
            .replace(Regex(amp + "#(\\d+);")) { m ->
                m.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: " "
            }
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
