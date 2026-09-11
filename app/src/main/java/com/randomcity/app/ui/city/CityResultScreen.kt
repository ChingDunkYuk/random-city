package com.randomcity.app.ui.city

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.randomcity.app.domain.RoutePlanner
import com.randomcity.app.domain.model.City
import com.randomcity.app.ui.components.RouteTimeline
import com.randomcity.app.ui.components.SectionTitle
import com.randomcity.app.ui.components.TagChip
import com.randomcity.app.ui.theme.CityDisplayStyle
import com.randomcity.app.util.CityVisuals

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CityResultScreen(
    viewModel: CityResultViewModel,
    onBack: () -> Unit,
    onRandomAgain: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val rolling by viewModel.rolling.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val city = uiState.city
    if (uiState.loading || city == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Reveal 动画(v0.9.5):单次 800ms,三组错峰淡入上滑
            val reveal = remember { Animatable(0f) }
            LaunchedEffect(city.id) {
                reveal.snapTo(0f)
                reveal.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
            }
            val p = reveal.value

            // 组 1:Hero(0→0.4)
            Box(
                modifier = Modifier.graphicsLayer {
                    val g = revealAt(p, 0f, 0.4f)
                    alpha = g
                    translationY = (1f - g) * 24.dp.toPx()
                }
            ) {
                CityHeroHeader(
                    city = city,
                    isSaved = isSaved,
                    imageUrl = imageUrl,
                    onBack = onBack,
                    onToggleSave = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleSave()
                    },
                    onShare = { shareCity(context, city) }
                )
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // 组 2:简介 + QuickInfo + 天气行(0.25→0.65)
                Column(
                    modifier = Modifier.graphicsLayer {
                        val g = revealAt(p, 0.25f, 0.65f)
                        alpha = g
                        translationY = (1f - g) * 20.dp.toPx()
                    }
                ) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // 一句话介绍(计划§14.1)
                    Text(
                        text = city.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Info:单卡四列一行,避免纵向堆叠(计划§14.2)
                    QuickInfoRow(city)

                    // 当前天气(v0.9,计划§42):Optional,失败整行隐藏
                    weather?.let { w ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Cloud,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "现在 ${w.temperatureC}°C · ${w.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 组 3:其余全部 section(0.45→0.85)
                Column(
                    modifier = Modifier.graphicsLayer {
                        val g = revealAt(p, 0.45f, 0.85f)
                        alpha = g
                        translationY = (1f - g) * 16.dp.toPx()
                    }
                ) {
                Spacer(modifier = Modifier.height(14.dp))

                // 必去景点(计划§15):chips 紧凑排列
                if (city.attractions.isNotEmpty()) {
                    SectionTitle("必去景点", icon = Icons.Outlined.LocationOn)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        city.attractions.take(5).forEach { place ->
                            TagChip(label = place)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 当地美食(计划§16):雾青 chips 与景点区分
                if (city.foods.isNotEmpty()) {
                    SectionTitle("当地美食", icon = Icons.Outlined.Restaurant)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        city.foods.take(5).forEach { food ->
                            TagChip(label = food, secondary = true)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 住在哪里(v0.3,计划§23):呼吸感列表
                if (city.stayAreas.isNotEmpty()) {
                    SectionTitle("住在哪里", icon = Icons.Outlined.Hotel)
                    Spacer(modifier = Modifier.height(10.dp))
                    city.stayAreas.forEach { area ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = area.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (area.note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = area.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 当地交通(v0.3,计划§24)
                if (city.transportTips.isNotEmpty()) {
                    SectionTitle("当地交通", icon = Icons.Outlined.DirectionsBus)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        city.transportTips.forEach { tip ->
                            TagChip(label = tip, secondary = true)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 建议路线(v0.5,计划§27-28)
                val route = remember(city.id) { RoutePlanner.suggestRoute(city) }
                if (route.isNotEmpty()) {
                    SectionTitle("建议路线", icon = Icons.Outlined.Route)
                    Spacer(modifier = Modifier.height(8.dp))
                    RouteTimeline(route = route)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 旅行贴士(v0.3,计划§25)
                if (city.travelTips.isNotEmpty()) {
                    SectionTitle("旅行贴士", icon = Icons.Outlined.Lightbulb)
                    Spacer(modifier = Modifier.height(8.dp))
                    city.travelTips.forEach { tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TagChip(label = tip.categoryLabel)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tip.text,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 出行链接(v0.9,计划§43):地图/机票/酒店/餐饮
                SectionTitle("出行链接", icon = Icons.Outlined.TravelExplore)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExternalLinkButton("地图", Icons.Outlined.Map, Modifier.weight(1f)) {
                        openInMaps(context, city)
                    }
                    ExternalLinkButton("机票", Icons.Outlined.Flight, Modifier.weight(1f)) {
                        openExternal(context, "https://www.google.com/travel/flights?q=" + Uri.encode("Flights to ${city.name}"))
                    }
                    ExternalLinkButton("酒店", Icons.Outlined.Hotel, Modifier.weight(1f)) {
                        openExternal(context, "https://www.booking.com/searchresults.html?ss=" + Uri.encode(city.name))
                    }
                    ExternalLinkButton("餐饮", Icons.Outlined.Restaurant, Modifier.weight(1f)) {
                        openExternal(context, "https://www.google.com/maps/search/restaurants+in+" + Uri.encode(city.name))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // 底部主操作:Random Again(计划§17)
        Button(
            onClick = { viewModel.onRandomAgain(onRandomAgain) },
            enabled = !rolling,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
                .height(56.dp),
            shape = RoundedCornerShape(50)
        ) {
            Icon(
                imageVector = Icons.Outlined.Casino,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (rolling) "抽取中…" else "再抽一个",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Reveal 区段插值:progress 在 [start, end] 内从 0 走到 1。 */
private fun revealAt(progress: Float, start: Float, end: Float): Float =
    ((progress - start) / (end - start)).coerceIn(0f, 1f)

/** Quick Info 单卡四列:天数 / 最佳时间 / 预算 / 交通,大圆角轻投影。 */
@Composable
private fun QuickInfoRow(city: City) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickInfoCell("天数", city.recommendedDaysLabel)
            QuickInfoCell("最佳时间", city.bestMonthsLabel)
            QuickInfoCell("预算", city.budgetLabel)
            QuickInfoCell("交通", "★".repeat(city.transportScore.coerceIn(1, 5)))
        }
    }
}

@Composable
private fun QuickInfoCell(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun CityHeroHeader(
    city: City,
    isSaved: Boolean,
    imageUrl: String?,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit
) {
    val (start, end) = CityVisuals.gradientFor(city.id)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Brush.linearGradient(listOf(start, end)))
    ) {
        // 远程城市图片(v0.9):paint 按 Crop 绘制进布局矩形,确定性铺满;失败回落渐变(§41)
        if (imageUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentScale = ContentScale.Crop
                    )
            )
        }

        // 顶部操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            Row {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "分享",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onToggleSave) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isSaved) "取消收藏" else "收藏",
                        tint = Color.White
                    )
                }
            }
        }

        // 底部蒙层:保证白字在明快渐变上可读
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f))
                    )
                )
        )

        // 城市名 + 国家 + 标签(计划§14)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = city.name.uppercase(),
                style = CityDisplayStyle,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${city.localName} · ${city.countryLocal.ifBlank { city.country }}",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                city.tags.take(4).forEach { tag ->
                    TagChip(label = tag.label, onDark = true)
                }
            }
        }
    }
}

/** 出行链接小按钮。 */
@Composable
private fun ExternalLinkButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** 系统地图打开城市位置,无地图应用时回退浏览器(§43)。 */
private fun openInMaps(context: Context, city: City) {
    val label = Uri.encode(city.name)
    val geoIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:${city.latitude},${city.longitude}?q=${city.latitude},${city.longitude}($label)")
    )
    if (geoIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(geoIntent)
    } else {
        openExternal(
            context,
            "https://www.google.com/maps/search/?api=1&query=${city.latitude},${city.longitude}"
        )
    }
}

private fun openExternal(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

/** 分享城市(§44,中文格式)。 */
private fun shareCity(context: Context, city: City) {
    val text = buildString {
        append("我在 Random City 发现了 ${city.localName} ${city.name} 🌏\n")
        append("${city.countryLocal.ifBlank { city.country }} ${city.country}\n")
        append(city.tags.take(3).joinToString(" · ") { it.label })
        append("\n建议游玩:${city.recommendedDaysLabel}")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching {
        context.startActivity(Intent.createChooser(sendIntent, null))
    }
}
