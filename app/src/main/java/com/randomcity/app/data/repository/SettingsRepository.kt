package com.randomcity.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
}
