package com.example.solicitudes.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasAccesoTest {

    private val clienteA = Usuario(id = 1, nombre = "Cliente A", correo = "a@demo.test", rol = Rol.CLIENTE, clienteId = 10)
    private val clienteB = Usuario(id = 2, nombre = "Cliente B", correo = "b@demo.test", rol = Rol.CLIENTE, clienteId = 20)
    private val admin = Usuario(id = 3, nombre = "Admin", correo = "admin@demo.test", rol = Rol.ADMIN)
    private val tecnico1 = Usuario(id = 4, nombre = "Técnico 1", correo = "t1@demo.test", rol = Rol.TECNICO)
    private val tecnico2 = Usuario(id = 5, nombre = "Técnico 2", correo = "t2@demo.test", rol = Rol.TECNICO)

    private val solicitudDeA = Solicitud(
        id = 100,
        codigo = "SOL-2026-0001",
        clienteId = 10,
        creadorId = 1,
        titulo = "Cambio de banner",
        descripcion = "Actualizar banner de la portada",
        categoria = Categoria.SOLICITUD_CAMBIO,
        servicioRelacionado = "Sitio web",
        estado = EstadoSolicitud.ASIGNADA,
        responsableId = 4,
        fechaCreacion = 0
    )

    @Test
    fun clienteSoloVeSusSolicitudes() {
        assertTrue(ReglasAcceso.puedeVer(clienteA, solicitudDeA))
        assertFalse(ReglasAcceso.puedeVer(clienteB, solicitudDeA))
    }

    @Test
    fun tecnicoSoloVeLasAsignadas() {
        assertTrue(ReglasAcceso.puedeVer(tecnico1, solicitudDeA))
        assertFalse(ReglasAcceso.puedeVer(tecnico2, solicitudDeA))
    }

    @Test
    fun adminVeTodas() {
        assertTrue(ReglasAcceso.puedeVer(admin, solicitudDeA))
    }

    @Test
    fun clienteNoVeComentariosInternos() {
        val interno = EventoHistorial(
            solicitudId = 100, estadoAnterior = null, estadoNuevo = EstadoSolicitud.ASIGNADA,
            accion = AccionSolicitud.COMENTAR, comentario = "Nota interna", interno = true,
            rol = Rol.ADMIN, usuarioId = 3, fecha = 0
        )
        assertFalse(ReglasAcceso.puedeVerEvento(Rol.CLIENTE, interno))
        assertTrue(ReglasAcceso.puedeVerEvento(Rol.TECNICO, interno))
        assertTrue(ReglasAcceso.puedeVerEvento(Rol.CLIENTE, interno.copy(interno = false)))
    }

    @Test
    fun tecnicoNoAsignadoNoPuedeActuar() {
        assertTrue(ReglasAcceso.puedeEjecutar(tecnico1, solicitudDeA, AccionSolicitud.INICIAR_TRABAJO))
        assertFalse(ReglasAcceso.puedeEjecutar(tecnico2, solicitudDeA, AccionSolicitud.INICIAR_TRABAJO))
    }

    @Test
    fun usuarioInactivoNoPuedeActuar() {
        assertFalse(ReglasAcceso.puedeEjecutar(admin.copy(activo = false), solicitudDeA, AccionSolicitud.COMENTAR))
    }
}
