package com.randomcity.app.util

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * 城市封面渐变:6 组雾感 pastel 双色渐变(低饱和冷色系),
 * 由城市 id hash 确定性选取。配合 Header 底部黑蒙层,白字恒可读。
 */
object CityVisuals {

    private val palettes: List<Pair<Color, Color>> = listOf(
        Color(0xFF9DB4EA) to Color(0xFF6E8FD6), // 雾蓝
        Color(0xFF93C8D4) to Color(0xFF5E9FC9), // 雾青蓝
        Color(0xFFA3A9E8) to Color(0xFF7079C8), // 雾靛
        Color(0xFF8FBFC9) to Color(0xFF5B93C4), // 雾蓝绿
        Color(0xFFAAB6EE) to Color(0xFF7C88D4), // 柔紫蓝
        Color(0xFF9CC3E8) to Color(0xFF6B96D6)  // 雾天蓝
    )

    fun gradientFor(cityId: String): Pair<Color, Color> {
        val index = abs(cityId.hashCode()) % palettes.size
        return palettes[index]
    }
}
