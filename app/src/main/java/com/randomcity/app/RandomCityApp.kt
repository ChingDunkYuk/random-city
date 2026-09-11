package com.randomcity.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.randomcity.app.data.local.CityDatabase
import com.randomcity.app.data.remote.CityRemoteRepository
import com.randomcity.app.data.repository.CityRepository
import com.randomcity.app.data.repository.SavedRepository
import com.randomcity.app.data.repository.SettingsRepository
import kotlinx.serialization.json.Json

/** 手动依赖注入容器(计划:不引入 Hilt)。 */
class AppContainer(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val database: CityDatabase = Room.databaseBuilder(
        context.applicationContext,
        CityDatabase::class.java,
        CityDatabase.NAME
    ).build()

    val settingsRepository = SettingsRepository(context.applicationContext)

    val cityRepository = CityRepository(
        context = context.applicationContext,
        cityDao = database.cityDao(),
        historyDao = database.cityHistoryDao(),
        settings = settingsRepository,
        json = json
    )

    val savedRepository = SavedRepository(
        savedCityDao = database.savedCityDao(),
        cityRepository = cityRepository
    )

    val cityRemoteRepository = CityRemoteRepository(context.applicationContext.assets)
}

class RandomCityApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
