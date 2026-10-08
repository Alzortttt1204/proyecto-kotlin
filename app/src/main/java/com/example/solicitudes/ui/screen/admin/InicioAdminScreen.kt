package com.example.solicitudes.ui.screen.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.ui.components.BarraFiltros
import com.example.solicitudes.ui.components.BarraSuperior
import com.example.solicitudes.ui.components.Buscador
import com.example.solicitudes.ui.components.ListaSolicitudes
import com.example.solicitudes.viewmodel.AppViewModelProvider
import com.example.solicitudes.viewmodel.ListaSolicitudesViewModel

/**
 * Inicio del administrador con NavigationBar (guía 10):
 *  - Bandeja: todas las solicitudes con búsqueda, filtros y accesos rápidos (tarjetas B1 y B4)
 *  - Resumen: panel con cantidades por estado, prioridad y responsable (tarjeta B5)
 */
@Composable
fun InicioAdminScreen(
    onAbrirSolicitud: (Long) -> Unit,
    viewModel: ListaSolicitudesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = if (pestana == 0) "Bandeja de solicitudes" else "Resumen general",
                subtitulo = estado.usuario?.nombre,
                onCerrarSesion = viewModel::cerrarSesion
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },
                    icon = { Icon(Icons.Outlined.Inbox, contentDescription = null) },
                    label = { Text("Bandeja") }
                )
                NavigationBarItem(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    icon = { Icon(Icons.Outlined.Dashboard, contentDescription = null) },
                    label = { Text("Resumen") }
                )
            }
        }
    ) { innerPadding ->
        if (pestana == 0) {
            ListaSolicitudes(
                estado = estado,
                onAbrir = onAbrirSolicitud,
                mensajeVacio = "No hay solicitudes registradas.",
                modifier = Modifier.padding(innerPadding),
                mostrarCliente = true,
                mostrarResponsable = true,
                encabezado = {
                    Column {
                        Buscador(estado.filtros.texto, viewModel::onTexto)
                        // Accesos rápidos: nuevas, pendientes de información y atrasadas (B4)
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = viewModel::verNuevas,
                                label = { Text("Nuevas") },
                                leadingIcon = { Icon(Icons.Outlined.NewReleases, contentDescription = null) }
                            )
                            AssistChip(
                                onClick = viewModel::verPendientesDeInformacion,
                                label = { Text("Esperan información") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null) }
                            )
                            AssistChip(
                                onClick = viewModel::verAtrasadas,
                                label = { Text("Atrasadas") },
                                leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) }
                            )
                        }
                        BarraFiltros(
                            filtros = estado.filtros,
                            onEstado = viewModel::onEstado,
                            onPrioridad = viewModel::onPrioridad,
                            clientes = estado.clientes,
                            onCliente = viewModel::onCliente,
                            tecnicos = estado.tecnicos,
                            onResponsable = viewModel::onResponsable,
                            onSoloAtrasadas = viewModel::onSoloAtrasadas,
                            onLimpiar = viewModel::limpiarFiltros
                        )
                        Text(
                            "Mostrando ${estado.solicitudes.size} de ${estado.totalVisibles}",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            )
        } else {
            // Al tocar un indicador del resumen se abre la bandeja con ese filtro aplicado
            ResumenPanel(
                modifier = Modifier.padding(innerPadding),
                onVerEstado = { e: EstadoSolicitud -> viewModel.limpiarFiltros(); viewModel.onEstado(e); pestana = 0 },
                onVerAtrasadas = { viewModel.verAtrasadas(); pestana = 0 },
                onVerResponsable = { id -> viewModel.limpiarFiltros(); viewModel.onResponsable(id); pestana = 0 }
            )
        }
    }
}
