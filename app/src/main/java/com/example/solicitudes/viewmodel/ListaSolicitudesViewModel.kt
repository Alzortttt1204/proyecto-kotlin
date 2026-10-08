package com.example.solicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solicitudes.model.Cliente
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.FiltrosSolicitud
import com.example.solicitudes.model.ListaSolicitudesUiState
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.model.Rol
import com.example.solicitudes.model.Usuario
import com.example.solicitudes.repository.SesionRepository
import com.example.solicitudes.repository.SolicitudesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Lista de solicitudes con búsqueda y filtros. La usan las tres pantallas de inicio:
 *  - Cliente: "Mis solicitudes" (tarjeta A2)
 *  - Administrador: bandeja con filtros, atrasadas y pendientes (tarjetas B1 y B4)
 *  - Técnico: solicitudes asignadas (tarjeta B6)
 * El repositorio ya entrega solo las solicitudes que el perfil puede ver.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ListaSolicitudesViewModel(
    private val repositorio: SolicitudesRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val filtros = MutableStateFlow(FiltrosSolicitud())

    private val visibles = sesion.usuarioActual.flatMapLatest { usuario ->
        usuario?.let { repositorio.observarSolicitudesVisibles(it) } ?: flowOf(emptyList())
    }

    private data class Catalogos(
        val clientes: Map<Long, Cliente>,
        val usuarios: List<Usuario>
    )

    private val catalogos = combine(repositorio.observarClientes(), repositorio.observarUsuarios()) { c, u ->
        Catalogos(c.associateBy { it.id }, u)
    }

    val estado: StateFlow<ListaSolicitudesUiState> =
        combine(sesion.usuarioActual, visibles, filtros, catalogos) { usuario, lista, f, cat ->
            val ahora = System.currentTimeMillis()
            ListaSolicitudesUiState(
                usuario = usuario,
                solicitudes = f.aplicar(lista, ahora),
                totalVisibles = lista.size,
                filtros = f,
                clientes = cat.clientes,
                tecnicos = cat.usuarios.filter { it.rol == Rol.TECNICO },
                nombresUsuarios = cat.usuarios.associate { it.id to it.nombre },
                ahora = ahora
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListaSolicitudesUiState())

    fun onTexto(texto: String) = filtros.update { it.copy(texto = texto) }
    fun onEstado(estado: EstadoSolicitud?) = filtros.update { it.copy(estado = estado) }
    fun onPrioridad(prioridad: Prioridad?) = filtros.update { it.copy(prioridad = prioridad) }
    fun onCliente(clienteId: Long?) = filtros.update { it.copy(clienteId = clienteId) }
    fun onResponsable(responsableId: Long?) = filtros.update { it.copy(responsableId = responsableId) }
    fun onSoloAtrasadas(valor: Boolean) = filtros.update { it.copy(soloAtrasadas = valor) }
    fun limpiarFiltros() = filtros.update { FiltrosSolicitud() }

    // Accesos rápidos de la bandeja del administrador (tarjeta B4)
    fun verNuevas() = filtros.update { FiltrosSolicitud(estado = EstadoSolicitud.RECIBIDA) }
    fun verPendientesDeInformacion() = filtros.update { FiltrosSolicitud(estado = EstadoSolicitud.PENDIENTE_INFORMACION) }
    fun verAtrasadas() = filtros.update { FiltrosSolicitud(soloAtrasadas = true) }

    fun cerrarSesion() = sesion.cerrarSesion()
}
