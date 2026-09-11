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

    /** 批量取城市中文名(v0.9.5 洗牌动画)。 */
    @Query("SELECT id, localName FROM cities WHERE id IN (:ids)")
    suspend fun getIdNames(ids: List<String>): List<CityIdName>
}

/** id + 中文名投影(洗牌动画批量查询用)。 */
data class CityIdName(
    val id: String,
    val localName: String
)
