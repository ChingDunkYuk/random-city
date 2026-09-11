package com.randomcity.app.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomcity.app.data.repository.CityRepository
import com.randomcity.app.data.repository.HistoryItem
import com.randomcity.app.data.repository.SavedRepository
import com.randomcity.app.domain.model.City
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 收藏排序方式(展示文案在 UI 层经 stringResource 取)。 */
enum class SavedSort {
    TIME_DESC,
    NAME_ASC
}

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModel(
    private val savedRepository: SavedRepository,
    cityRepository: CityRepository
) : ViewModel() {

    private val savedCities = savedRepository.observeSavedCities()

    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SavedSort.TIME_DESC)

    /** 搜索 + 排序后的收藏列表(v0.8)。 */
    val displayedSaved: StateFlow<List<City>> =
        combine(savedCities, searchQuery, sortOrder) { cities, query, sort ->
            val filtered = if (query.isBlank()) {
                cities
            } else {
                val q = query.trim().lowercase()
                cities.filter {
                    it.localName.contains(q, ignoreCase = true) ||
                        it.name.lowercase().contains(q) ||
                        it.countryLocal.contains(q, ignoreCase = true) ||
                        it.country.lowercase().contains(q)
                }
            }
            when (sort) {
                SavedSort.TIME_DESC -> filtered // DAO 已按 createdAt 倒序
                SavedSort.NAME_ASC -> filtered.sortedBy { it.localName }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 浏览历史(v0.8)。 */
    val history: StateFlow<List<HistoryItem>> = cityRepository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun remove(cityId: String) {
        viewModelScope.launch {
            savedRepository.remove(cityId)
        }
    }
}
