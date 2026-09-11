package com.randomcity.app.data.repository

import android.content.Context
import com.randomcity.app.data.local.CityDao
import com.randomcity.app.data.local.CityHistoryDao
import com.randomcity.app.data.local.entity.CityEntity
import com.randomcity.app.data.local.entity.CityHistoryEntity
import com.randomcity.app.data.model.CityDataSet
import com.randomcity.app.data.model.CityDto
import com.randomcity.app.domain.RandomEngine
import com.randomcity.app.domain.model.City
import com.randomcity.app.domain.model.CityFilter
import com.randomcity.app.domain.model.Continent
import com.randomcity.app.domain.model.RouteDay
import com.randomcity.app.domain.model.StayArea
import com.randomcity.app.domain.model.TravelTag
import com.randomcity.app.domain.model.TravelTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** 历史记录条目(城市 + 浏览时间)。 */
data class HistoryItem(
    val city: City,
    val viewedAt: Long
)

class CityRepository(
    private val context: Context,
    private val cityDao: CityDao,
    private val historyDao: CityHistoryDao,
    private val settings: SettingsRepository,
    private val json: Json
) {

    /** 首次启动或数据版本变化时,从 assets/cities.json 导入 Room。 */
    suspend fun ensureImported() = withContext(Dispatchers.IO) {
        val dataSet = parseAssets()
        val imported = settings.citySchemaVersion()
        if (imported != dataSet.schemaVersion || cityDao.count() == 0) {
            val entities = dataSet.cities.map { it.toEntity() }
            cityDao.clearAll()
            cityDao.insertAll(entities)
            settings.setCitySchemaVersion(dataSet.schemaVersion)
        }
    }

    private fun parseAssets(): CityDataSet {
        val text = context.assets.open("cities.json").bufferedReader().use { it.readText() }
        return json.decodeFromString(CityDataSet.serializer(), text)
    }

    suspend fun getAllIds(): List<String> = withContext(Dispatchers.IO) {
        cityDao.getAllIds()
    }

    suspend fun getCity(id: String): City? = withContext(Dispatchers.IO) {
        cityDao.getById(id)?.toDomain()
    }

    /** 批量取城市中文名(v0.9.5 洗牌动画);顺序与入参 ids 一致。 */
    suspend fun getLocalNamesByIds(ids: List<String>): List<String> = withContext(Dispatchers.IO) {
        val byId = cityDao.getIdNames(ids).associate { it.id to it.localName }
        ids.mapNotNull { byId[it] }
    }

    /**
     * 按筛选条件取候选城市 id(103 城内存过滤,成本可忽略)。
     */
    suspend fun getFilteredIds(filter: CityFilter): List<String> = withContext(Dispatchers.IO) {
        if (!filter.isActive) return@withContext cityDao.getAllIds()
        cityDao.getAll()
            .map { it.toDomain() }
            .filter { filter.matches(it) }
            .map { it.id }
    }

    /**
     * 抽一个城市(Random Engine v2,计划书§31/§71):
     * 城市池(筛选 / Surprise 全量)→ 排除最近 10 城 → 随机。
     * pool 为空返回 null,由 UI 提示放宽筛选。
     */
    suspend fun rollRandomCity(filter: CityFilter, surprise: Boolean): String? =
        withContext(Dispatchers.IO) {
            val pool = when {
                surprise -> cityDao.getAllIds()
                else -> getFilteredIds(filter)
            }
            if (pool.isEmpty()) return@withContext null
            val exclude = historyDao.recentIds(RECENT_EXCLUDE).toSet()
            val picked = RandomEngine.pick(pool, excludeIds = exclude) ?: return@withContext null
            settings.setLastCityId(picked)
            historyDao.insert(CityHistoryEntity(cityId = picked, viewedAt = System.currentTimeMillis()))
            historyDao.trimTo(HISTORY_KEEP)
            picked
        }

    /**
     * Featured 精选城市(v0.7,计划书§35):静态运营推荐,非算法。
     */
    suspend fun getFeaturedCities(): List<City> = withContext(Dispatchers.IO) {
        FEATURED_IDS.mapNotNull { cityDao.getById(it)?.toDomain() }
    }

    /** 浏览历史(v0.8,计划书§38):城市 + 浏览时间,倒序。 */
    fun observeHistory(): Flow<List<HistoryItem>> =
        historyDao.observeHistoryWithCity(HISTORY_KEEP).map { list ->
            list.map { HistoryItem(city = it.city.toDomain(), viewedAt = it.viewedAt) }
        }

    fun entityToDomain(entity: CityEntity): City = entity.toDomain()

    private fun CityDto.toEntity() = CityEntity(
        id = id,
        name = name,
        localName = localName,
        country = country,
        countryLocal = countryLocal,
        countryCode = countryCode,
        continent = continent,
        latitude = latitude,
        longitude = longitude,
        description = description,
        tagsJson = json.encodeToString(tags),
        recommendedDaysMin = recommendedDaysMin,
        recommendedDaysMax = recommendedDaysMax,
        bestMonthsJson = json.encodeToString(bestMonths),
        budgetLevel = budgetLevel,
        transportScore = transportScore,
        attractionsJson = json.encodeToString(attractions),
        foodsJson = json.encodeToString(foods),
        stayAreasJson = json.encodeToString(stayAreas),
        transportTipsJson = json.encodeToString(transportTips),
        travelTipsJson = json.encodeToString(travelTips),
        routeJson = json.encodeToString(route),
        imageKeywordsJson = json.encodeToString(imageKeywords)
    )

    private fun CityEntity.toDomain(): City {
        val continent = Continent.entries.firstOrNull { it.name == this@toDomain.continent }
            ?: Continent.ASIA
        val tags = json.decodeFromString<List<String>>(tagsJson)
            .mapNotNull { name -> TravelTag.entries.firstOrNull { it.name == name } }
        return City(
            id = id,
            name = name,
            localName = localName,
            country = country,
            countryLocal = countryLocal,
            countryCode = countryCode,
            continent = continent,
            latitude = latitude,
            longitude = longitude,
            description = description,
            tags = tags,
            recommendedDaysMin = recommendedDaysMin,
            recommendedDaysMax = recommendedDaysMax,
            bestMonths = json.decodeFromString(bestMonthsJson),
            budgetLevel = budgetLevel,
            transportScore = transportScore,
            attractions = json.decodeFromString(attractionsJson),
            foods = json.decodeFromString(foodsJson),
            stayAreas = json.decodeFromString<List<com.randomcity.app.data.model.StayAreaDto>>(stayAreasJson)
                .map { StayArea(it.name, it.note) },
            transportTips = json.decodeFromString(transportTipsJson),
            travelTips = json.decodeFromString<List<com.randomcity.app.data.model.TravelTipDto>>(travelTipsJson)
                .map { TravelTip(it.category, it.text) },
            route = json.decodeFromString<List<com.randomcity.app.data.model.RouteDayDto>>(routeJson)
                .map { RouteDay(it.day, it.stops) },
            imageKeywords = json.decodeFromString(imageKeywordsJson)
        )
    }

    companion object {
        const val HISTORY_KEEP = 100
        const val RECENT_EXCLUDE = 10

        /** 精选城市静态名单(运营推荐,风格各异、视觉感强)。 */
        val FEATURED_IDS = listOf(
            "kyoto",
            "santorini",
            "tbilisi",
            "queenstown",
            "marrakech"
        )
    }
}
