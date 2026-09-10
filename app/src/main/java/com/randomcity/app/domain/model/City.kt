package com.randomcity.app.domain.model

enum class Continent(val label: String) {
    ASIA("亚洲"),
    EUROPE("欧洲"),
    AFRICA("非洲"),
    NORTH_AMERICA("北美洲"),
    SOUTH_AMERICA("南美洲"),
    OCEANIA("大洋洲")
}

enum class TravelTag(val label: String, val emoji: String) {
    FOOD("美食", "🍜"),
    HISTORY("历史", "🏛"),
    NATURE("自然", "🌲"),
    BEACH("海岛", "🏖"),
    CITY("都市", "🌆"),
    NIGHTLIFE("夜生活", "🌃"),
    PHOTOGRAPHY("摄影", "📷"),
    ADVENTURE("探险", "🧗"),
    BUDGET("穷游", "🎒")
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

    val bestMonthsLabel: String
        get() {
            if (bestMonths.isEmpty()) return "全年"
            val sorted = bestMonths.sorted()
            val ranges = mutableListOf<String>()
            var start = sorted.first()
            var prev = start
            for (m in sorted.drop(1)) {
                if (m == prev + 1) {
                    prev = m
                } else {
                    ranges += if (start == prev) "${start}月" else "${start}–${prev}月"
                    start = m
                    prev = m
                }
            }
            ranges += if (start == prev) "${start}月" else "${start}–${prev}月"
            return ranges.joinToString(" · ")
        }

    val budgetLabel: String get() = "$".repeat(budgetLevel.coerceIn(1, 4))

    val recommendedDaysLabel: String
        get() = if (recommendedDaysMin == recommendedDaysMax) {
            "$recommendedDaysMin 天"
        } else {
            "$recommendedDaysMin–$recommendedDaysMax 天"
        }
}
