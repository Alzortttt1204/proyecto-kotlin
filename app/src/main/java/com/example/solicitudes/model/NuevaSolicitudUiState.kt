package com.example.solicitudes.model

// Archivo ya copiado al almacenamiento de la app, listo para adjuntarse al enviar
data class EvidenciaPendiente(
    val nombreArchivo: String,
    val tipoMime: String,
    val tamanoBytes: Long,
    val uri: String
)

// Estado del formulario "Nueva solicitud" (tarjetas A3, A4 y A5)
data class NuevaSolicitudUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: Categoria? = null,
    val servicio: String = "",
    val activo: String = "",                          // sitio web o activo relacionado (opcional)
    val serviciosDelCliente: List<String> = emptyList(),
    val evidencias: List<EvidenciaPendiente> = emptyList(),
    val enviando: Boolean = false,
    val mensaje: String? = null,                      // avisos breves (borrador guardado, errores)
    val creada: Solicitud? = null,                    // solicitud enviada: muestra la confirmación
    val errores: NuevaSolicitudErrores = NuevaSolicitudErrores()
)

data class NuevaSolicitudErrores(
    val titulo: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val servicio: String? = null
)
