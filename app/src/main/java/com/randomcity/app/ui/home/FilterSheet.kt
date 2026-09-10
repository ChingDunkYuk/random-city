package com.randomcity.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.randomcity.app.domain.model.CityFilter
import com.randomcity.app.domain.model.Continent
import com.randomcity.app.domain.model.TravelTag

/**
 * Random Filters(计划书§30):大洲 / 旅行风格 / 预算,多选。
 * 选择即时生效并通过 [onFilterChange] 持久化。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    filter: CityFilter,
    onFilterChange: (CityFilter) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "筛选",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            FilterSection(title = "大洲") {
                Continent.entries.forEach { continent ->
                    FilterChip(
                        selected = continent in filter.continents,
                        onClick = {
                            onFilterChange(
                                filter.copy(continents = filter.continents.toggle(continent))
                            )
                        },
                        label = { Text(continent.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilterSection(title = "旅行风格") {
                TravelTag.entries.forEach { tag ->
                    FilterChip(
                        selected = tag in filter.styles,
                        onClick = {
                            onFilterChange(filter.copy(styles = filter.styles.toggle(tag)))
                        },
                        label = { Text(tag.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilterSection(title = "预算") {
                (1..4).forEach { level ->
                    FilterChip(
                        selected = level in filter.budgets,
                        onClick = {
                            onFilterChange(filter.copy(budgets = filter.budgets.toggle(level)))
                        },
                        label = { Text("$".repeat(level)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onFilterChange(CityFilter.EMPTY) }) {
                    Text("重置")
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("完成")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> =
    if (item in this) this - item else this + item
