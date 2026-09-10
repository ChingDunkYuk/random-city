package com.randomcity.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomcity.app.data.repository.CityRepository
import com.randomcity.app.data.repository.SettingsRepository
import com.randomcity.app.domain.model.CityFilter
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

    /** Random:应用当前筛选。 */
    fun onRandom(onPicked: (String) -> Unit) = roll(surprise = false, onPicked)

    /** Surprise Me:忽略筛选,完全随机(计划书§32)。 */
    fun onSurprise(onPicked: (String) -> Unit) = roll(surprise = true, onPicked)

    private fun roll(surprise: Boolean, onPicked: (String) -> Unit) {
        if (_rolling.value) return
        viewModelScope.launch {
            _rolling.value = true
            _emptyResult.value = false
            try {
                val picked = cityRepository.rollRandomCity(filter.value, surprise)
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
