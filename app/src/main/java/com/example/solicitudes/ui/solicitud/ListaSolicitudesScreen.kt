package com.example.solicitudes.ui.solicitud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.solicitudes.data.local.SolicitudEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaSolicitudesScreen(
    navController: NavController,
    viewModel: SolicitudViewModel
) {
    // La lista viene de Room: al registrar o editar se actualiza sola
    val solicitudes by viewModel.solicitudes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        // Botón: agregar una nueva solicitud
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.nuevaSolicitud()
                    navController.navigate("formulario")
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva solicitud")
            }
        }
    ) { innerPadding ->
        if (solicitudes.isEmpty()) {
            Text(
                "No hay solicitudes registradas. Usa el botón + para agregar una.",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(solicitudes, key = { it.id }) { solicitud ->
                SolicitudItem(
                    solicitud = solicitud,
                    onClick = {
                        viewModel.editarSolicitud(solicitud)
                        navController.navigate("formulario")
                    },
                    // Animación: la tarjeta aparece y se reacomoda suavemente al agregar una solicitud
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

// Tarjeta con los datos principales de una solicitud
@Composable
fun SolicitudItem(
    solicitud: SolicitudEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    solicitud.codigo,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(onClick = onClick, label = { Text(solicitud.estado) })
            }
            Text(solicitud.titulo, style = MaterialTheme.typography.titleMedium)
            Text("Categoría: ${solicitud.categoria}", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    solicitud.fechaHora,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                if (solicitud.evidenciaUri != null) {
                    Icon(Icons.Filled.Image, contentDescription = "Tiene foto de evidencia")
                }
            }
        }
    }
}
