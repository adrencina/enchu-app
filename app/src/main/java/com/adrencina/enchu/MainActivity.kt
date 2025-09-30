package com.adrencina.enchu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.adrencina.enchu.ui.navigation.AppNavigation
import com.adrencina.enchu.ui.theme.EnchuTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.activity.SystemBarStyle
import android.graphics.Color

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            // Se aplica un scrim (capa de color) translúcido a la barra de navegación.
            navigationBarStyle = SystemBarStyle.light(
                Color.parseColor("#33FFFFFF"), Color.parseColor("#33FFFFFF")
            )
        )
        super.onCreate(savedInstanceState)

        setContent {
            EnchuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
