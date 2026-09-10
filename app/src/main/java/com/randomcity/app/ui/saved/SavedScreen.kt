package com.randomcity.app.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomcity.app.data.repository.HistoryItem
import com.randomcity.app.domain.model.City
import com.randomcity.app.util.CityVisuals
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAB_SAVED = 0
private const val TAB_HISTORY = 1

@Composable
fun SavedScreen(
    viewModel: SavedViewModel,
    onOpenCity: (String) -> Unit
) {
    val saved by viewModel.displayedSaved.collectAsState()
    val history by viewModel.history.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val sort by viewModel.sortOrder.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_SAVED) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(56.dp))

        // 标题 + 排序(仅收藏 Tab 显示)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedTab == TAB_SAVED) "收藏" else "历史",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (selectedTab == TAB_SAVED) {
                Box {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Sort,
                            contentDescription = "排序",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuOpen,
                        onDismissRequest = { sortMenuOpen = false }
                    ) {
                        SavedSort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        color = if (option == sort) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                },
                                onClick = {
                                    viewModel.sortOrder.value = option
                                    sortMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 分段切换:收藏 | 历史
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = selectedTab == TAB_SAVED,
                onClick = { selectedTab = TAB_SAVED },
                label = { Text("收藏") }
            )
            FilterChip(
                selected = selectedTab == TAB_HISTORY,
                onClick = { selectedTab = TAB_HISTORY },
                label = { Text("历史") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            TAB_SAVED -> SavedTab(
                saved = saved,
                query = query,
                onQueryChange = { viewModel.searchQuery.value = it },
                onOpenCity = onOpenCity,
                onRemove = viewModel::remove
            )

            TAB_HISTORY -> HistoryTab(
                history = history,
                onOpenCity = onOpenCity
            )
        }
    }
}

@Composable
private fun SavedTab(
    saved: List<City>,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenCity: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            placeholder = { Text("搜索城市或国家") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (saved.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (query.isBlank()) {
                        "还没有收藏城市\n去随机一个吧"
                    } else {
                        "没有匹配的收藏"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(saved, key = { it.id }) { city ->
                    CityRowCard(
                        city = city,
                        trailing = {
                            IconButton(onClick = { onRemove(city.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "删除收藏",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = { onOpenCity(city.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(
    history: List<HistoryItem>,
    onOpenCity: (String) -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "还没有浏览记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val timeFormat = remember { SimpleDateFormat("M月d日 HH:mm", Locale.CHINA) }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history, key = { it.city.id }) { item ->
                CityRowCard(
                    city = item.city,
                    trailing = {
                        Text(
                            text = timeFormat.format(Date(item.viewedAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = { onOpenCity(item.city.id) }
                )
            }
        }
    }
}

/** 收藏/历史通用城市行卡。 */
@Composable
private fun CityRowCard(
    city: City,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (start, end) = CityVisuals.gradientFor(city.id)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(start, end)))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${city.countryLocal.ifBlank { city.country }} · " +
                        city.tags.take(3).joinToString(" · ") { it.label },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            trailing()
        }
    }
}
