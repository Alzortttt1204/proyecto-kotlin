package com.example.solicitudes.model

import java.util.Locale

// Reglas de negocio de las solicitudes que no dependen del flujo de estados
object ReglasSolicitud {

    private const val MILIS_POR_HORA = 3_600_000L

    // Prioridad usada para calcular plazos mientras el administrador no define una
    val prioridadPorDefecto = Prioridad.MEDIA

    // Código único de seguimiento, por ejemplo SOL-2026-0007 (criterio 4 del caso)
    fun generarCodigo(correlativo: Long, anio: Int): String {
        require(correlativo > 0) { "El correlativo debe ser mayor que cero" }
        return String.format(Locale.ROOT, "SOL-%d-%04d", anio, correlativo)
    }

    // Fecha límite de atención según la prioridad de la solicitud
    fun fechaCompromiso(solicitud: Solicitud): Long {
        val prioridad = solicitud.prioridad ?: prioridadPorDefecto
        return solicitud.fechaCreacion + prioridad.plazoHoras * MILIS_POR_HORA
    }

    /**
     * Una solicitud está atrasada si la empresa todavía debe trabajar en ella
     * y ya pasó su fecha compromiso. No cuentan las resueltas, cerradas, canceladas
     * ni las que esperan información del cliente (en ese caso la espera es del cliente).
     */
    fun estaAtrasada(solicitud: Solicitud, ahora: Long): Boolean {
        val enEsperaDeLaEmpresa = solicitud.estado in setOf(
            EstadoSolicitud.RECIBIDA,
            EstadoSolicitud.EN_REVISION,
            EstadoSolicitud.ASIGNADA,
            EstadoSolicitud.EN_PROCESO
        )
        return enEsperaDeLaEmpresa && ahora > fechaCompromiso(solicitud)
    }
}
