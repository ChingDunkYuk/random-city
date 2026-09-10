package com.randomcity.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val localName: String,
    val country: String,
    val countryLocal: String,
    val countryCode: String,
    val continent: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val tagsJson: String,
    val recommendedDaysMin: Int,
    val recommendedDaysMax: Int,
    val bestMonthsJson: String,
    val budgetLevel: Int,
    val transportScore: Int,
    val attractionsJson: String,
    val foodsJson: String,
    val stayAreasJson: String,
    val transportTipsJson: String,
    val travelTipsJson: String,
    val routeJson: String,
    val imageKeywordsJson: String
)
