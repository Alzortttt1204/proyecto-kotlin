package com.example.solicitudes.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlujoSolicitudTest {

    @Test
    fun solicitudNuevaComienzaRecibida() {
        assertEquals(EstadoSolicitud.RECIBIDA, FlujoSolicitud.estadoInicial)
    }

    @Test
    fun soloElClientePuedeCrearSolicitudes() {
        assertTrue(FlujoSolicitud.puedeCrear(Rol.CLIENTE))
        assertFalse(FlujoSolicitud.puedeCrear(Rol.ADMIN))
        assertFalse(FlujoSolicitud.puedeCrear(Rol.TECNICO))
    }

    @Test
    fun flujoCompletoDesdeRecibidaHastaCerrada() {
        var estado = FlujoSolicitud.estadoInicial
        estado = FlujoSolicitud.estadoDestino(Rol.ADMIN, AccionSolicitud.REVISAR, estado)
        assertEquals(EstadoSolicitud.EN_REVISION, estado)
        estado = FlujoSolicitud.estadoDestino(Rol.ADMIN, AccionSolicitud.ASIGNAR, estado)
        assertEquals(EstadoSolicitud.ASIGNADA, estado)
        estado = FlujoSolicitud.estadoDestino(Rol.TECNICO, AccionSolicitud.INICIAR_TRABAJO, estado)
        assertEquals(EstadoSolicitud.EN_PROCESO, estado)
        estado = FlujoSolicitud.estadoDestino(Rol.TECNICO, AccionSolicitud.MARCAR_RESUELTA, estado)
        assertEquals(EstadoSolicitud.RESUELTA, estado)
        estado = FlujoSolicitud.estadoDestino(Rol.CLIENTE, AccionSolicitud.CONFIRMAR_RESOLUCION, estado)
        assertEquals(EstadoSolicitud.CERRADA, estado)
    }

    @Test
    fun clienteNoPuedeCerrarDirectamenteNiPriorizarNiAsignar() {
        EstadoSolicitud.entries.forEach { estado ->
            assertFalse(FlujoSolicitud.puedeEjecutar(Rol.CLIENTE, AccionSolicitud.CERRAR, estado))
            assertFalse(FlujoSolicitud.puedeEjecutar(Rol.CLIENTE, AccionSolicitud.CAMBIAR_PRIORIDAD, estado))
            assertFalse(FlujoSolicitud.puedeEjecutar(Rol.CLIENTE, AccionSolicitud.ASIGNAR, estado))
        }
    }

    @Test
    fun tecnicoNoPuedeCerrarNiAsignar() {
        EstadoSolicitud.entries.forEach { estado ->
            assertFalse(FlujoSolicitud.puedeEjecutar(Rol.TECNICO, AccionSolicitud.CERRAR, estado))
            assertFalse(FlujoSolicitud.puedeEjecutar(Rol.TECNICO, AccionSolicitud.ASIGNAR, estado))
            assertFalse(FlujoSolicitud.puedeEjecutar(Rol.TECNICO, AccionSolicitud.REASIGNAR, estado))
        }
    }

    @Test
    fun clienteSoloConfirmaUnaSolicitudResuelta() {
        assertTrue(FlujoSolicitud.puedeEjecutar(Rol.CLIENTE, AccionSolicitud.CONFIRMAR_RESOLUCION, EstadoSolicitud.RESUELTA))
        assertFalse(FlujoSolicitud.puedeEjecutar(Rol.CLIENTE, AccionSolicitud.CONFIRMAR_RESOLUCION, EstadoSolicitud.EN_PROCESO))
    }

    @Test
    fun solicitudCerradaSoloAdmiteReapertura() {
        Rol.entries.forEach { rol ->
            val acciones = FlujoSolicitud.accionesDisponibles(rol, EstadoSolicitud.CERRADA)
            val esperadas = if (rol == Rol.CLIENTE) listOf(AccionSolicitud.REABRIR) else emptyList()
            assertEquals("Acciones de ${rol.etiqueta} en Cerrada", esperadas, acciones)
        }
    }

    @Test
    fun solicitudCanceladaNoAdmiteAcciones() {
        Rol.entries.forEach { rol ->
            assertTrue(FlujoSolicitud.accionesDisponibles(rol, EstadoSolicitud.CANCELADA).isEmpty())
        }
    }

    @Test
    fun reaperturaVuelveARevision() {
        assertEquals(
            EstadoSolicitud.EN_REVISION,
            FlujoSolicitud.estadoDestino(Rol.CLIENTE, AccionSolicitud.REABRIR, EstadoSolicitud.CERRADA)
        )
    }

    @Test
    fun respuestaDelClienteVuelveAlEstadoAnteriorALaPeticion() {
        assertEquals(
            EstadoSolicitud.EN_PROCESO,
            FlujoSolicitud.estadoDestino(
                Rol.CLIENTE, AccionSolicitud.RESPONDER_INFORMACION,
                EstadoSolicitud.PENDIENTE_INFORMACION,
                estadoAntesDePendiente = EstadoSolicitud.EN_PROCESO
            )
        )
    }

    @Test
    fun respuestaSinEstadoAnteriorVuelveARevision() {
        assertEquals(
            EstadoSolicitud.EN_REVISION,
            FlujoSolicitud.estadoDestino(
                Rol.CLIENTE, AccionSolicitud.RESPONDER_INFORMACION,
                EstadoSolicitud.PENDIENTE_INFORMACION
            )
        )
    }

    @Test
    fun comentarioNoCambiaElEstado() {
        assertEquals(
            EstadoSolicitud.EN_PROCESO,
            FlujoSolicitud.estadoDestino(Rol.TECNICO, AccionSolicitud.COMENTAR, EstadoSolicitud.EN_PROCESO)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun transicionNoPermitidaLanzaExcepcion() {
        FlujoSolicitud.estadoDestino(Rol.TECNICO, AccionSolicitud.MARCAR_RESUELTA, EstadoSolicitud.RECIBIDA)
    }
}
