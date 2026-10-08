package com.example.solicitudes.ui.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.compositionLocalOf

// Tamaño de pantalla actual (guía 9). MainActivity lo calcula con calculateWindowSizeClass()
// y lo entrega a toda la app con este CompositionLocal.
val LocalAnchoVentana = compositionLocalOf { WindowWidthSizeClass.Compact }

// Columnas de las listas de solicitudes según el ancho:
// teléfono = 1, teléfono horizontal o tablet pequeña = 2, tablet grande = 3
fun columnasPara(ancho: WindowWidthSizeClass): Int = when (ancho) {
    WindowWidthSizeClass.Expanded -> 3
    WindowWidthSizeClass.Medium -> 2
    else -> 1
}

// En pantallas anchas el formulario y el detalle no ocupan todo el ancho, para que sean legibles
fun esPantallaAncha(ancho: WindowWidthSizeClass): Boolean = ancho != WindowWidthSizeClass.Compact
