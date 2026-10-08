package com.example.solicitudes.ui.screen.tecnico

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solicitudes.ui.components.BarraFiltros
import com.example.solicitudes.ui.components.BarraSuperior
import com.example.solicitudes.ui.components.Buscador
import com.example.solicitudes.ui.components.ListaSolicitudes
import com.example.solicitudes.viewmodel.AppViewModelProvider
import com.example.solicitudes.viewmodel.ListaSolicitudesViewModel

// Solicitudes asignadas al técnico de la sesión (tarjeta B6)
@Composable
fun InicioTecnicoScreen(
    onAbrirSolicitud: (Long) -> Unit,
    viewModel: ListaSolicitudesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Mis asignaciones",
                subtitulo = estado.usuario?.nombre,
                onCerrarSesion = viewModel::cerrarSesion
            )
        }
    ) { innerPadding ->
        ListaSolicitudes(
            estado = estado,
            onAbrir = onAbrirSolicitud,
            mensajeVacio = "No tienes solicitudes asignadas.",
            modifier = Modifier.padding(innerPadding),
            mostrarCliente = true,
            encabezado = {
                Column {
                    Buscador(estado.filtros.texto, viewModel::onTexto)
                    BarraFiltros(
                        filtros = estado.filtros,
                        onEstado = viewModel::onEstado,
                        onPrioridad = viewModel::onPrioridad,
                        onSoloAtrasadas = viewModel::onSoloAtrasadas,
                        onLimpiar = viewModel::limpiarFiltros
                    )
                }
            }
        )
    }
}
