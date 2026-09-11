package com.randomcity.app

import com.randomcity.app.data.model.CityDataSet
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 内置城市图完整性校验(v1.0):
 * 每个城市必须有 assets/city_images/{id}.jpg,且无游离图片文件。
 */
class CityImagesValidationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val cityIds: List<String> by lazy {
        val file = File("src/main/assets/cities.json")
        json.decodeFromString(CityDataSet.serializer(), file.readText())
            .cities.map { it.id }
    }

    private val imageDir = File("src/main/assets/city_images")

    @Test
    fun `every city has a bundled image`() {
        val missing = cityIds.filter { id ->
            val f = File(imageDir, "$id.jpg")
            !f.exists() || f.length() <= MIN_IMAGE_BYTES
        }
        assertEquals("以下城市缺失内置图或图片异常(≤10KB): $missing", emptyList<String>(), missing)
    }

    @Test
    fun `no orphan image files`() {
        val idSet = cityIds.toSet()
        val orphans = imageDir.listFiles()
            ?.filter { it.isFile && it.extension == "jpg" && it.nameWithoutExtension !in idSet }
            ?.map { it.name }
            ?: emptyList()
        assertEquals("存在无对应城市的游离图片: $orphans", emptyList<String>(), orphans)
    }

    @Test
    fun `image count matches city count`() {
        val imageCount = imageDir.listFiles()?.count { it.isFile && it.extension == "jpg" } ?: 0
        assertEquals(
            "图片数量与城市数量不一致: $imageCount vs ${cityIds.size}",
            cityIds.size,
            imageCount
        )
    }

    companion object {
        /** 小于该体积视为异常图(纯白/占位约 6.4KB,正常照片 ≥30KB)。 */
        private const val MIN_IMAGE_BYTES = 10 * 1024L
    }
}
