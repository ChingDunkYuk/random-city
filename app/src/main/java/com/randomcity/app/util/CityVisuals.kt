package com.randomcity.app.util

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * 由城市 id 生成确定性的冷色渐变配色,作为 MVP 本地 Cover 视觉。
 * 同一城市每次渲染颜色一致。
 */
object CityVisuals {

    private val palettes: List<Pair<Color, Color>> = listOf(
        Color(0xFF1E3A8A) to Color(0xFF3B82F6), // 深蓝 → 蓝
        Color(0xFF0F766E) to Color(0xFF2DD4BF), // 青 → 浅青
        Color(0xFF312E81) to Color(0xFF818CF8), // 靛 → 浅靛
        Color(0xFF155E75) to Color(0xFF22D3EE), // 深青 → 亮青
        Color(0xFF1E293B) to Color(0xFF64748B), // 岩灰 → 灰蓝
        Color(0xFF134E4A) to Color(0xFF14B8A6), // 墨绿 → 翠绿
        Color(0xFF1D4ED8) to Color(0xFF60A5FA), // 宝蓝 → 天蓝
        Color(0xFF3730A3) to Color(0xFF6366F1), // 深靛 → 靛
        Color(0xFF0C4A6E) to Color(0xFF38BDF8), // 海蓝 → 亮蓝
        Color(0xFF334155) to Color(0xFF94A3B8)  // 板岩 → 浅板岩
    )

    fun gradientFor(cityId: String): Pair<Color, Color> {
        val index = abs(cityId.hashCode()) % palettes.size
        return palettes[index]
    }
}
