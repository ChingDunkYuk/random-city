package com.randomcity.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StayAreaDto(
    val name: String,
    val note: String = ""
)

@Serializable
data class TravelTipDto(
    val category: String,
    val text: String
)

@Serializable
data class RouteDayDto(
    val day: Int,
    val stops: List<String> = emptyList()
)

/**
 * 结构层 DTO(v1.1):assets/data/cities.json。
 * 只含跨语言机器字段;展示文案一律在 [CityTextDto](按 locale 一个文件)。
 */
@Serializable
data class CityStructureDto(
    val id: String,
    val name: String,
    val country: String,
    val countryCode: String = "",
    val continent: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: List<String> = emptyList(),
    val recommendedDaysMin: Int,
    val recommendedDaysMax: Int,
    val bestMonths: List<Int> = emptyList(),
    val budgetLevel: Int = 2,
    val transportScore: Int = 3,
    val imageKeywords: List<String> = emptyList()
)

@Serializable
data class CityStructureDataSet(
    val schemaVersion: Int,
    val cities: List<CityStructureDto>
)

/**
 * 文案层 DTO(v1.1):assets/data/cities_{locale}.json(当前仅 zh)。
 * 所有面向用户的展示字符串;加语言 = 新增一个同格式文件,不动结构层。
 */
@Serializable
data class CityTextDto(
    val id: String,
    val localName: String,
    val countryLocal: String = "",
    val description: String,
    val attractions: List<String> = emptyList(),
    val foods: List<String> = emptyList(),
    val stayAreas: List<StayAreaDto> = emptyList(),
    val transportTips: List<String> = emptyList(),
    val travelTips: List<TravelTipDto> = emptyList(),
    val route: List<RouteDayDto> = emptyList()
)

@Serializable
data class CityTextDataSet(
    val schemaVersion: Int,
    val cities: List<CityTextDto>
)

/** 合并产物:结构层 + 文案层按 id 合并后的完整城市数据(字段与 v1.0 一致)。 */
@Serializable
data class CityDto(
    val id: String,
    val name: String,
    val localName: String,
    val country: String,
    val countryLocal: String = "",
    val countryCode: String = "",
    val continent: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String,
    val tags: List<String> = emptyList(),
    val recommendedDaysMin: Int,
    val recommendedDaysMax: Int,
    val bestMonths: List<Int> = emptyList(),
    val budgetLevel: Int = 2,
    val transportScore: Int = 3,
    val attractions: List<String> = emptyList(),
    val foods: List<String> = emptyList(),
    val stayAreas: List<StayAreaDto> = emptyList(),
    val transportTips: List<String> = emptyList(),
    val travelTips: List<TravelTipDto> = emptyList(),
    val route: List<RouteDayDto> = emptyList(),
    val imageKeywords: List<String> = emptyList()
)

@Serializable
data class CityDataSet(
    val schemaVersion: Int,
    val cities: List<CityDto>
)

/** 结构 + 文案按 id 合并;文案缺失直接抛异常(由数据校验单测兜底)。 */
fun mergeCities(
    structure: CityStructureDataSet,
    text: CityTextDataSet
): CityDataSet {
    val textById = text.cities.associateBy { it.id }
    val merged = structure.cities.map { s ->
        val t = textById[s.id]
            ?: error("城市 ${s.id} 缺少文案层数据(cities_zh.json)")
        CityDto(
            id = s.id,
            name = s.name,
            localName = t.localName,
            country = s.country,
            countryLocal = t.countryLocal,
            countryCode = s.countryCode,
            continent = s.continent,
            latitude = s.latitude,
            longitude = s.longitude,
            description = t.description,
            tags = s.tags,
            recommendedDaysMin = s.recommendedDaysMin,
            recommendedDaysMax = s.recommendedDaysMax,
            bestMonths = s.bestMonths,
            budgetLevel = s.budgetLevel,
            transportScore = s.transportScore,
            attractions = t.attractions,
            foods = t.foods,
            stayAreas = t.stayAreas,
            transportTips = t.transportTips,
            travelTips = t.travelTips,
            route = t.route,
            imageKeywords = s.imageKeywords
        )
    }
    return CityDataSet(schemaVersion = structure.schemaVersion, cities = merged)
}
