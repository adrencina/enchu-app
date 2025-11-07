package com.adrencina.enchu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.adrencina.enchu.ui.navigation.AppNavigation
import com.adrencina.enchu.ui.theme.EnchuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita el modo edge-to-edge para que la app se dibuje a pantalla completa.
        enableEdgeToEdge()
        setContent {
            EnchuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
