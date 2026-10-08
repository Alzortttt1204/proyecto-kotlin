package com.example.solicitudes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

/**
 * TopAppBar común de la app.
 * @param onVolver si no es null muestra la flecha para volver atrás.
 * @param onCerrarSesion si no es null muestra el botón de cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    subtitulo: String? = null,
    onVolver: (() -> Unit)? = null,
    onCerrarSesion: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            androidx.compose.foundation.layout.Column {
                Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
                subtitulo?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            onVolver?.let {
                IconButton(onClick = it) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = {
            onCerrarSesion?.let {
                IconButton(onClick = it) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
