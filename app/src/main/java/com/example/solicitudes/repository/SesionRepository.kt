package com.example.solicitudes.repository

import com.example.solicitudes.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Guarda el usuario que inició sesión mientras la app está abierta.
 * Es única para toda la app (vive en AppSolicitudesApp) y la comparten todos los ViewModels.
 *
 * MainViewModel observa usuarioActual: cuando alguien inicia sesión navega al inicio
 * de su perfil, y cuando la cierra vuelve al login.
 */
class SesionRepository {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun iniciarSesion(usuario: Usuario) {
        require(usuario.activo) { "La cuenta está desactivada." }
        _usuarioActual.value = usuario
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }

    // Para ViewModels de pantallas que solo se abren con sesión iniciada
    fun usuarioRequerido(): Usuario =
        checkNotNull(_usuarioActual.value) { "No hay una sesión iniciada." }
}
