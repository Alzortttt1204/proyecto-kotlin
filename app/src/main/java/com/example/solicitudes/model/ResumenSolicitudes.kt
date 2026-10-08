package com.example.solicitudes.model

// Resumen general para el panel del administrador (criterios 29 y 30).
// Se calcula a partir de las mismas solicitudes guardadas en Room,
// por eso siempre coincide con los registros.
data class ResumenSolicitudes(
    val total: Int,
    val porEstado: Map<EstadoSolicitud, Int>,
    val porPrioridad: Map<Prioridad?, Int>,      // null = sin prioridad definida
    val porResponsable: Map<Long?, Int>,          // null = sin responsable asignado
    val atrasadas: Int
) {
    companion object {
        fun calcular(solicitudes: List<Solicitud>, ahora: Long): ResumenSolicitudes =
            ResumenSolicitudes(
                total = solicitudes.size,
                // Se incluyen todos los estados aunque tengan 0 solicitudes
                porEstado = EstadoSolicitud.entries.associateWith { estado ->
                    solicitudes.count { it.estado == estado }
                },
                porPrioridad = solicitudes.groupingBy { it.prioridad }.eachCount(),
                // Solo cuenta trabajo vigente (no cerradas ni canceladas) por responsable
                porResponsable = solicitudes
                    .filterNot { it.estado.esFinal }
                    .groupingBy { it.responsableId }
                    .eachCount(),
                atrasadas = solicitudes.count { ReglasSolicitud.estaAtrasada(it, ahora) }
            )
    }
}
