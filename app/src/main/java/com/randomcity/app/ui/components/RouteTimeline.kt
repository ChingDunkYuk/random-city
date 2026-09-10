package com.randomcity.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.randomcity.app.domain.model.RouteDay

/**
 * 建议路线 Timeline UI(计划书§28):
 * Day N 标题 + 圆点站点,站点间用竖线连接。
 */
@Composable
fun RouteTimeline(route: List<RouteDay>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        route.forEachIndexed { dayIndex, day ->
            Text(
                text = "Day ${day.day}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = if (dayIndex == 0) 0.dp else 10.dp, bottom = 4.dp)
            )
            day.stops.forEachIndexed { stopIndex, stop ->
                val isLastStop = dayIndex == route.lastIndex && stopIndex == day.stops.lastIndex
                TimelineStopRow(stop = stop, showLine = !isLastStop)
            }
        }
    }
}

@Composable
private fun TimelineStopRow(stop: String, showLine: Boolean) {
    Row {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(14.dp)
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            if (showLine) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stop,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
