package com.randomcity.app.domain.model

/**
 * Random 筛选条件(计划书§30):大洲 / 旅行风格 / 预算。
 * 维度内多值 OR,维度间 AND;某维为空 = 该维不限制。
 */
data class CityFilter(
    val continents: Set<Continent> = emptySet(),
    val styles: Set<TravelTag> = emptySet(),
    val budgets: Set<Int> = emptySet()
) {
    val isActive: Boolean
        get() = continents.isNotEmpty() || styles.isNotEmpty() || budgets.isNotEmpty()

    fun matches(city: City): Boolean =
        (continents.isEmpty() || city.continent in continents) &&
            (styles.isEmpty() || city.tags.any { it in styles }) &&
            (budgets.isEmpty() || city.budgetLevel in budgets)

    companion object {
        val EMPTY = CityFilter()
    }
}
