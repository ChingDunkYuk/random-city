package com.randomcity.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.randomcity.app.domain.model.CityFilter
import com.randomcity.app.domain.model.Continent
import com.randomcity.app.domain.model.TravelTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** 用户设置:lastCityId、数据 schemaVersion、主题。 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val LAST_CITY_ID = stringPreferencesKey("last_city_id")
        val CITY_SCHEMA_VERSION = intPreferencesKey("city_schema_version")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FILTER_CONTINENTS = stringPreferencesKey("filter_continents")
        val FILTER_STYLES = stringPreferencesKey("filter_styles")
        val FILTER_BUDGETS = stringPreferencesKey("filter_budgets")
    }

    val lastCityId: Flow<String?> = context.dataStore.data.map { it[Keys.LAST_CITY_ID] }

    suspend fun setLastCityId(cityId: String) {
        context.dataStore.edit { it[Keys.LAST_CITY_ID] = cityId }
    }

    suspend fun citySchemaVersion(): Int =
        context.dataStore.data.map { it[Keys.CITY_SCHEMA_VERSION] ?: 0 }.first()

    suspend fun setCitySchemaVersion(version: Int) {
        context.dataStore.edit { it[Keys.CITY_SCHEMA_VERSION] = version }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: THEME_SYSTEM }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    /** Random 筛选条件(v0.6):枚举名/数字逗号串持久化。 */
    val cityFilter: Flow<CityFilter> = context.dataStore.data.map { prefs ->
        CityFilter(
            continents = (prefs[Keys.FILTER_CONTINENTS] ?: "").toContinentSet(),
            styles = (prefs[Keys.FILTER_STYLES] ?: "").toStyleSet(),
            budgets = (prefs[Keys.FILTER_BUDGETS] ?: "").toBudgetSet()
        )
    }

    suspend fun setCityFilter(filter: CityFilter) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FILTER_CONTINENTS] = filter.continents.joinToString(",") { it.name }
            prefs[Keys.FILTER_STYLES] = filter.styles.joinToString(",") { it.name }
            prefs[Keys.FILTER_BUDGETS] = filter.budgets.joinToString(",") { it.toString() }
        }
    }

    private fun String.toContinentSet(): Set<Continent> =
        split(",").mapNotNull { name -> Continent.entries.firstOrNull { it.name == name } }.toSet()

    private fun String.toStyleSet(): Set<TravelTag> =
        split(",").mapNotNull { name -> TravelTag.entries.firstOrNull { it.name == name } }.toSet()

    private fun String.toBudgetSet(): Set<Int> =
        split(",").mapNotNull { it.toIntOrNull() }.filter { it in 1..4 }.toSet()

    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
}
