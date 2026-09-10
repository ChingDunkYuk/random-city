package com.randomcity.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath

/** 程序化手绘地球:海洋渐变 + 抽象大陆 + 极地冰盖 + 云带 + 大气光晕。 */
@Composable
fun EarthVisual(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // 大气光晕
        drawCircle(
            color = Color(0xFF64B5F6).copy(alpha = 0.18f),
            radius = radius,
            center = center
        )

        val globePath = Path().apply {
            addOval(Rect(center = center, radius = radius * 0.9f))
        }

        clipPath(globePath) {
            // 海洋(径向渐变,光源在左上)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4FC3F7), Color(0xFF1E88E5), Color(0xFF0D47A1)),
                    center = Offset(size.width * 0.38f, size.height * 0.34f),
                    radius = radius * 1.6f
                ),
                radius = radius,
                center = center
            )

            val land = Color(0xFF81C784)
            val landDark = Color(0xFF388E3C)

            // 抽象大陆块
            drawOval(
                color = landDark,
                topLeft = Offset(size.width * 0.14f, size.height * 0.20f),
                size = Size(size.width * 0.40f, size.height * 0.28f)
            )
            drawOval(
                color = land,
                topLeft = Offset(size.width * 0.58f, size.height * 0.30f),
                size = Size(size.width * 0.30f, size.height * 0.22f)
            )
            drawOval(
                color = land,
                topLeft = Offset(size.width * 0.22f, size.height * 0.55f),
                size = Size(size.width * 0.26f, size.height * 0.20f)
            )
            drawOval(
                color = landDark,
                topLeft = Offset(size.width * 0.62f, size.height * 0.62f),
                size = Size(size.width * 0.22f, size.height * 0.16f)
            )

            // 极地冰盖
            drawOval(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(size.width * 0.30f, -size.height * 0.06f),
                size = Size(size.width * 0.40f, size.height * 0.14f)
            )

            // 云带
            drawArc(
                color = Color.White.copy(alpha = 0.35f),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(size.width * 0.05f, size.height * 0.30f),
                size = Size(size.width * 0.90f, size.height * 0.30f),
                style = Stroke(width = size.width * 0.030f)
            )
            drawArc(
                color = Color.White.copy(alpha = 0.25f),
                startAngle = 20f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(size.width * 0.10f, size.height * 0.58f),
                size = Size(size.width * 0.80f, size.height * 0.26f),
                style = Stroke(width = size.width * 0.025f)
            )
        }

        // 向阳面高光
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                center = Offset(size.width * 0.35f, size.height * 0.30f),
                radius = radius * 0.9f
            ),
            radius = radius * 0.9f,
            center = center
        )

        // 球体描边
        drawCircle(
            color = Color(0xFF90CAF9).copy(alpha = 0.6f),
            radius = radius * 0.9f,
            center = center,
            style = Stroke(width = size.width * 0.008f)
        )
    }
}
