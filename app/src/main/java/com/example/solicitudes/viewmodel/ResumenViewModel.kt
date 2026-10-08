package com.example.solicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solicitudes.model.ResumenSolicitudes
import com.example.solicitudes.model.ResumenUiState
import com.example.solicitudes.model.Rol
import com.example.solicitudes.repository.SesionRepository
import com.example.solicitudes.repository.SolicitudesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

// Panel resumen del administrador (tarjeta B5, criterios 29 y 30).
@OptIn(ExperimentalCoroutinesApi::class)
class ResumenViewModel(
    private val repositorio: SolicitudesRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val solicitudes = sesion.usuarioActual.flatMapLatest { usuario ->
        if (usuario?.rol == Rol.ADMIN) repositorio.observarSolicitudesVisibles(usuario) else flowOf(emptyList())
    }

    val estado: StateFlow<ResumenUiState> = combine(
        solicitudes,
        repositorio.observarConteoPorEstado(),
        repositorio.observarUsuarios()
    ) { lista, conteo, usuarios ->
        ResumenUiState(
            resumen = ResumenSolicitudes.calcular(lista, System.currentTimeMillis()),
            conteoBaseDeDatos = conteo.associate { it.estado to it.cantidad },
            nombresUsuarios = usuarios.associate { it.id to it.nombre }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumenUiState())
}
