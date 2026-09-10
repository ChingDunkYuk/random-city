package com.randomcity.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.randomcity.app.data.repository.CityRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(private val cityRepository: CityRepository) : ViewModel() {

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    init {
        viewModelScope.launch {
            // 并行:城市库导入 + 最短展示时间,目标冷启动 ≤2s
            val importJob = launch { cityRepository.ensureImported() }
            delay(800)
            importJob.join()
            _ready.value = true
        }
    }
}
