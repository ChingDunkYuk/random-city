package com.randomcity.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_history")
data class CityHistoryEntity(
    @PrimaryKey val cityId: String,
    val viewedAt: Long
)
