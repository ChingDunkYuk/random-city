package com.randomcity.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StayAreaDto(
    val name: String,
    val note: String = ""
)

@Serializable
data class TravelTipDto(
    val category: String,
    val text: String
)

@Serializable
data class RouteDayDto(
    val day: Int,
    val stops: List<String> = emptyList()
)

@Serializable
data class CityDto(
    val id: String,
    val name: String,
    val localName: String,
    val country: String,
    val countryLocal: String = "",
    val countryCode: String = "",
    val continent: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String,
    val tags: List<String> = emptyList(),
    val recommendedDaysMin: Int,
    val recommendedDaysMax: Int,
    val bestMonths: List<Int> = emptyList(),
    val budgetLevel: Int = 2,
    val transportScore: Int = 3,
    val attractions: List<String> = emptyList(),
    val foods: List<String> = emptyList(),
    val stayAreas: List<StayAreaDto> = emptyList(),
    val transportTips: List<String> = emptyList(),
    val travelTips: List<TravelTipDto> = emptyList(),
    val route: List<RouteDayDto> = emptyList(),
    val imageKeywords: List<String> = emptyList()
)

@Serializable
data class CityDataSet(
    val schemaVersion: Int,
    val cities: List<CityDto>
)
