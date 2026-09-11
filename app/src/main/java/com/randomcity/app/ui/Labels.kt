package com.randomcity.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.randomcity.app.R
import com.randomcity.app.domain.model.City
import com.randomcity.app.domain.model.Continent
import com.randomcity.app.domain.model.TravelTag
import com.randomcity.app.domain.model.TravelTip

/**
 * 展示文案统一出口(v1.1):domain 层只存枚举/数值,
 * 所有面向用户的字符串经 stringResource 取,加语言只加 values-{locale}。
 */

@Composable
fun Continent.label(): String = stringResource(
    when (this) {
        Continent.ASIA -> R.string.continent_asia
        Continent.EUROPE -> R.string.continent_europe
        Continent.AFRICA -> R.string.continent_africa
        Continent.NORTH_AMERICA -> R.string.continent_north_america
        Continent.SOUTH_AMERICA -> R.string.continent_south_america
        Continent.OCEANIA -> R.string.continent_oceania
    }
)

@Composable
fun TravelTag.label(): String = stringResource(
    when (this) {
        TravelTag.FOOD -> R.string.tag_food
        TravelTag.HISTORY -> R.string.tag_history
        TravelTag.NATURE -> R.string.tag_nature
        TravelTag.BEACH -> R.string.tag_beach
        TravelTag.CITY -> R.string.tag_city
        TravelTag.NIGHTLIFE -> R.string.tag_nightlife
        TravelTag.PHOTOGRAPHY -> R.string.tag_photography
        TravelTag.ADVENTURE -> R.string.tag_adventure
        TravelTag.BUDGET -> R.string.tag_budget
    }
)

@Composable
fun TravelTip.categoryLabel(): String = when (category.uppercase()) {
    "PAYMENT" -> stringResource(R.string.tip_category_payment)
    "LANGUAGE" -> stringResource(R.string.tip_category_language)
    "CURRENCY" -> stringResource(R.string.tip_category_currency)
    "SAFETY" -> stringResource(R.string.tip_category_safety)
    "PLUG" -> stringResource(R.string.tip_category_plug)
    "TRANSPORT" -> stringResource(R.string.tip_category_transport)
    "VISA" -> stringResource(R.string.tip_category_visa)
    else -> category
}

/** 每日预算区间(带"/天"后缀,详情页用)。 */
@Composable
fun City.budgetLabel(): String = stringResource(
    when (budgetLevel.coerceIn(1, 4)) {
        1 -> R.string.budget_per_day_1
        2 -> R.string.budget_per_day_2
        3 -> R.string.budget_per_day_3
        else -> R.string.budget_per_day_4
    }
)

/** 预算档位金额区间(不带"/天"后缀,筛选面板用)。 */
@Composable
fun budgetRangeLabel(level: Int): String = stringResource(
    when (level.coerceIn(1, 4)) {
        1 -> R.string.budget_1
        2 -> R.string.budget_2
        3 -> R.string.budget_3
        else -> R.string.budget_4
    }
)

/** 最佳月份区间合并(3–5月 · 10–11月);文案经资源格式化。 */
@Composable
fun City.bestMonthsLabel(): String {
    if (bestMonths.isEmpty()) return stringResource(R.string.label_all_year)
    val sorted = bestMonths.sorted()
    val ranges = mutableListOf<String>()
    var start = sorted.first()
    var prev = start
    for (m in sorted.drop(1)) {
        if (m == prev + 1) {
            prev = m
        } else {
            ranges += monthRangeLabel(start, prev)
            start = m
            prev = m
        }
    }
    ranges += monthRangeLabel(start, prev)
    return ranges.joinToString(" · ")
}

@Composable
private fun monthRangeLabel(start: Int, end: Int): String =
    if (start == end) {
        stringResource(R.string.month_single, start)
    } else {
        stringResource(R.string.month_range, start, end)
    }

@Composable
fun City.recommendedDaysLabel(): String =
    if (effectiveDaysMin == effectiveDaysMax) {
        stringResource(R.string.days_single, effectiveDaysMin)
    } else {
        stringResource(R.string.days_range, effectiveDaysMin, effectiveDaysMax)
    }

/** WMO weathercode → 展示文案(数据层只传 code,文案在此映射)。 */
@Composable
fun weatherLabel(code: Int): String = stringResource(
    when (code) {
        0 -> R.string.weather_clear
        1, 2 -> R.string.weather_cloudy
        3 -> R.string.weather_overcast
        45, 48 -> R.string.weather_fog
        in 51..57 -> R.string.weather_drizzle
        in 61..67 -> R.string.weather_rain
        in 71..77 -> R.string.weather_snow
        in 80..82 -> R.string.weather_showers
        85, 86 -> R.string.weather_snow_showers
        in 95..99 -> R.string.weather_thunder
        else -> R.string.weather_unknown
    }
)
