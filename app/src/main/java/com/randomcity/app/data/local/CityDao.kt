package com.randomcity.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.randomcity.app.data.local.entity.CityEntity

@Dao
interface CityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<CityEntity>)

    @Query("DELETE FROM cities")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun count(): Int

    @Query("SELECT id FROM cities")
    suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM cities")
    suspend fun getAll(): List<CityEntity>

    @Query("SELECT * FROM cities WHERE id = :id")
    suspend fun getById(id: String): CityEntity?
}
