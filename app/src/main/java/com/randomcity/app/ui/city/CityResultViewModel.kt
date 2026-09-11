package com.randomcity.app.ui.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomcity.app.data.remote.CityRemoteRepository
import com.randomcity.app.data.remote.CurrentWeather
import com.randomcity.app.data.repository.CityRepository
import com.randomcity.app.data.repository.SavedRepository
import com.randomcity.app.data.repository.SettingsRepository
import com.randomcity.app.domain.model.City
import com.randomcity.app.domain.model.CityFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CityResultUiState(
    val loading: Boolean = true,
    val city: City? = null
)

class CityResultViewModel(
    private val cityId: String,
    private val cityRepository: CityRepository,
    private val savedRepository: SavedRepository,
    settingsRepository: SettingsRepository,
    private val remoteRepository: CityRemoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CityResultUiState())
    val uiState: StateFlow<CityResultUiState> = _uiState.asStateFlow()

    val isSaved: StateFlow<Boolean> = savedRepository.observeIsSaved(cityId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _rolling = MutableStateFlow(false)
    val rolling: StateFlow<Boolean> = _rolling.asStateFlow()

    private val filter = settingsRepository.cityFilter
        .stateIn(viewModelScope, SharingStarted.Eagerly, CityFilter.EMPTY)

    /** 城市图片 URL(v0.9,失败为 null → UI 回落渐变)。 */
    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

    /** 当前天气(v0.9,失败为 null → UI 隐藏该行)。 */
    private val _weather = MutableStateFlow<CurrentWeather?>(null)
    val weather: StateFlow<CurrentWeather?> = _weather.asStateFlow()

    init {
        viewModelScope.launch {
            val city = cityRepository.getCity(cityId)
            _uiState.value = CityResultUiState(loading = false, city = city)
            if (city != null) {
                // 图片与天气并行拉取,各自独立失败(§42 不影响页面)
                launch {
                    _imageUrl.value = remoteRepository.fetchCityImageUrl(
                        city.id,
                        city.name,
                        city.imageKeywords
                    )
                }
                launch {
                    _weather.value = remoteRepository.fetchCurrentWeather(
                        city.latitude,
                        city.longitude
                    )
                }
            }
        }
    }

    fun toggleSave() {
        viewModelScope.launch {
            savedRepository.toggle(cityId)
        }
    }

    /** Random Again:应用当前筛选抽新城市,由导航替换当前页(计划§17)。 */
    fun onRandomAgain(onPicked: (String) -> Unit) {
        if (_rolling.value) return
        viewModelScope.launch {
            _rolling.value = true
            try {
                val picked = cityRepository.rollRandomCity(filter.value, surprise = false)
                if (picked != null) {
                    onPicked(picked)
                }
            } finally {
                _rolling.value = false
            }
        }
    }
}
