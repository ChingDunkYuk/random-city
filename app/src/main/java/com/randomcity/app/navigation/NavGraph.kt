package com.randomcity.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.randomcity.app.AppContainer
import com.randomcity.app.ui.city.CityResultScreen
import com.randomcity.app.ui.city.CityResultViewModel
import com.randomcity.app.ui.main.MainScreen
import com.randomcity.app.ui.splash.SplashScreen
import com.randomcity.app.ui.splash.SplashViewModel

object Routes {
    const val SPLASH = "splash"
    const val MAIN = "main"
    const val CITY = "city/{cityId}"

    fun city(cityId: String) = "city/$cityId"
}

inline fun <reified VM : ViewModel> viewModelFactory(
    crossinline create: () -> VM
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return create() as T
    }
}

@Composable
fun RandomCityNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            val vm: SplashViewModel = viewModel(
                factory = viewModelFactory { SplashViewModel(container.cityRepository) }
            )
            SplashScreen(
                viewModel = vm,
                onReady = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                container = container,
                onCityPicked = { cityId ->
                    navController.navigate(Routes.city(cityId))
                },
                onOpenCity = { cityId ->
                    navController.navigate(Routes.city(cityId))
                }
            )
        }

        composable(
            route = Routes.CITY,
            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
        ) { entry ->
            val cityId = entry.arguments?.getString("cityId") ?: return@composable
            val vm: CityResultViewModel = viewModel(
                key = "city_$cityId",
                factory = viewModelFactory {
                    CityResultViewModel(cityId, container.cityRepository, container.savedRepository)
                }
            )
            CityResultScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onRandomAgain = { newCityId ->
                    // 替换当前城市页,避免返回栈堆积(计划§5.4)
                    navController.navigate(Routes.city(newCityId)) {
                        popUpTo(Routes.MAIN)
                    }
                }
            )
        }
    }
}
