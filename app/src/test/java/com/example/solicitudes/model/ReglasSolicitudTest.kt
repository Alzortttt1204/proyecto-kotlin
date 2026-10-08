package com.example.solicitudes.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasSolicitudTest {

    private val hora = 3_600_000L
    private val creacion = 1_000_000_000L

    private fun solicitud(estado: EstadoSolicitud, prioridad: Prioridad?) = Solicitud(
        codigo = "SOL-2026-0001",
        clienteId = 1,
        creadorId = 1,
        titulo = "Sitio caído",
        descripcion = "El sitio de prueba no carga",
        categoria = Categoria.INCIDENTE_TECNICO,
        servicioRelacionado = "Hosting",
        prioridad = prioridad,
        estado = estado,
        fechaCreacion = creacion
    )

    @Test
    fun codigoTieneFormatoConCuatroDigitos() {
        assertEquals("SOL-2026-0007", ReglasSolicitud.generarCodigo(7, 2026))
        assertEquals("SOL-2026-1234", ReglasSolicitud.generarCodigo(1234, 2026))
    }

    @Test(expected = IllegalArgumentException::class)
    fun codigoNoAceptaCorrelativoCero() {
        ReglasSolicitud.generarCodigo(0, 2026)
    }

    @Test
    fun urgenteSeAtrasaDespuesDe24Horas() {
        val s = solicitud(EstadoSolicitud.EN_PROCESO, Prioridad.URGENTE)
        assertFalse(ReglasSolicitud.estaAtrasada(s, creacion + 24 * hora))
        assertTrue(ReglasSolicitud.estaAtrasada(s, creacion + 24 * hora + 1))
    }

    @Test
    fun sinPrioridadUsaPlazoMedio() {
        val s = solicitud(EstadoSolicitud.RECIBIDA, prioridad = null)
        assertFalse(ReglasSolicitud.estaAtrasada(s, creacion + 72 * hora))
        assertTrue(ReglasSolicitud.estaAtrasada(s, creacion + 73 * hora))
    }

    @Test
    fun resueltasCerradasCanceladasYPendientesNoSeAtrasan() {
        val muchoDespues = creacion + 1_000 * hora
        listOf(
            EstadoSolicitud.RESUELTA,
            EstadoSolicitud.CERRADA,
            EstadoSolicitud.CANCELADA,
            EstadoSolicitud.PENDIENTE_INFORMACION
        ).forEach { estado ->
            assertFalse(estado.etiqueta, ReglasSolicitud.estaAtrasada(solicitud(estado, Prioridad.URGENTE), muchoDespues))
        }
    }
}
