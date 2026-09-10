package com.randomcity.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.randomcity.app.navigation.RandomCityNavHost
import com.randomcity.app.ui.theme.RandomCityTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RandomCityApp).container
        setContent {
            RandomCityTheme {
                RandomCityNavHost(container = container)
            }
        }
    }
}
