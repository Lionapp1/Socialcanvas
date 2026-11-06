package com.forzagundem.socialcanvas.data

import android.content.Context
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class ForzaArticle(
    val id: String,
    val title: String,
    val summary: String,
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
    private val feeds = listOf(
        Feed("A Spor", "https://www.aspor.com.tr/rss/news.xml", "Gündem"),
        Feed("TRT Spor", "https://www.trthaber.com/spor_articles.rss", "Spor"),
        Feed("Habertürk", "https://www.haberturk.com/rss/spor.xml", "Spor"),
        Feed("Sky Sports", "https://www.skysports.com/rss/11095", "Dünya")
    )
    private data class Feed(val source: String, val url: String, val category: String)

    fun cached(context: Context): List<ForzaArticle> = runCatching {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ITEMS, "[]") ?: "[]"
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            ForzaArticle(o.getString("id"), o.getString("title"), o.optString("summary"), o.getString("link"),
                o.getString("source"), o.getString("category"), o.optString("image").takeIf { it.isNotBlank() },
                o.optString("publishedAt"))
        }
    }.getOrDefault(emptyList())

    fun lastUpdated(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(UPDATED, 0L)

    suspend fun refresh(context: Context): List<ForzaArticle> = withContext(Dispatchers.IO) {
        val collected = mutableListOf<ForzaArticle>()
        feeds.forEach { feed ->
            runCatching { collected += readFeed(feed) }
        }
        val merged = (collected + cached(context)).distinctBy { it.link }
            .sortedByDescending { it.publishedAt }.take(150)
        if (merged.isNotEmpty()) {
            val json = JSONArray()
            merged.forEach { a ->
                json.put(JSONObject().put("id", a.id).put("title", a.title).put("summary", a.summary)
                    .put("link", a.link).put("source", a.source).put("category", a.category)
                    .put("image", a.imageUrl ?: "").put("publishedAt", a.publishedAt))
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(ITEMS, json.toString()).putLong(UPDATED, System.currentTimeMillis()).apply()
        }
        merged
    }

    private fun readFeed(feed: Feed): List<ForzaArticle> {
        val connection = (URL(feed.url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 9000
            readTimeout = 12000
            setRequestProperty("User-Agent", "ForzaX/1.0 (Android news reader)")
            setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml")
        }
        return try {
            connection.inputStream.use { input ->
                val parser = Xml.newPullParser().apply { setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true); setInput(input, null) }
                val result = mutableListOf<ForzaArticle>()
                var event = parser.eventType
                var inItem = false
                var title = ""; var description = ""; var link = ""; var image: String? = null; var date = ""
                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            val tag = parser.name.lowercase(Locale.ROOT)
                            if (tag == "item" || tag == "entry") { inItem = true; title = ""; description = ""; link = ""; image = null; date = "" }
                            else if (inItem) {
                                when (tag) {
                                    "title" -> title = parser.nextText().stripHtml()
                                    "description", "summary", "encoded" -> if (description.isBlank()) description = parser.nextText().stripHtml()
                                    "link" -> {
                                        val href = parser.getAttributeValue(null, "href")
                                        link = href ?: parser.nextText()
                                    }
                                    "pubdate", "published", "updated" -> date = parser.nextText()
                                    "thumbnail", "content", "enclosure" -> {
                                        val url = parser.getAttributeValue(null, "url") ?: parser.getAttributeValue(null, "href")
                                        if (!url.isNullOrBlank() && (tag != "content" || parser.getAttributeValue(null, "medium") == "image")) image = url
                                    }
                                    "image" -> image = parser.nextText().trim()
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> if (parser.name.equals("item", true) || parser.name.equals("entry", true)) {
                            inItem = false
                            if (title.isNotBlank() && link.startsWith("http")) {
                                result += ForzaArticle(link, title, description.take(260), link, feed.source, feed.category, image, date)
                            }
                        }
                    }
                    event = parser.next()
                }
                result
            }
        } finally { connection.disconnect() }
    }

    private fun String.stripHtml(): String = replace(Regex("<[^>]*>"), " ").replace("&nbsp;", " ")
        .replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'")
        .replace(Regex("\\s+"), " ").trim()
}
