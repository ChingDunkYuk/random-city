package com.randomcity.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.randomcity.app.navigation.RandomCityNavHost
import com.randomcity.app.ui.theme.RandomCityTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RandomCityApp).container
        setContent {
            RandomCityTheme {
                // 统一主题背景,避免页面透出 window 黑色底色
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RandomCityNavHost(container = container)
                }
            }
        }
    }
}
