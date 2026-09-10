package com.randomcity.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.randomcity.app.data.local.entity.CityHistoryEntity

@Dao
interface CityHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CityHistoryEntity)

    @Query(
        "DELETE FROM city_history WHERE cityId NOT IN " +
            "(SELECT cityId FROM city_history ORDER BY viewedAt DESC LIMIT :keep)"
    )
    suspend fun trimTo(keep: Int)

    @Query("SELECT cityId FROM city_history ORDER BY viewedAt DESC LIMIT :limit")
    suspend fun recentIds(limit: Int): List<String>
}
