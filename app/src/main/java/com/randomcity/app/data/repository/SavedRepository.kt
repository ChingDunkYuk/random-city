package com.randomcity.app.data.repository

import com.randomcity.app.data.local.SavedCityDao
import com.randomcity.app.data.local.entity.SavedCityEntity
import com.randomcity.app.domain.model.City
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SavedRepository(
    private val savedCityDao: SavedCityDao,
    private val cityRepository: CityRepository
) {

    fun observeSavedCities(): Flow<List<City>> =
        savedCityDao.observeSavedCities().map { list ->
            list.map { cityRepository.entityToDomain(it) }
        }

    fun observeIsSaved(cityId: String): Flow<Boolean> =
        savedCityDao.observeIsSaved(cityId)

    suspend fun toggle(cityId: String) = withContext(Dispatchers.IO) {
        // Room 没有 "toggle",先查再写
        val saved = savedCityDao.isSavedOnce(cityId)
        if (saved) {
            savedCityDao.remove(cityId)
        } else {
            savedCityDao.save(SavedCityEntity(cityId = cityId, createdAt = System.currentTimeMillis()))
        }
    }

    suspend fun remove(cityId: String) = withContext(Dispatchers.IO) {
        savedCityDao.remove(cityId)
    }
}
