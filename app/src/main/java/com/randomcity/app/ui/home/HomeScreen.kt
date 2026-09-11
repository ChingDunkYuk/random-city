package com.randomcity.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomcity.app.domain.model.City
import com.randomcity.app.domain.model.TravelTag
import com.randomcity.app.ui.components.CityImage
import com.randomcity.app.ui.components.EarthVisual
import com.randomcity.app.ui.components.SectionTitle
import com.randomcity.app.util.CityVisuals

/** 心情入口(计划书§34):标签 + 中文名 + 线性图标。 */
private data class MoodItem(
    val tag: TravelTag,
    val label: String,
    val icon: ImageVector
)

private val MOODS = listOf(
    MoodItem(TravelTag.FOOD, "美食之旅", Icons.Outlined.Restaurant),
    MoodItem(TravelTag.BEACH, "海岛", Icons.Outlined.BeachAccess),
    MoodItem(TravelTag.PHOTOGRAPHY, "摄影", Icons.Outlined.PhotoCamera),
    MoodItem(TravelTag.HISTORY, "历史", Icons.Outlined.AccountBalance),
    MoodItem(TravelTag.NATURE, "自然", Icons.Outlined.Park)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCityPicked: (String) -> Unit
) {
    val rolling by viewModel.rolling.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val emptyResult by viewModel.emptyResult.collectAsState()
    val featured by viewModel.featured.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Random City",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "下一站,去哪呢?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Q 版地球吉祥物
        EarthVisual(modifier = Modifier.size(170.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // 核心操作:随机(屏幕中下部,单手可达)
        Button(
            onClick = { viewModel.onRandom(onCityPicked) },
            enabled = !rolling,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Casino,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (rolling) "抽取中…" else "随机",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 次级操作:筛选(带激活圆点)/ 惊喜一下
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { showFilterSheet = true },
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("筛选")
                if (filter.isActive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            OutlinedButton(
                onClick = { viewModel.onSurprise(onCityPicked) },
                enabled = !rolling,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("惊喜一下")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (emptyResult) {
                "没有匹配的城市,试试放宽筛选"
            } else {
                "探索意想不到的地方"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (emptyResult) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 按心情探索(v0.7,计划书§34)
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("按心情探索")
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MOODS.forEach { mood ->
                    MoodCard(
                        mood = mood,
                        enabled = !rolling,
                        onClick = { viewModel.onMood(mood.tag, onCityPicked) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 精选城市(v0.7,计划书§35)
        if (featured.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionTitle("精选城市")
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 24.dp)
                ) {
                    items(featured, key = { it.id }) { city ->
                        FeaturedCityCard(
                            city = city,
                            onClick = { onCityPicked(city.id) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showFilterSheet) {
        FilterSheet(
            filter = filter,
            onFilterChange = viewModel::setFilter,
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
private fun MoodCard(
    mood: MoodItem,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = mood.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = mood.label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun FeaturedCityCard(
    city: City,
    onClick: () -> Unit
) {
    val (start, end) = CityVisuals.gradientFor(city.id)
    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(start, end)))
            .clickable(onClick = onClick)
    ) {
        // 真实城市图片:铺满卡片,未就绪/失败时回落渐变
        CityImage(city = city, modifier = Modifier.fillMaxSize())

        // 底部蒙层:保证白字在图片上可读
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = city.localName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${city.name} · ${city.countryLocal.ifBlank { city.country }}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
        }
    }
}
