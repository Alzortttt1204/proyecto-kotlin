package com.example.solicitudes.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    // Usuario de prueba (ficticio) para esta primera entrega
    private val usuarioDemo = "demo"
    private val claveDemo = "1234"

    // Estado interno mutable
    private val _estado = MutableStateFlow(LoginUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<LoginUiState> = _estado

    // Actualiza el campo usuario y limpia su error
    fun onUsuarioChange(valor: String) {
        _estado.update { it.copy(usuario = valor, usuarioError = null) }
    }

    // Actualiza el campo clave y limpia su error
    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, claveError = null) }
    }

    // REGLAS DEL NEGOCIO
    fun validarFormulario(): Boolean {
        val estadoActual = _estado.value

        val usuarioError = if (estadoActual.usuario.isBlank()) "Campo obligatorio" else null

        val claveError = when {
            estadoActual.clave.isBlank() -> "Campo obligatorio"
            estadoActual.usuario.trim() != usuarioDemo || estadoActual.clave != claveDemo ->
                "Usuario o clave incorrectos"
            else -> null
        }

        _estado.update { it.copy(usuarioError = usuarioError, claveError = claveError) }

        return usuarioError == null && claveError == null
    }
}
