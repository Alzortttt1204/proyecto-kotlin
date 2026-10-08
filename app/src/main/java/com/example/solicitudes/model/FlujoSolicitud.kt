package com.example.solicitudes.model

import com.example.solicitudes.model.EstadoSolicitud.ASIGNADA
import com.example.solicitudes.model.EstadoSolicitud.CANCELADA
import com.example.solicitudes.model.EstadoSolicitud.CERRADA
import com.example.solicitudes.model.EstadoSolicitud.EN_PROCESO
import com.example.solicitudes.model.EstadoSolicitud.EN_REVISION
import com.example.solicitudes.model.EstadoSolicitud.PENDIENTE_INFORMACION
import com.example.solicitudes.model.EstadoSolicitud.RECIBIDA
import com.example.solicitudes.model.EstadoSolicitud.RESUELTA

/**
 * Reglas del ciclo de vida de una solicitud: qué perfil puede ejecutar qué acción
 * y en qué estado, y a qué estado pasa la solicitud después.
 *
 * Es lógica pura (sin Android) para poder probarla con pruebas unitarias.
 */
object FlujoSolicitud {

    // Toda solicitud nueva comienza como "Recibida" (criterio 6 del caso)
    val estadoInicial = RECIBIDA

    // Una transición: el rol puede ejecutar la acción si la solicitud está en algún estado de "desde".
    // "hacia" = null significa que la acción no cambia el estado (por ejemplo, un comentario).
    private data class Transicion(
        val accion: AccionSolicitud,
        val rol: Rol,
        val desde: Set<EstadoSolicitud>,
        val hacia: EstadoSolicitud?
    )

    private val noFinales = EstadoSolicitud.entries.filterNot { it.esFinal }.toSet()

    private val transiciones = listOf(
        // ---------- Administrador / coordinador ----------
        Transicion(AccionSolicitud.REVISAR, Rol.ADMIN, setOf(RECIBIDA), EN_REVISION),
        Transicion(AccionSolicitud.CLASIFICAR, Rol.ADMIN, noFinales, null),
        Transicion(AccionSolicitud.CAMBIAR_PRIORIDAD, Rol.ADMIN, noFinales, null),
        Transicion(
            AccionSolicitud.SOLICITAR_INFORMACION, Rol.ADMIN,
            setOf(RECIBIDA, EN_REVISION, ASIGNADA, EN_PROCESO), PENDIENTE_INFORMACION
        ),
        Transicion(AccionSolicitud.ASIGNAR, Rol.ADMIN, setOf(RECIBIDA, EN_REVISION), ASIGNADA),
        Transicion(AccionSolicitud.REASIGNAR, Rol.ADMIN, setOf(ASIGNADA, EN_PROCESO), ASIGNADA),
        Transicion(AccionSolicitud.COMENTAR, Rol.ADMIN, noFinales, null),
        Transicion(AccionSolicitud.CERRAR, Rol.ADMIN, setOf(RESUELTA), CERRADA),
        Transicion(
            AccionSolicitud.CANCELAR, Rol.ADMIN,
            setOf(RECIBIDA, EN_REVISION, PENDIENTE_INFORMACION, ASIGNADA, EN_PROCESO), CANCELADA
        ),

        // ---------- Técnico ----------
        Transicion(AccionSolicitud.INICIAR_TRABAJO, Rol.TECNICO, setOf(ASIGNADA), EN_PROCESO),
        Transicion(AccionSolicitud.REGISTRAR_AVANCE, Rol.TECNICO, setOf(EN_PROCESO), null),
        Transicion(AccionSolicitud.COMENTAR, Rol.TECNICO, setOf(ASIGNADA, EN_PROCESO, PENDIENTE_INFORMACION), null),
        Transicion(AccionSolicitud.ADJUNTAR_EVIDENCIA, Rol.TECNICO, setOf(ASIGNADA, EN_PROCESO), null),
        Transicion(
            AccionSolicitud.SOLICITAR_INFORMACION, Rol.TECNICO,
            setOf(ASIGNADA, EN_PROCESO), PENDIENTE_INFORMACION
        ),
        Transicion(AccionSolicitud.MARCAR_RESUELTA, Rol.TECNICO, setOf(EN_PROCESO), RESUELTA),

        // ---------- Cliente ----------
        // El destino real de RESPONDER_INFORMACION se calcula en estadoDestino()
        Transicion(AccionSolicitud.RESPONDER_INFORMACION, Rol.CLIENTE, setOf(PENDIENTE_INFORMACION), EN_REVISION),
        Transicion(AccionSolicitud.COMENTAR, Rol.CLIENTE, noFinales, null),
        Transicion(
            AccionSolicitud.ADJUNTAR_EVIDENCIA, Rol.CLIENTE,
            setOf(RECIBIDA, EN_REVISION, PENDIENTE_INFORMACION), null
        ),
        Transicion(AccionSolicitud.CONFIRMAR_RESOLUCION, Rol.CLIENTE, setOf(RESUELTA), CERRADA),
        Transicion(AccionSolicitud.REABRIR, Rol.CLIENTE, setOf(RESUELTA, CERRADA), EN_REVISION)
    )

    // Solo el cliente registra solicitudes nuevas
    fun puedeCrear(rol: Rol): Boolean = rol == Rol.CLIENTE

    fun puedeEjecutar(rol: Rol, accion: AccionSolicitud, estado: EstadoSolicitud): Boolean =
        transiciones.any { it.accion == accion && it.rol == rol && estado in it.desde }

    // Acciones que la pantalla de detalle debe ofrecer al usuario según su perfil
    fun accionesDisponibles(rol: Rol, estado: EstadoSolicitud): List<AccionSolicitud> =
        transiciones
            .filter { it.rol == rol && estado in it.desde }
            .map { it.accion }
            .distinct()

    /**
     * Estado al que pasa la solicitud después de ejecutar la acción.
     *
     * @param estadoAntesDePendiente estado que tenía la solicitud antes de quedar
     * "Pendiente de información" (se obtiene del historial). Cuando el cliente responde,
     * la solicitud vuelve a ese estado; si no se conoce, vuelve a "En revisión".
     * @throws IllegalArgumentException si el rol no puede ejecutar la acción en ese estado.
     */
    fun estadoDestino(
        rol: Rol,
        accion: AccionSolicitud,
        estadoActual: EstadoSolicitud,
        estadoAntesDePendiente: EstadoSolicitud? = null
    ): EstadoSolicitud {
        val transicion = transiciones.firstOrNull {
            it.accion == accion && it.rol == rol && estadoActual in it.desde
        } ?: throw IllegalArgumentException(
            "${rol.etiqueta} no puede ejecutar '${accion.etiqueta}' en estado '${estadoActual.etiqueta}'"
        )

        if (accion == AccionSolicitud.RESPONDER_INFORMACION) {
            return estadoAntesDePendiente
                ?.takeUnless { it == PENDIENTE_INFORMACION || it.esFinal }
                ?: EN_REVISION
        }
        return transicion.hacia ?: estadoActual
    }
}
