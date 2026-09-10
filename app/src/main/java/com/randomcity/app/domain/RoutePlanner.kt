package com.randomcity.app.domain

import com.randomcity.app.domain.model.City
import com.randomcity.app.domain.model.RouteDay
import kotlin.math.ceil

/**
 * 建议路线生成(计划书§27:根据推荐天数生成简单路线)。
 * 城市带人工编排的 route 数据时直接使用;
 * 否则将 attractions 按推荐天数均分为每日 2-3 站,最多 3 天。
 */
object RoutePlanner {

    private const val MAX_DAYS = 3

    fun suggestRoute(city: City): List<RouteDay> {
        if (city.route.isNotEmpty()) {
            return city.route.sortedBy { it.day }
        }
        val stops = city.attractions
        if (stops.isEmpty()) return emptyList()

        val days = city.recommendedDaysMax.coerceIn(1, MAX_DAYS)
        val perDay = ceil(stops.size / days.toDouble()).toInt().coerceAtLeast(1)
        return stops.chunked(perDay)
            .take(days)
            .mapIndexed { index, chunk -> RouteDay(day = index + 1, stops = chunk) }
    }
}
