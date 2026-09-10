package com.randomcity.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.randomcity.app.data.local.entity.CityEntity
import com.randomcity.app.data.local.entity.SavedCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: SavedCityEntity)

    @Query("DELETE FROM saved_cities WHERE cityId = :cityId")
    suspend fun remove(cityId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_cities WHERE cityId = :cityId)")
    fun observeIsSaved(cityId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_cities WHERE cityId = :cityId)")
    suspend fun isSavedOnce(cityId: String): Boolean

    @Query(
        "SELECT c.* FROM cities c " +
            "INNER JOIN saved_cities s ON c.id = s.cityId " +
            "ORDER BY s.createdAt DESC"
    )
    fun observeSavedCities(): Flow<List<CityEntity>>
}
