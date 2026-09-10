package com.randomcity.app

import com.randomcity.app.domain.model.City
import com.randomcity.app.domain.model.CityFilter
import com.randomcity.app.domain.model.Continent
import com.randomcity.app.domain.model.TravelTag
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CityFilterTest {

    private fun city(
        continent: Continent = Continent.ASIA,
        tags: List<TravelTag> = listOf(TravelTag.FOOD, TravelTag.CITY),
        budget: Int = 2
    ) = City(
        id = "test",
        name = "Test",
        localName = "测试",
        country = "X",
        countryLocal = "X",
        countryCode = "XX",
        continent = continent,
        latitude = 0.0,
        longitude = 0.0,
        description = "desc",
        tags = tags,
        recommendedDaysMin = 1,
        recommendedDaysMax = 3,
        bestMonths = listOf(1),
        budgetLevel = budget,
        transportScore = 3,
        attractions = listOf("A"),
        foods = listOf("F")
    )

    @Test
    fun `empty filter matches everything`() {
        val filter = CityFilter.EMPTY
        assertFalse(filter.isActive)
        assertTrue(filter.matches(city()))
        assertTrue(filter.matches(city(continent = Continent.AFRICA, budget = 4)))
    }

    @Test
    fun `continent filter matches within set`() {
        val filter = CityFilter(continents = setOf(Continent.ASIA, Continent.EUROPE))
        assertTrue(filter.matches(city(continent = Continent.ASIA)))
        assertTrue(filter.matches(city(continent = Continent.EUROPE)))
        assertFalse(filter.matches(city(continent = Continent.AFRICA)))
    }

    @Test
    fun `style filter matches any tag overlap`() {
        val filter = CityFilter(styles = setOf(TravelTag.FOOD))
        assertTrue(filter.matches(city(tags = listOf(TravelTag.FOOD, TravelTag.CITY))))
        assertFalse(filter.matches(city(tags = listOf(TravelTag.BEACH))))
    }

    @Test
    fun `budget filter matches within set`() {
        val filter = CityFilter(budgets = setOf(1, 2))
        assertTrue(filter.matches(city(budget = 1)))
        assertTrue(filter.matches(city(budget = 2)))
        assertFalse(filter.matches(city(budget = 3)))
    }

    @Test
    fun `dimensions combine with AND`() {
        val filter = CityFilter(
            continents = setOf(Continent.ASIA),
            styles = setOf(TravelTag.FOOD),
            budgets = setOf(1, 2)
        )
        assertTrue(filter.matches(city()))
        assertFalse(filter.matches(city(continent = Continent.EUROPE)))
        assertFalse(filter.matches(city(tags = listOf(TravelTag.BEACH))))
        assertFalse(filter.matches(city(budget = 4)))
    }

    @Test
    fun `inactive dimensions do not restrict`() {
        val filter = CityFilter(continents = setOf(Continent.OCEANIA))
        assertTrue(filter.matches(city(continent = Continent.OCEANIA, budget = 4, tags = listOf(TravelTag.BEACH))))
    }
}
