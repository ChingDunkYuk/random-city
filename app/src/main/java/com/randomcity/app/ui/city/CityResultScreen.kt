package com.randomcity.app.ui.city

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomcity.app.domain.model.City
import com.randomcity.app.ui.components.SectionTitle
import com.randomcity.app.ui.components.TagChip
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
            CityHeroHeader(
                city = city,
                isSaved = isSaved,
                onBack = onBack,
                onToggleSave = viewModel::toggleSave
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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

                Spacer(modifier = Modifier.height(14.dp))

                // 必去景点(计划§15):chips 紧凑排列
                if (city.attractions.isNotEmpty()) {
                    SectionTitle("必去景点")
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

                // 当地美食(计划§16)
                if (city.foods.isNotEmpty()) {
                    SectionTitle("当地美食")
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        city.foods.take(5).forEach { food ->
                            TagChip(label = food)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 住在哪里(v0.3,计划§23):单行紧凑文本
                if (city.stayAreas.isNotEmpty()) {
                    SectionTitle("住在哪里")
                    Spacer(modifier = Modifier.height(8.dp))
                    city.stayAreas.forEach { area ->
                        Text(
                            text = if (area.note.isBlank()) {
                                "· ${area.name}"
                            } else {
                                "· ${area.name} — ${area.note}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 当地交通(v0.3,计划§24)
                if (city.transportTips.isNotEmpty()) {
                    SectionTitle("当地交通")
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        city.transportTips.forEach { tip ->
                            TagChip(label = tip)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 旅行贴士(v0.3,计划§25)
                if (city.travelTips.isNotEmpty()) {
                    SectionTitle("旅行贴士")
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
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (rolling) "🎲 Rolling..." else "🎲 Random Again",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Quick Info 单卡四列:天数 / 最佳时间 / 预算 / 交通。 */
@Composable
private fun QuickInfoRow(city: City) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
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
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CityHeroHeader(
    city: City,
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSave: () -> Unit
) {
    val (start, end) = CityVisuals.gradientFor(city.id)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(Brush.linearGradient(listOf(start, end)))
    ) {
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
            IconButton(onClick = onToggleSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isSaved) "取消收藏" else "收藏",
                    tint = Color.White
                )
            }
        }

        // 城市名 + 国家 + 标签(计划§14)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = city.name.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${city.localName} · ${city.countryLocal.ifBlank { city.country }}",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                city.tags.take(4).forEach { tag ->
                    TagChip(label = tag.label, emoji = tag.emoji)
                }
            }
        }
    }
}
