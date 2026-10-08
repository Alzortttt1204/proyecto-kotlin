package com.example.solicitudes.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solicitudes.data.DatosSemilla
import com.example.solicitudes.model.LoginErrores
import com.example.solicitudes.model.LoginUiState
import com.example.solicitudes.repository.SesionRepository
import com.example.solicitudes.repository.SolicitudesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Login por perfil (tarjeta A1). Al iniciar sesión, MainViewModel navega solo
// al inicio del perfil correspondiente.
class LoginViewModel(
    private val repositorio: SolicitudesRepository,
    private val sesion: SesionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, mensajeError = null, errores = it.errores.copy(correo = null)) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, mensajeError = null, errores = it.errores.copy(clave = null)) }
    }

    // Rellena el formulario con una cuenta de prueba (botones de acceso rápido)
    fun usarCuentaDemo(correo: String) {
        _estado.update { LoginUiState(correo = correo, clave = DatosSemilla.CLAVE_DEMO) }
    }

    // REGLAS DEL NEGOCIO
    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val errores = LoginErrores(
            correo = when {
                actual.correo.isBlank() -> "Ingresa tu correo"
                !Patterns.EMAIL_ADDRESS.matcher(actual.correo.trim()).matches() -> "Correo no válido"
                else -> null
            },
            clave = if (actual.clave.isBlank()) "Ingresa tu clave" else null
        )
        _estado.update { it.copy(errores = errores) }
        return errores.correo == null && errores.clave == null
    }

    fun iniciarSesion() {
        if (!validarFormulario()) return
        val actual = _estado.value
        _estado.update { it.copy(cargando = true, mensajeError = null) }
        viewModelScope.launch {
            val usuario = repositorio.autenticar(actual.correo, actual.clave)
            if (usuario == null) {
                _estado.update { it.copy(cargando = false, mensajeError = "Correo o clave incorrectos") }
            } else {
                // Se limpia el formulario para que la clave no quede en memoria de la pantalla
                _estado.value = LoginUiState()
                sesion.iniciarSesion(usuario)
            }
        }
    }
}
