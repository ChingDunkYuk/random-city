package com.randomcity.app.data.remote

import android.content.res.AssetManager
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** 当前天气(Open-Meteo);weatherCode 为 WMO 代码,展示文案由 UI 层按 locale 映射。 */
data class CurrentWeather(
    val temperatureC: Int,
    val weatherCode: Int
)

/**
 * 联网数据仓库(v0.9,全部免 API Key):
 * 城市图片 → 内置 assets 优先,远程 Wikipedia/Openverse 兜底;天气 → Open-Meteo。
 * 任何失败返回 null,由 UI 静默回落(计划书§41/§42)。
 */
class CityRemoteRepository(assets: AssetManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /** 内置城市图清单(assets/city_images 下的 webp,离线可用、必铺满)。 */
    private val bundledImages: Set<String> =
        runCatching { assets.list("city_images")?.toSet() ?: emptySet() }
            .getOrDefault(emptySet())

    /** 图片 URL 内存缓存(成功才存,避免失效链接)。 */
    private val imageCache = LruCache<String, String>(50)

    suspend fun fetchCityImageUrl(
        cityId: String,
        englishName: String,
        imageKeywords: List<String> = emptyList()
    ): String? = withContext(Dispatchers.IO) {
        // 内置 asset:离线、零延迟、已精确裁剪 800x450 的 WebP
        if ("$cityId.webp" in bundledImages) {
            return@withContext "file:///android_asset/city_images/$cityId.webp"
        }
        imageCache.get(englishName)?.let { return@withContext it }
        // 远程兜底(未内置城市):Wikipedia → Openverse
        val url = fetchImageFromWikipedia(englishName)
            ?: fetchImageFromOpenverse(englishName)
            ?: imageKeywords.firstOrNull()?.let { fetchImageFromOpenverse(it) }
        url?.also { imageCache.put(englishName, it) }
    }

    /** 主源:Wikipedia PageImages(取前 3 个搜索结果中第一个有代表图的)。 */
    private fun fetchImageFromWikipedia(englishName: String): String? = runCatching {
        val query = URLEncoder.encode(englishName, "UTF-8")
        val url = "https://en.wikipedia.org/w/api.php?action=query" +
            "&generator=search&gsrsearch=$query&gsrlimit=3&gsrnamespace=0" +
            "&prop=pageimages&pithumbsize=800&format=json"
        val body = get(url) ?: return@runCatching null
        val pages = json.parseToJsonElement(body)
            .jsonObject["query"]?.jsonObject?.get("pages")?.jsonObject
            ?: return@runCatching null
        pages.values
            .map { it.jsonObject }
            .sortedBy { it["index"]?.jsonPrimitive?.intOrNull ?: Int.MAX_VALUE }
            .firstNotNullOfOrNull { page ->
                page["thumbnail"]?.jsonObject?.get("source")?.jsonPrimitive?.content
            }
    }.onFailure {
        Log.w(TAG, "wikipedia image failed for $englishName", it)
    }.getOrNull()

    /**
     * 二级源:Openverse 官方 JSON API(免 key,CC 授权图库)。
     * 用 thumbnail 字段(api.openverse.org 代理),不依赖 Flickr 原图直连。
     */
    private fun fetchImageFromOpenverse(englishName: String): String? = runCatching {
        val query = URLEncoder.encode(englishName, "UTF-8")
        val url = "https://api.openverse.org/v1/images/?q=$query" +
            "&aspect_ratio=wide&per_page=5&filter_dead=true"
        val body = get(url) ?: return@runCatching null
        val results = json.parseToJsonElement(body)
            .jsonObject["results"]?.jsonArray ?: return@runCatching null
        results.firstNotNullOfOrNull { item ->
            item.jsonObject["thumbnail"]?.jsonPrimitive?.contentOrNull
        }
    }.onFailure {
        Log.w(TAG, "openverse image failed for $englishName", it)
    }.getOrNull()

    suspend fun fetchCurrentWeather(lat: Double, lng: Double): CurrentWeather? =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$lat&longitude=$lng&current_weather=true"
                val body = get(url) ?: return@runCatching null
                val current = json.parseToJsonElement(body)
                    .jsonObject["current_weather"]?.jsonObject ?: return@runCatching null
                val temp = current["temperature"]?.jsonPrimitive?.doubleOrNull
                    ?: return@runCatching null
                val code = current["weathercode"]?.jsonPrimitive?.intOrNull ?: 0
                CurrentWeather(
                    temperatureC = temp.toInt(),
                    weatherCode = code
                )
            }.getOrNull()
        }

    private fun get(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
        return client.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() else null
        }
    }

    companion object {
        private const val TAG = "CityRemote"
        private const val USER_AGENT = "RandomCityApp/0.9 (https://localhost; random-city)"
    }
}
