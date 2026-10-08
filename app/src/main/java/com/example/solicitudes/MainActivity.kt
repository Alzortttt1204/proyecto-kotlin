package com.example.solicitudes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import com.example.solicitudes.navigation.AppNavigation
import com.example.solicitudes.ui.theme.AppSolicitudesTheme
import com.example.solicitudes.ui.utils.LocalAnchoVentana

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Tamaño de la ventana (guía 9): se recalcula al girar el teléfono o cambiar de tamaño
            val tamanoVentana = calculateWindowSizeClass(this)
            AppSolicitudesTheme {
                CompositionLocalProvider(LocalAnchoVentana provides tamanoVentana.widthSizeClass) {
                    AppNavigation()
                }
            }
        }
    }
}
