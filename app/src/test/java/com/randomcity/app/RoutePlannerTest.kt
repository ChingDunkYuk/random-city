package com.randomcity.app

import com.randomcity.app.domain.RoutePlanner
import com.randomcity.app.domain.model.City
import com.randomcity.app.domain.model.Continent
import com.randomcity.app.domain.model.RouteDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutePlannerTest {

    private fun city(
        attractions: List<String> = listOf("A", "B", "C", "D", "E"),
        daysMax: Int = 3,
        route: List<RouteDay> = emptyList()
    ) = City(
        id = "test",
        name = "Test",
        localName = "测试",
        country = "X",
        countryLocal = "X",
        countryCode = "XX",
        continent = Continent.ASIA,
        latitude = 0.0,
        longitude = 0.0,
        description = "desc",
        tags = emptyList(),
        recommendedDaysMin = 1,
        recommendedDaysMax = daysMax,
        bestMonths = listOf(1),
        budgetLevel = 1,
        transportScore = 1,
        attractions = attractions,
        foods = emptyList(),
        route = route
    )

    @Test
    fun `uses curated route when present`() {
        val curated = listOf(
            RouteDay(day = 2, stops = listOf("X")),
            RouteDay(day = 1, stops = listOf("Y", "Z"))
        )
        val result = RoutePlanner.suggestRoute(city(route = curated))
        assertEquals(listOf(1, 2), result.map { it.day })
        assertEquals(listOf("Y", "Z"), result.first().stops)
    }

    @Test
    fun `generates route from attractions split by days`() {
        val result = RoutePlanner.suggestRoute(city())
        assertEquals(3, result.size)
        assertEquals(listOf("A", "B"), result[0].stops)
        assertEquals(listOf("C", "D"), result[1].stops)
        assertEquals(listOf("E"), result[2].stops)
    }

    @Test
    fun `respects max three days`() {
        val result = RoutePlanner.suggestRoute(city(daysMax = 6))
        assertTrue(result.size <= 3)
    }

    @Test
    fun `few attractions spread one stop per day when days allow`() {
        val result = RoutePlanner.suggestRoute(city(attractions = listOf("A", "B"), daysMax = 3))
        assertEquals(2, result.size)
        assertEquals(listOf("A"), result[0].stops)
        assertEquals(listOf("B"), result[1].stops)
    }

    @Test
    fun `empty attractions produce empty route`() {
        assertTrue(RoutePlanner.suggestRoute(city(attractions = emptyList())).isEmpty())
    }

    @Test
    fun `days are sequential starting from one`() {
        val result = RoutePlanner.suggestRoute(city())
        assertEquals((1..result.size).toList(), result.map { it.day })
    }
}
