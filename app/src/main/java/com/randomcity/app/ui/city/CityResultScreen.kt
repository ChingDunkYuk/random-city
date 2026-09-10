package com.randomcity.app.ui.city

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.randomcity.app.ui.components.QuickInfoCard
import com.randomcity.app.ui.components.SectionTitle
import com.randomcity.app.ui.components.TagChip
import com.randomcity.app.util.CityVisuals

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

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(20.dp))

                // 一句话介绍(计划§14.1)
                Text(
                    text = city.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Info 四卡(计划§14.2)
                SectionTitle("Quick Info")
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    QuickInfoCard(
                        title = "Recommended",
                        value = city.recommendedDaysLabel,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    QuickInfoCard(
                        title = "Best Time",
                        value = city.bestMonthsLabel,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    QuickInfoCard(
                        title = "Budget",
                        value = city.budgetLabel,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    QuickInfoCard(
                        title = "Transport",
                        value = "★".repeat(city.transportScore.coerceIn(1, 5)),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 核心景点(计划§15)
                if (city.attractions.isNotEmpty()) {
                    SectionTitle("必去景点")
                    Spacer(modifier = Modifier.height(10.dp))
                    city.attractions.take(5).forEach { place ->
                        Text(
                            text = "· $place",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 当地美食(计划§16)
                if (city.foods.isNotEmpty()) {
                    SectionTitle("当地美食")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        city.foods.take(5).forEach { food ->
                            TagChip(label = food)
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // 底部主操作:Random Again(计划§17)
        Button(
            onClick = { viewModel.onRandomAgain(onRandomAgain) },
            enabled = !rolling,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .height(60.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = if (rolling) "🎲 Rolling..." else "🎲 Random Again",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
            .height(300.dp)
            .background(Brush.linearGradient(listOf(start, end)))
    ) {
        // 顶部操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = city.name.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${city.localName} · ${city.countryLocal.ifBlank { city.country }}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                city.tags.take(4).forEach { tag ->
                    TagChip(label = tag.label, emoji = tag.emoji)
                }
            }
        }
    }
}
