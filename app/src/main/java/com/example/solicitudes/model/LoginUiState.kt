package com.example.solicitudes.model

// Estado de la pantalla de login (mismo patrón que UsuarioUiState de la guía 11)
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val mensajeError: String? = null,      // error general, por ejemplo credenciales inválidas
    val errores: LoginErrores = LoginErrores()
)

// null = campo válido; texto = mensaje de error a mostrar bajo el campo
data class LoginErrores(
    val correo: String? = null,
    val clave: String? = null
)
