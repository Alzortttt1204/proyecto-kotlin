package com.example.solicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solicitudes.model.ReglasSolicitud
import com.example.solicitudes.model.Rol
import com.example.solicitudes.model.Usuario
import com.example.solicitudes.navigation.NavigationEvent
import com.example.solicitudes.navigation.Screen
import com.example.solicitudes.notificaciones.Avisos
import com.example.solicitudes.repository.SesionRepository
import com.example.solicitudes.repository.SolicitudesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Emite los eventos de navegación que AppNavigation escucha (guía 10) y
// redirige según la sesión: al iniciar sesión va al inicio del perfil, al cerrarla vuelve al login.
// Se usa viewModelScope en vez de CoroutineScope(Dispatchers.Main) para que las
// corrutinas se cancelen solas cuando el ViewModel se destruye.
class MainViewModel(
    private val repositorio: SolicitudesRepository,
    private val sesion: SesionRepository,
    private val avisos: Avisos
) : ViewModel() {

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    // Se expone como SharedFlow de solo lectura para que la UI no pueda emitir eventos
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    val usuarioActual: StateFlow<Usuario?> = sesion.usuarioActual

    init {
        // drop(1) ignora el valor inicial: esa pantalla ya la decide AppNavigation (startDestination)
        viewModelScope.launch {
            sesion.usuarioActual
                .map { it?.id to it?.rol }
                .distinctUntilChanged()
                .drop(1)
                .collect { (_, rol) ->
                    val destino = rol?.let { Screen.inicioPara(it) } ?: Screen.Login
                    _navigationEvents.emit(
                        NavigationEvent.NavigateTo(destino.route, singleTop = true, limpiarPila = true)
                    )
                    if (rol == Rol.ADMIN) avisarAtrasadas()
                }
        }
    }

    fun navigateTo(screen: Screen) = navigateTo(screen.route)

    fun navigateTo(route: String, popUpToRoute: String? = null, inclusive: Boolean = false) {
        viewModelScope.launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateTo(
                    route = route,
                    popUpToRoute = popUpToRoute,
                    inclusive = inclusive,
                    singleTop = true
                )
            )
        }
    }

    fun navigateBack() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.PopBackStack) }
    }

    fun navigateUp() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.NavigateUp) }
    }

    fun cerrarSesion() = sesion.cerrarSesion()

    // Si se perdió la sesión (por ejemplo, Android cerró la app en segundo plano)
    // y se intenta abrir una pantalla protegida, se vuelve al login.
    fun irAlLogin() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateTo(Screen.Login.route, singleTop = true, limpiarPila = true))
        }
    }

    // Aviso "Solicitud atrasada" (sección 3.2 del caso) al entrar el administrador
    private suspend fun avisarAtrasadas() {
        val admin = sesion.usuarioActual.value ?: return
        val ahora = System.currentTimeMillis()
        val atrasadas = repositorio.observarSolicitudesVisibles(admin).first()
            .count { ReglasSolicitud.estaAtrasada(it, ahora) }
        if (atrasadas > 0) {
            avisos.avisar(
                "Solicitudes atrasadas",
                if (atrasadas == 1) "Hay 1 solicitud atrasada" else "Hay $atrasadas solicitudes atrasadas"
            )
        }
    }
}
