package com.randomcity.app

import com.randomcity.app.data.model.CityDataSet
import com.randomcity.app.domain.model.Continent
import com.randomcity.app.domain.model.TravelTag
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * cities.json 数据完整性校验(计划书§53):
 * 必填字段缺失或 id 重复的城市不允许进入正式城市池。
 */
class CityDataValidationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val dataSet: CityDataSet by lazy {
        val file = File("src/main/assets/cities.json")
        assertTrue("cities.json 不存在: ${file.absolutePath}", file.exists())
        json.decodeFromString(CityDataSet.serializer(), file.readText())
    }

    @Test
    fun `contains at least 100 cities`() {
        assertTrue(
            "城市数量不足 100: ${dataSet.cities.size}",
            dataSet.cities.size >= 100
        )
    }

    @Test
    fun `city ids are unique`() {
        val ids = dataSet.cities.map { it.id }
        val duplicates = ids.groupBy { it }.filterValues { it.size > 1 }.keys
        assertEquals("存在重复 id: $duplicates", emptySet<String>(), duplicates)
    }

    @Test
    fun `required fields are present for every city`() {
        dataSet.cities.forEach { city ->
            fun check(condition: Boolean, field: String) {
                if (!condition) fail("城市 ${city.id} 缺少必填字段: $field")
            }
            check(city.id.isNotBlank(), "id")
            check(city.id.matches(Regex("^[a-z0-9_]+$")), "id 格式(小写字母数字下划线)")
            check(city.name.isNotBlank(), "name")
            check(city.localName.isNotBlank(), "localName")
            check(city.country.isNotBlank(), "country")
            check(city.continent.isNotBlank(), "continent")
            check(city.description.isNotBlank(), "description")
            check(city.tags.isNotEmpty(), "tags")
            check(city.recommendedDaysMin >= 1, "recommendedDaysMin")
            check(city.recommendedDaysMax >= city.recommendedDaysMin, "recommendedDaysMax")
            check(city.bestMonths.isNotEmpty(), "bestMonths")
            check(city.attractions.isNotEmpty(), "attractions")
            check(city.foods.isNotEmpty(), "foods")
        }
    }

    @Test
    fun `continent values are valid`() {
        val valid = Continent.entries.map { it.name }.toSet()
        dataSet.cities.forEach { city ->
            assertTrue(
                "城市 ${city.id} 的 continent 非法: ${city.continent}",
                city.continent in valid
            )
        }
    }

    @Test
    fun `tag values are valid`() {
        val valid = TravelTag.entries.map { it.name }.toSet()
        dataSet.cities.forEach { city ->
            city.tags.forEach { tag ->
                assertTrue("城市 ${city.id} 的 tag 非法: $tag", tag in valid)
            }
        }
    }

    @Test
    fun `bestMonths are within 1 to 12`() {
        dataSet.cities.forEach { city ->
            city.bestMonths.forEach { month ->
                assertTrue("城市 ${city.id} 月份非法: $month", month in 1..12)
            }
        }
    }

    @Test
    fun `budgetLevel and transportScore are in range`() {
        dataSet.cities.forEach { city ->
            assertTrue("城市 ${city.id} budgetLevel 非法: ${city.budgetLevel}", city.budgetLevel in 1..4)
            assertTrue("城市 ${city.id} transportScore 非法: ${city.transportScore}", city.transportScore in 1..5)
        }
    }

    @Test
    fun `covers at least 4 continents`() {
        val continents = dataSet.cities.map { it.continent }.toSet()
        assertTrue("大洲覆盖不足: $continents", continents.size >= 4)
    }
}
