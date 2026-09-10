package com.randomcity.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Q 版地球吉祥物(参考用户提供的 kawaii 地球图):
 * 深色粗描边外圈、天蓝海洋 + 深蓝弧底、双层绿大陆、白云、
 * 黑眼单高光、小 u 嘴、大圆珊瑚腮红。
 * 动效:上下浮动(2.2s)+ 周期眨眼(约 3.8s)。
 */
@Composable
fun EarthVisual(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "mascot")

    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    val eyeScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3800
                1f at 0
                1f at 3600
                0.08f at 3700
                1f at 3800
            }
        ),
        label = "blink"
    )

    val outline = Color(0xFF16203A)
    val oceanLight = Color(0xFF5BC5F0)
    val oceanDeep = Color(0xFF3E9BE0)
    val landLight = Color(0xFF7FDBA0)
    val landShadow = Color(0xFF45B87E)
    val blush = Color(0xFFFF8F85)

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val bobOffset = (bob - 0.5f) * size.height * 0.06f
        val r = radius * 0.90f

        // 地面阴影(轻微)
        val shadowScale = 1f - bob * 0.12f
        drawOval(
            color = Color(0xFF2A2E35).copy(alpha = 0.08f - bob * 0.03f),
            topLeft = Offset(center.x - radius * 0.50f * shadowScale, size.height * 0.91f),
            size = Size(radius * 1.0f * shadowScale, size.height * 0.045f)
        )

        withTransform(transformBlock = { translate(left = 0f, top = bobOffset) }) {
            val globePath = Path().apply { addOval(Rect(center = center, radius = r)) }

            clipPath(globePath) {
                // 海洋主体(天蓝)
                drawCircle(color = oceanLight, radius = r, center = center)

                // 深蓝弧底(大圆从下方切入,形成弧形深海带)
                drawCircle(
                    color = oceanDeep,
                    radius = r * 1.05f,
                    center = Offset(center.x + r * 0.25f, center.y + r * 0.85f)
                )

                // 大陆(双层:深绿偏移做底,浅绿在上,形成边缘阴影)
                landBlobs(center, r, landShadow, Offset(r * 0.025f, r * 0.035f))
                landBlobs(center, r, landLight, Offset.Zero)

                // 白云:顶部一朵(两椭圆叠加)
                drawOval(
                    color = Color.White,
                    topLeft = Offset(center.x - r * 0.26f, center.y - r * 0.86f),
                    size = Size(r * 0.52f, r * 0.20f)
                )
                drawOval(
                    color = Color.White,
                    topLeft = Offset(center.x - r * 0.10f, center.y - r * 0.92f),
                    size = Size(r * 0.26f, r * 0.16f)
                )
                // 左上小云
                drawOval(
                    color = Color.White,
                    topLeft = Offset(center.x - r * 0.66f, center.y - r * 0.42f),
                    size = Size(r * 0.22f, r * 0.13f)
                )
                drawOval(
                    color = Color.White,
                    topLeft = Offset(center.x - r * 0.72f, center.y - r * 0.28f),
                    size = Size(r * 0.16f, r * 0.11f)
                )
            }

            // 眼睛:纯黑圆 + 左上白色单高光(眨眼时压扁)
            val eyeY = center.y - r * 0.04f
            val eyeDX = r * 0.30f
            val eyeRadius = r * 0.115f
            val eyeH = eyeRadius * 2f * eyeScale

            listOf(-eyeDX, eyeDX).forEach { dx ->
                drawOval(
                    color = outline,
                    topLeft = Offset(center.x + dx - eyeRadius, eyeY - eyeH / 2),
                    size = Size(eyeRadius * 2f, eyeH)
                )
                if (eyeScale > 0.3f) {
                    drawCircle(
                        color = Color.White,
                        radius = r * 0.042f,
                        center = Offset(center.x + dx - r * 0.035f, eyeY - r * 0.04f)
                    )
                }
            }

            // 小 u 嘴(线描)
            drawArc(
                color = outline,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(center.x - r * 0.10f, center.y + r * 0.06f),
                size = Size(r * 0.20f, r * 0.13f),
                style = Stroke(width = r * 0.035f, cap = StrokeCap.Round)
            )

            // 大圆珊瑚腮红
            listOf(-1f, 1f).forEach { side ->
                drawCircle(
                    color = blush.copy(alpha = 0.85f),
                    radius = r * 0.115f,
                    center = Offset(center.x + side * r * 0.46f, center.y + r * 0.16f)
                )
            }

            // 深色粗描边外圈(参考图最显著特征)
            drawCircle(
                color = outline,
                radius = r,
                center = center,
                style = Stroke(width = r * 0.07f)
            )
        }
    }
}

/** 四片圆润大陆块,每片由 2-3 个椭圆叠出有机形状。 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.landBlobs(
    center: Offset,
    r: Float,
    color: Color,
    offset: Offset
) {
    fun ox(x: Float) = center.x + x + offset.x
    fun oy(y: Float) = center.y + y + offset.y

    // 左上大片
    drawOval(
        color = color,
        topLeft = Offset(ox(-r * 0.72f), oy(-r * 0.55f)),
        size = Size(r * 0.50f, r * 0.34f)
    )
    drawOval(
        color = color,
        topLeft = Offset(ox(-r * 0.80f), oy(-r * 0.42f)),
        size = Size(r * 0.32f, r * 0.24f)
    )
    drawCircle(
        color = color,
        radius = r * 0.08f,
        center = Offset(ox(-r * 0.36f), oy(-r * 0.52f))
    )

    // 右上小片
    drawOval(
        color = color,
        topLeft = Offset(ox(r * 0.30f), oy(-r * 0.62f)),
        size = Size(r * 0.34f, r * 0.24f)
    )
    drawCircle(
        color = color,
        radius = r * 0.07f,
        center = Offset(ox(r * 0.34f), oy(-r * 0.36f))
    )

    // 左下大片
    drawOval(
        color = color,
        topLeft = Offset(ox(-r * 0.68f), oy(r * 0.32f)),
        size = Size(r * 0.48f, r * 0.32f)
    )
    drawOval(
        color = color,
        topLeft = Offset(ox(-r * 0.46f), oy(r * 0.44f)),
        size = Size(r * 0.30f, r * 0.22f)
    )

    // 右侧竖片
    drawOval(
        color = color,
        topLeft = Offset(ox(r * 0.36f), oy(-r * 0.10f)),
        size = Size(r * 0.30f, r * 0.42f)
    )
    drawOval(
        color = color,
        topLeft = Offset(ox(r * 0.28f), oy(r * 0.06f)),
        size = Size(r * 0.24f, r * 0.28f)
    )
}
