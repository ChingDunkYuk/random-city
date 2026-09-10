package com.randomcity.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.randomcity.app.data.local.entity.CityEntity
import com.randomcity.app.data.local.entity.CityHistoryEntity
import com.randomcity.app.data.local.entity.SavedCityEntity

@Database(
    entities = [CityEntity::class, SavedCityEntity::class, CityHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CityDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
    abstract fun savedCityDao(): SavedCityDao
    abstract fun cityHistoryDao(): CityHistoryDao

    companion object {
        const val NAME = "random_city.db"
    }
}
