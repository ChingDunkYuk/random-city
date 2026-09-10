package com.randomcity.app.ui.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CityResultUiState())
    val uiState: StateFlow<CityResultUiState> = _uiState.asStateFlow()

    val isSaved: StateFlow<Boolean> = savedRepository.observeIsSaved(cityId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _rolling = MutableStateFlow(false)
    val rolling: StateFlow<Boolean> = _rolling.asStateFlow()

    private val filter = settingsRepository.cityFilter
        .stateIn(viewModelScope, SharingStarted.Eagerly, CityFilter.EMPTY)

    init {
        viewModelScope.launch {
            _uiState.value = CityResultUiState(
                loading = false,
                city = cityRepository.getCity(cityId)
            )
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
