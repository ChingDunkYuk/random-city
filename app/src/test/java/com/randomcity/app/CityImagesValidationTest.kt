package com.randomcity.app

import com.randomcity.app.data.model.CityStructureDataSet
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 内置城市图完整性校验(v1.1,WebP):
 * 每个城市必须有 assets/city_images/{id}.webp,无游离文件,
 * 文件为合法 RIFF/WEBP 且体积在 (10KB, 150KB] 区间。
 * 尺寸(800×450)由 tools/city_images.py validate 把关,Java ImageIO 不解析 WebP。
 */
class CityImagesValidationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val cityIds: List<String> by lazy {
        val file = File("src/main/assets/data/cities.json")
        json.decodeFromString(CityStructureDataSet.serializer(), file.readText())
            .cities.map { it.id }
    }

    private val imageDir = File("src/main/assets/city_images")

    @Test
    fun `every city has a bundled image`() {
        val missing = cityIds.filter { id ->
            val f = File(imageDir, "$id.webp")
            !f.exists() || f.length() <= MIN_IMAGE_BYTES
        }
        assertEquals("以下城市缺失内置图或图片异常(≤10KB): $missing", emptyList<String>(), missing)
    }

    @Test
    fun `no orphan image files`() {
        val idSet = cityIds.toSet()
        val orphans = imageDir.listFiles()
            ?.filter { it.isFile && it.extension == "webp" && it.nameWithoutExtension !in idSet }
            ?.map { it.name }
            ?: emptyList()
        assertEquals("存在无对应城市的游离图片: $orphans", emptyList<String>(), orphans)
    }

    @Test
    fun `image count matches city count`() {
        val imageCount = imageDir.listFiles()?.count { it.isFile && it.extension == "webp" } ?: 0
        assertEquals(
            "图片数量与城市数量不一致: $imageCount vs ${cityIds.size}",
            cityIds.size,
            imageCount
        )
    }

    @Test
    fun `images are valid webp within size limit`() {
        val invalid = cityIds.mapNotNull { id ->
            val f = File(imageDir, "$id.webp")
            if (!f.exists()) return@mapNotNull null
            val header = f.inputStream().use { it.readBytes().take(16).toByteArray() }
            val isRiff = header.size >= 16 &&
                header.copyOfRange(0, 4).contentEquals("RIFF".toByteArray()) &&
                header.copyOfRange(8, 12).contentEquals("WEBP".toByteArray())
            when {
                !isRiff -> "$id.webp 不是合法 RIFF/WEBP 文件"
                f.length() > MAX_IMAGE_BYTES -> "$id.webp 超限(${f.length()}B > ${MAX_IMAGE_BYTES}B)"
                else -> null
            }
        }
        assertEquals("以下图片格式或体积不合规: $invalid", emptyList<String>(), invalid)
    }

    @Test
    fun `no legacy jpg files remain`() {
        val legacy = imageDir.listFiles()
            ?.filter { it.isFile && it.extension.equals("jpg", ignoreCase = true) }
            ?.map { it.name }
            ?: emptyList()
        assertEquals("存在残留旧格式 jpg: $legacy", emptyList<String>(), legacy)
    }

    companion object {
        /** 小于该体积视为异常图(纯白/占位约 6.4KB,正常照片 ≥30KB)。 */
        private const val MIN_IMAGE_BYTES = 10 * 1024L

        /** 管线标准上限 120KB,单测留 30KB 余量防误报;严格值由 tools 校验。 */
        private const val MAX_IMAGE_BYTES = 150 * 1024L
    }
}
