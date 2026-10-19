package com.example.solicitudes.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.solicitudes.data.local.SolicitudEntity

@Composable
fun ListaSolicitudesScreen(
    navController: NavController,
    viewModel: SolicitudViewModel
) {
    // La lista viene de Room: al registrar o editar se actualiza sola
    val solicitudes by viewModel.solicitudes.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Solicitudes", style = MaterialTheme.typography.headlineSmall)

            if (solicitudes.isEmpty()) {
                Text("No hay solicitudes registradas. Usa el botón + para agregar una.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(solicitudes) { solicitud ->
                    SolicitudItem(
                        solicitud = solicitud,
                        onClick = {
                            viewModel.editarSolicitud(solicitud)
                            navController.navigate("formulario")
                        }
                    )
                }
            }
        }

        // Botón: agregar una nueva solicitud
        FloatingActionButton(
            onClick = {
                viewModel.nuevaSolicitud()
                navController.navigate("formulario")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva solicitud")
        }
    }
}

// Tarjeta con los datos principales de una solicitud
@Composable
fun SolicitudItem(
    solicitud: SolicitudEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(solicitud.codigo, style = MaterialTheme.typography.labelMedium)
            Text(solicitud.titulo, style = MaterialTheme.typography.titleMedium)
            Text("Categoría: ${solicitud.categoria}")
            Text("Estado: ${solicitud.estado}")
            Text(solicitud.fechaHora, style = MaterialTheme.typography.bodySmall)
            if (solicitud.evidenciaUri != null) {
                Text("Con foto de evidencia", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
