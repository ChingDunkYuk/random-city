package com.randomcity.app

import com.randomcity.app.domain.RandomEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class RandomEngineTest {

    private val ids = listOf("tokyo", "paris", "seoul", "tbilisi", "bangkok")

    @Test
    fun `pick returns null for empty pool`() {
        assertNull(RandomEngine.pick(emptyList()))
    }

    @Test
    fun `pick excludes given ids`() {
        repeat(50) {
            val picked = RandomEngine.pick(ids, excludeIds = setOf("tokyo"), random = Random(it))
            assertNotNull(picked)
            assertNotEquals("tokyo", picked)
        }
    }

    @Test
    fun `pick falls back to full pool when exclusion empties candidates`() {
        val single = listOf("tokyo")
        val picked = RandomEngine.pick(single, excludeIds = setOf("tokyo"), random = Random(1))
        assertEquals("tokyo", picked)
    }

    @Test
    fun `pick with single candidate returns it`() {
        assertEquals("paris", RandomEngine.pick(listOf("paris"), random = Random(42)))
    }

    @Test
    fun `consecutive picks never repeat current city`() {
        var last: String? = null
        repeat(200) { seed ->
            val picked = RandomEngine.pick(
                ids,
                excludeIds = setOfNotNull(last),
                random = Random(seed)
            )
            assertNotNull(picked)
            assertNotEquals(last, picked)
            last = picked
        }
    }

    @Test
    fun `picked id always comes from candidates`() {
        repeat(100) {
            val picked = RandomEngine.pick(ids, random = Random(it))
            assertTrue(picked in ids)
        }
    }
}
