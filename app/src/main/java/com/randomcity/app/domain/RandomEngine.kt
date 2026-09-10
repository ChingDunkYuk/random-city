package com.randomcity.app.domain

import kotlin.random.Random

/**
 * 纯函数随机引擎。
 * 从候选城市 id 中等概率抽取,排除 [excludeIds](当前城市 / 最近浏览)。
 * 若排除后候选为空,自动放宽排除条件而不是报错(计划书§71)。
 */
object RandomEngine {

    fun pick(
        cityIds: List<String>,
        excludeIds: Set<String> = emptySet(),
        random: Random = Random
    ): String? {
        if (cityIds.isEmpty()) return null
        val pool = cityIds.filterNot { it in excludeIds }
        val effective = pool.ifEmpty { cityIds }
        return effective.random(random)
    }
}
