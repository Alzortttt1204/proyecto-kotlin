package com.example.solicitudes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.solicitudes.model.ListaSolicitudesUiState
import com.example.solicitudes.ui.utils.LocalAnchoVentana
import com.example.solicitudes.ui.utils.columnasPara

/**
 * Lista de solicitudes adaptable (guía 9): 1, 2 o 3 columnas según el ancho de la pantalla.
 * @param mostrarCliente muestra la empresa solicitante (admin y técnico).
 * @param mostrarResponsable muestra el técnico asignado (admin).
 */
@Composable
fun ListaSolicitudes(
    estado: ListaSolicitudesUiState,
    onAbrir: (Long) -> Unit,
    mensajeVacio: String,
    modifier: Modifier = Modifier,
    mostrarCliente: Boolean = false,
    mostrarResponsable: Boolean = false,
    encabezado: (@Composable () -> Unit)? = null
) {
    val columnas = columnasPara(LocalAnchoVentana.current)

    Column(modifier = modifier.fillMaxSize()) {
        encabezado?.invoke()
        if (estado.solicitudes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (estado.filtros.hayFiltrosActivos) "Ninguna solicitud coincide con los filtros." else mensajeVacio,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnas),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estado.solicitudes, key = { it.id }) { solicitud ->
                    SolicitudCard(
                        solicitud = solicitud,
                        onClick = { onAbrir(solicitud.id) },
                        nombreCliente = if (mostrarCliente) estado.clientes[solicitud.clienteId]?.empresa else null,
                        nombreResponsable = if (mostrarResponsable) {
                            solicitud.responsableId?.let { estado.nombresUsuarios[it] } ?: "Sin asignar"
                        } else null,
                        ahora = estado.ahora
                    )
                }
            }
        }
    }
}
