package com.randomcity.app.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomcity.app.data.repository.SavedRepository
import com.randomcity.app.domain.model.City
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(private val savedRepository: SavedRepository) : ViewModel() {

    val savedCities: StateFlow<List<City>> = savedRepository.observeSavedCities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun remove(cityId: String) {
        viewModelScope.launch {
            savedRepository.remove(cityId)
        }
    }
}
