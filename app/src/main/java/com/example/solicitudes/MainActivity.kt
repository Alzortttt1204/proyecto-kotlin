package com.example.solicitudes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.solicitudes.ui.navigation.AppNavigation
import com.example.solicitudes.ui.theme.AppSolicitudesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppSolicitudesTheme {
                AppNavigation()
            }
        }
    }
}
