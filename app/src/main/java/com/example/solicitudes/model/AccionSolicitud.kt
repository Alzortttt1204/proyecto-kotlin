package com.example.solicitudes.model

// Acciones que quedan registradas en el historial de una solicitud.
// requiereTexto indica que la acción necesita un comentario, motivo o solución.
enum class AccionSolicitud(val etiqueta: String, val requiereTexto: Boolean = false) {
    CREAR("Solicitud creada"),
    REVISAR("Revisión iniciada"),
    CLASIFICAR("Categoría actualizada"),
    CAMBIAR_PRIORIDAD("Prioridad definida"),
    SOLICITAR_INFORMACION("Información solicitada", requiereTexto = true),
    RESPONDER_INFORMACION("Información aportada", requiereTexto = true),
    ASIGNAR("Responsable asignado"),
    REASIGNAR("Responsable reasignado"),
    INICIAR_TRABAJO("Trabajo iniciado"),
    REGISTRAR_AVANCE("Avance registrado", requiereTexto = true),
    COMENTAR("Comentario", requiereTexto = true),
    ADJUNTAR_EVIDENCIA("Evidencia adjuntada"),
    MARCAR_RESUELTA("Solución registrada", requiereTexto = true),
    CONFIRMAR_RESOLUCION("Resolución confirmada por el cliente"),
    CERRAR("Solicitud cerrada"),
    CANCELAR("Solicitud cancelada", requiereTexto = true),
    REABRIR("Reapertura solicitada", requiereTexto = true)
}
