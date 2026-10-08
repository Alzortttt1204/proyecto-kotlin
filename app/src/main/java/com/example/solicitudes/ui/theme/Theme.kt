package com.example.solicitudes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Azul40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Azul90,
    onPrimaryContainer = Azul10,
    secondary = Gris40,
    secondaryContainer = Gris90,
    tertiary = Turquesa40
)

private val DarkColorScheme = darkColorScheme(
    primary = Azul80,
    onPrimary = Azul10,
    primaryContainer = Azul30,
    onPrimaryContainer = Azul90,
    secondary = Gris80,
    tertiary = Turquesa80
)

// No se usa color dinámico (Android 12+) para que los colores de la app,
// incluidos los de cada estado de solicitud, sean iguales en todos los dispositivos.
@Composable
fun AppSolicitudesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
