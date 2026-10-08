package com.example.solicitudes.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltrosYResumenTest {

    private val hora = 3_600_000L
    private val ahora = 1_000 * hora

    private fun solicitud(
        id: Long, estado: EstadoSolicitud, prioridad: Prioridad?, clienteId: Long,
        responsableId: Long?, horasAtras: Long, titulo: String = "Solicitud $id"
    ) = Solicitud(
        id = id, codigo = "SOL-2026-%04d".format(id), clienteId = clienteId, creadorId = 1,
        titulo = titulo, descripcion = "Descripción de prueba", categoria = Categoria.CONSULTA,
        servicioRelacionado = "Sitio web", prioridad = prioridad, estado = estado,
        responsableId = responsableId, fechaCreacion = ahora - horasAtras * hora
    )

    private val lista = listOf(
        solicitud(1, EstadoSolicitud.RECIBIDA, null, 10, null, 2, "Cambiar banner"),
        solicitud(2, EstadoSolicitud.ASIGNADA, Prioridad.URGENTE, 10, 7, 100),       // atrasada
        solicitud(3, EstadoSolicitud.EN_PROCESO, Prioridad.ALTA, 20, 7, 10),
        solicitud(4, EstadoSolicitud.PENDIENTE_INFORMACION, Prioridad.MEDIA, 20, null, 200),
        solicitud(5, EstadoSolicitud.CERRADA, Prioridad.BAJA, 30, 8, 300)
    )

    @Test
    fun sinFiltrosDevuelveTodo() {
        val filtros = FiltrosSolicitud()
        assertFalse(filtros.hayFiltrosActivos)
        assertEquals(5, filtros.aplicar(lista, ahora).size)
    }

    @Test
    fun filtraPorEstadoPrioridadClienteYResponsable() {
        assertEquals(listOf(3L), FiltrosSolicitud(estado = EstadoSolicitud.EN_PROCESO).aplicar(lista, ahora).map { it.id })
        assertEquals(listOf(2L), FiltrosSolicitud(prioridad = Prioridad.URGENTE).aplicar(lista, ahora).map { it.id })
        assertEquals(listOf(3L, 4L), FiltrosSolicitud(clienteId = 20).aplicar(lista, ahora).map { it.id })
        assertEquals(listOf(2L, 3L), FiltrosSolicitud(responsableId = 7).aplicar(lista, ahora).map { it.id })
    }

    @Test
    fun combinaFiltros() {
        val filtros = FiltrosSolicitud(clienteId = 10, responsableId = 7)
        assertTrue(filtros.hayFiltrosActivos)
        assertEquals(listOf(2L), filtros.aplicar(lista, ahora).map { it.id })
    }

    @Test
    fun buscaPorCodigoOTituloSinImportarMayusculas() {
        assertEquals(listOf(1L), FiltrosSolicitud(texto = "banner").aplicar(lista, ahora).map { it.id })
        assertEquals(listOf(4L), FiltrosSolicitud(texto = "sol-2026-0004").aplicar(lista, ahora).map { it.id })
    }

    @Test
    fun soloAtrasadas() {
        assertEquals(listOf(2L), FiltrosSolicitud(soloAtrasadas = true).aplicar(lista, ahora).map { it.id })
    }

    @Test
    fun resumenCuentaPorEstadoPrioridadYResponsable() {
        val resumen = ResumenSolicitudes.calcular(lista, ahora)
        assertEquals(5, resumen.total)
        assertEquals(EstadoSolicitud.entries.size, resumen.porEstado.size)
        assertEquals(1, resumen.porEstado[EstadoSolicitud.RECIBIDA])
        assertEquals(0, resumen.porEstado[EstadoSolicitud.CANCELADA])
        assertEquals(resumen.total, resumen.porEstado.values.sum())
        assertEquals(1, resumen.porPrioridad[null])
        assertEquals(1, resumen.porPrioridad[Prioridad.URGENTE])
        assertEquals(2, resumen.porResponsable[7L])
        assertEquals(null, resumen.porResponsable[8L])   // la cerrada no cuenta como carga vigente
        assertEquals(1, resumen.atrasadas)
    }

    @Test
    fun mensajeWhatsAppNoIncluyeDescripcion() {
        val s = lista[0].copy(descripcion = "Dato interno que no debe salir")
        val mensaje = MensajeWhatsApp.construir(s)
        assertTrue(mensaje.contains(s.codigo))
        assertTrue(mensaje.contains(s.titulo))
        assertTrue(mensaje.contains(s.categoria.etiqueta))
        assertTrue(mensaje.contains(MensajeWhatsApp.referencia(s.codigo)))
        assertFalse(mensaje.contains(s.descripcion))
    }
}
