package com.randomcity.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomcity.app.data.repository.CityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val cityRepository: CityRepository) : ViewModel() {

    private val _rolling = MutableStateFlow(false)
    val rolling: StateFlow<Boolean> = _rolling.asStateFlow()

    /** 点击 Random:抽中后回调 cityId 进行导航。 */
    fun onRandom(onPicked: (String) -> Unit) {
        if (_rolling.value) return
        viewModelScope.launch {
            _rolling.value = true
            try {
                val cityId = cityRepository.rollRandomCity()
                if (cityId != null) onPicked(cityId)
            } finally {
                _rolling.value = false
            }
        }
    }
}
