package com.example.solicitudes.ui.login

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",

    val usuarioError: String? = null,
    val claveError: String? = null
)
