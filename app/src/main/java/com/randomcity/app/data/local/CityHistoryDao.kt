package com.randomcity.app.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.randomcity.app.data.local.entity.CityEntity
import com.randomcity.app.data.local.entity.CityHistoryEntity
import kotlinx.coroutines.flow.Flow

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

    @Query(
        "SELECT c.*, h.viewedAt AS viewedAt FROM cities c " +
            "INNER JOIN city_history h ON c.id = h.cityId " +
            "ORDER BY h.viewedAt DESC LIMIT :limit"
    )
    fun observeHistoryWithCity(limit: Int): Flow<List<HistoryWithCity>>
}

/** 历史记录 + 城市信息联合查询结果。 */
data class HistoryWithCity(
    @Embedded val city: CityEntity,
    val viewedAt: Long
)
