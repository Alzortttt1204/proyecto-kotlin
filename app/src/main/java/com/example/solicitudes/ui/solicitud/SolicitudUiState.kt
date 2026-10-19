package com.example.solicitudes.ui.solicitud

import com.example.solicitudes.data.local.SolicitudEntity

data class SolicitudUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",
    val evidenciaUri: String? = null,

    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val servicioError: String? = null,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,

    // Solicitud que se está editando (null = solicitud nueva)
    val solicitudEditando: SolicitudEntity? = null
)
