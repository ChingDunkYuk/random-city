package com.randomcity.app.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.randomcity.app.AppContainer
import com.randomcity.app.navigation.viewModelFactory
import com.randomcity.app.ui.home.HomeScreen
import com.randomcity.app.ui.home.HomeViewModel
import com.randomcity.app.ui.saved.SavedScreen
import com.randomcity.app.ui.saved.SavedViewModel

private const val TAB_DISCOVER = 0
private const val TAB_SAVED = 1

/** 主页面:底部导航 Discover / Saved(计划§20)。 */
@Composable
fun MainScreen(
    container: AppContainer,
    onCityPicked: (String) -> Unit,
    onOpenCity: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_DISCOVER) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == TAB_DISCOVER,
                    onClick = { selectedTab = TAB_DISCOVER },
                    icon = { Icon(Icons.Outlined.Place, contentDescription = "Discover") },
                    label = { Text("Discover") }
                )
                NavigationBarItem(
                    selected = selectedTab == TAB_SAVED,
                    onClick = { selectedTab = TAB_SAVED },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Saved") },
                    label = { Text("Saved") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                TAB_DISCOVER -> {
                    val vm: HomeViewModel = viewModel(
                        factory = viewModelFactory { HomeViewModel(container.cityRepository) }
                    )
                    HomeScreen(
                        viewModel = vm,
                        onCityPicked = onCityPicked
                    )
                }

                TAB_SAVED -> {
                    val vm: SavedViewModel = viewModel(
                        factory = viewModelFactory { SavedViewModel(container.savedRepository) }
                    )
                    SavedScreen(
                        viewModel = vm,
                        onOpenCity = onOpenCity
                    )
                }
            }
        }
    }
}
