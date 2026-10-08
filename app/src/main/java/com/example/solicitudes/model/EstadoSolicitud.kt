package com.example.solicitudes.model

// Estados mínimos que exige el caso (sección 3.2)
enum class EstadoSolicitud(val etiqueta: String) {
    RECIBIDA("Recibida"),
    EN_REVISION("En revisión"),
    PENDIENTE_INFORMACION("Pendiente de información"),
    ASIGNADA("Asignada"),
    EN_PROCESO("En proceso"),
    RESUELTA("Resuelta"),
    CERRADA("Cerrada"),
    CANCELADA("Cancelada");

    // Una solicitud cerrada o cancelada ya no admite cambios (solo reapertura si está cerrada)
    val esFinal: Boolean
        get() = this == CERRADA || this == CANCELADA
}
