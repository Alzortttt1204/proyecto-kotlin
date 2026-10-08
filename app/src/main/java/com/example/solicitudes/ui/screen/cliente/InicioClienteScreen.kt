package com.example.solicitudes.ui.screen.cliente

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

// "Mis solicitudes": el cliente solo ve las de su empresa (tarjeta A2)
@Composable
fun InicioClienteScreen(
    onNuevaSolicitud: () -> Unit,
    onAbrirSolicitud: (Long) -> Unit,
    viewModel: ListaSolicitudesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Mis solicitudes",
                subtitulo = estado.usuario?.let { u -> estado.clientes[u.clienteId]?.empresa ?: u.nombre },
                onCerrarSesion = viewModel::cerrarSesion
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevaSolicitud,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva solicitud") }
            )
        }
    ) { innerPadding ->
        ListaSolicitudes(
            estado = estado,
            onAbrir = onAbrirSolicitud,
            mensajeVacio = "Aún no tienes solicitudes. Usa \"Nueva solicitud\" para registrar la primera.",
            modifier = Modifier.padding(innerPadding),
            encabezado = {
                Column {
                    Buscador(estado.filtros.texto, viewModel::onTexto)
                    BarraFiltros(
                        filtros = estado.filtros,
                        onEstado = viewModel::onEstado,
                        onLimpiar = viewModel::limpiarFiltros
                    )
                }
            }
        )
    }
}
