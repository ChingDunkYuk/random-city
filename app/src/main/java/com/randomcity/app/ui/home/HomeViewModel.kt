package com.randomcity.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomcity.app.data.repository.CityRepository
import com.randomcity.app.data.repository.SettingsRepository
import com.randomcity.app.domain.model.City
import com.randomcity.app.domain.model.CityFilter
import com.randomcity.app.domain.model.TravelTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val cityRepository: CityRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _rolling = MutableStateFlow(false)
    val rolling: StateFlow<Boolean> = _rolling.asStateFlow()

    /** 当前筛选条件(v0.6,持久化于 DataStore)。 */
    val filter: StateFlow<CityFilter> = settingsRepository.cityFilter
        .stateIn(viewModelScope, SharingStarted.Eagerly, CityFilter.EMPTY)

    private val _emptyResult = MutableStateFlow(false)
    val emptyResult: StateFlow<Boolean> = _emptyResult.asStateFlow()

    /** Featured 精选城市(v0.7)。 */
    private val _featured = MutableStateFlow<List<City>>(emptyList())
    val featured: StateFlow<List<City>> = _featured.asStateFlow()

    init {
        viewModelScope.launch {
            _featured.value = cityRepository.getFeaturedCities()
        }
    }

    /** Random:应用当前筛选。 */
    fun onRandom(onPicked: (String) -> Unit) = roll(surprise = false, onPicked)

    /** Surprise Me:忽略筛选,完全随机(计划书§32)。 */
    fun onSurprise(onPicked: (String) -> Unit) = roll(surprise = true, onPicked)

    /** 按心情探索(v0.7,计划书§34):覆盖风格维并立即开抽。 */
    fun onMood(tag: TravelTag, onPicked: (String) -> Unit) {
        val newFilter = filter.value.copy(styles = setOf(tag))
        viewModelScope.launch {
            settingsRepository.setCityFilter(newFilter)
        }
        roll(surprise = false, onPicked, overrideFilter = newFilter)
    }

    private fun roll(
        surprise: Boolean,
        onPicked: (String) -> Unit,
        overrideFilter: CityFilter? = null
    ) {
        if (_rolling.value) return
        viewModelScope.launch {
            _rolling.value = true
            _emptyResult.value = false
            try {
                val picked = cityRepository.rollRandomCity(
                    overrideFilter ?: filter.value,
                    surprise
                )
                if (picked != null) {
                    onPicked(picked)
                } else {
                    _emptyResult.value = true
                }
            } finally {
                _rolling.value = false
            }
        }
    }

    fun setFilter(filter: CityFilter) {
        _emptyResult.value = false
        viewModelScope.launch {
            settingsRepository.setCityFilter(filter)
        }
    }
}
