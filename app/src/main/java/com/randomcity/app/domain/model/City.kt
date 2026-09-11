package com.randomcity.app.domain.model

/** 大洲(展示文案在 ui/Labels.kt,经 stringResource 按 locale 取)。 */
enum class Continent {
    ASIA,
    EUROPE,
    AFRICA,
    NORTH_AMERICA,
    SOUTH_AMERICA,
    OCEANIA
}

/** 旅行风格标签;emoji 为图形符号随枚举走,中文文案在 ui/Labels.kt。 */
enum class TravelTag(val emoji: String) {
    FOOD("🍜"),
    HISTORY("🏛"),
    NATURE("🌲"),
    BEACH("🏖"),
    CITY("🌆"),
    NIGHTLIFE("🌃"),
    PHOTOGRAPHY("📷"),
    ADVENTURE("🧗"),
    BUDGET("🎒")
}

data class StayArea(
    val name: String,
    val note: String
)

data class TravelTip(
    val category: String,
    val text: String
)

data class RouteDay(
    val day: Int,
    val stops: List<String>
)

data class City(
    val id: String,
    val name: String,
    val localName: String,
    val country: String,
    val countryLocal: String,
    val countryCode: String,
    val continent: Continent,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val tags: List<TravelTag>,
    val recommendedDaysMin: Int,
    val recommendedDaysMax: Int,
    val bestMonths: List<Int>,
    /** 1..4 对应 $..$$$$ */
    val budgetLevel: Int,
    /** 1..5 交通便利度 */
    val transportScore: Int,
    val attractions: List<String>,
    val foods: List<String>,
    val stayAreas: List<StayArea> = emptyList(),
    val transportTips: List<String> = emptyList(),
    val travelTips: List<TravelTip> = emptyList(),
    val route: List<RouteDay> = emptyList(),
    val imageKeywords: List<String> = emptyList()
) {
    val displayName: String get() = "$localName $name"

    /** 有效推荐天数(v0.9.1):5+ 景点的城市 2–3 天太赶,上调下限。 */
    val effectiveDaysMin: Int
        get() = if (attractions.size >= 5) maxOf(recommendedDaysMin, 3) else recommendedDaysMin

    val effectiveDaysMax: Int
        get() = if (attractions.size >= 5) maxOf(recommendedDaysMax, 4) else recommendedDaysMax
}
