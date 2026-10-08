package com.example.solicitudes.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.solicitudes.data.AppDatabase
import com.example.solicitudes.data.DatosSemilla
import com.example.solicitudes.model.AccionSolicitud
import com.example.solicitudes.model.Categoria
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.model.Rol
import com.example.solicitudes.model.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Pruebas de la capa de datos con una base de datos Room en memoria.
// Se ejecutan en el emulador o en un dispositivo: gradlew connectedDebugAndroidTest
@RunWith(AndroidJUnit4::class)
class SolicitudesRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: SolicitudesRepository
    private var ahora = 1_800_000_000_000L

    private lateinit var ana: Usuario
    private lateinit var bruno: Usuario
    private lateinit var admin: Usuario
    private lateinit var esteban: Usuario
    private lateinit var fernanda: Usuario

    @Before
    fun crearBaseDeDatos() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        DatosSemilla.poblarSiVacia(db, ahora)
        repo = SolicitudesRepository(db) { ahora }

        ana = repo.autenticar("ana.prueba@demo.test", DatosSemilla.CLAVE_DEMO)!!
        bruno = repo.autenticar("bruno.ejemplo@demo.test", DatosSemilla.CLAVE_DEMO)!!
        admin = repo.autenticar("coordinacion@demo.test", DatosSemilla.CLAVE_DEMO)!!
        esteban = repo.autenticar("tecnico.esteban@demo.test", DatosSemilla.CLAVE_DEMO)!!
        fernanda = repo.autenticar("tecnico.fernanda@demo.test", DatosSemilla.CLAVE_DEMO)!!
    }

    @After
    fun cerrarBaseDeDatos() = db.close()

    @Test
    fun datosSemillaCubrenTodosLosEstados() = runBlocking {
        val todas = repo.observarSolicitudesVisibles(admin).first()
        assertEquals(8, todas.size)
        assertEquals(EstadoSolicitud.entries.toSet(), todas.map { it.estado }.toSet())
        assertTrue(todas.all { it.codigo.matches(Regex("SOL-\\d{4}-\\d{4}")) })
        assertEquals(todas.size, todas.map { it.codigo }.toSet().size)
    }

    @Test
    fun datosSemillaNoSeDuplicanAlVolverAPoblar() = runBlocking {
        DatosSemilla.poblarSiVacia(db, ahora)
        assertEquals(8, repo.observarSolicitudesVisibles(admin).first().size)
    }

    @Test
    fun loginRechazaClaveIncorrectaYCorreoInexistente() = runBlocking {
        assertNull(repo.autenticar("ana.prueba@demo.test", "otra"))
        assertNull(repo.autenticar("nadie@demo.test", DatosSemilla.CLAVE_DEMO))
        assertEquals(Rol.ADMIN, admin.rol)
    }

    @Test
    fun cadaPerfilVeSoloLoQueLeCorresponde() = runBlocking {
        val deAna = repo.observarSolicitudesVisibles(ana).first()
        assertTrue(deAna.isNotEmpty())
        assertTrue(deAna.all { it.clienteId == ana.clienteId })

        val deEsteban = repo.observarSolicitudesVisibles(esteban).first()
        assertTrue(deEsteban.isNotEmpty())
        assertTrue(deEsteban.all { it.responsableId == esteban.id })

        // Bruno no puede abrir una solicitud de Ana aunque conozca su id
        assertNull(repo.observarSolicitud(bruno, deAna.first().id).first())
    }

    @Test
    fun clienteNoVeComentariosInternos() = runBlocking {
        val ssl = repo.observarSolicitudesVisibles(admin).first().first { it.estado == EstadoSolicitud.EN_PROCESO }
        val paraAdmin = repo.observarHistorial(admin, ssl.id).first()
        val paraCliente = repo.observarHistorial(bruno, ssl.id).first()
        assertTrue(paraAdmin.any { it.interno })
        assertFalse(paraCliente.any { it.interno })
    }

    @Test
    fun flujoCompletoDesdeRegistroHastaCierreQuedaEnElHistorial() = runBlocking {
        val creada = repo.crearSolicitud(
            ana, "Cambiar logo del sitio", "Subir el logo nuevo de prueba",
            Categoria.SOLICITUD_CAMBIO, "Sitio web"
        ).getOrThrow()
        assertEquals(EstadoSolicitud.RECIBIDA, creada.estado)
        assertEquals(ahora, creada.fechaCreacion)
        assertTrue(creada.codigo.startsWith("SOL-"))

        ahora += 1_000
        repo.ejecutarAccion(admin, creada.id, AccionSolicitud.REVISAR).getOrThrow()
        repo.ejecutarAccion(admin, creada.id, AccionSolicitud.CAMBIAR_PRIORIDAD, prioridad = Prioridad.ALTA).getOrThrow()
        repo.ejecutarAccion(admin, creada.id, AccionSolicitud.ASIGNAR, responsableId = fernanda.id).getOrThrow()
        repo.ejecutarAccion(fernanda, creada.id, AccionSolicitud.INICIAR_TRABAJO).getOrThrow()
        repo.ejecutarAccion(fernanda, creada.id, AccionSolicitud.MARCAR_RESUELTA, "Logo reemplazado").getOrThrow()
        val cerrada = repo.ejecutarAccion(ana, creada.id, AccionSolicitud.CONFIRMAR_RESOLUCION).getOrThrow()

        assertEquals(EstadoSolicitud.CERRADA, cerrada.estado)
        assertTrue(cerrada.confirmacionCierre)
        assertEquals("Logo reemplazado", cerrada.solucion)

        val historial = repo.observarHistorial(admin, creada.id).first()
        assertEquals(
            listOf(
                AccionSolicitud.CREAR, AccionSolicitud.REVISAR, AccionSolicitud.CAMBIAR_PRIORIDAD,
                AccionSolicitud.ASIGNAR, AccionSolicitud.INICIAR_TRABAJO,
                AccionSolicitud.MARCAR_RESUELTA, AccionSolicitud.CONFIRMAR_RESOLUCION
            ),
            historial.map { it.accion }
        )
        // Cada evento conserva usuario y fecha (criterio 21)
        val asignacion = historial.first { it.accion == AccionSolicitud.ASIGNAR }
        assertEquals(admin.id, asignacion.usuarioId)
        assertEquals(ahora, asignacion.fecha)
        assertTrue(asignacion.comentario!!.contains(fernanda.nombre))
    }

    @Test
    fun accionNoPermitidaFallaYNoDejaRastro() = runBlocking {
        val nueva = repo.observarSolicitudesVisibles(admin).first().first { it.estado == EstadoSolicitud.RECIBIDA }
        val eventosAntes = repo.observarHistorial(admin, nueva.id).first().size

        val resultado = repo.ejecutarAccion(ana, nueva.id, AccionSolicitud.CERRAR)
        assertTrue(resultado.isFailure)
        assertNotNull(resultado.exceptionOrNull()?.message)

        assertEquals(eventosAntes, repo.observarHistorial(admin, nueva.id).first().size)
        assertEquals(EstadoSolicitud.RECIBIDA, repo.observarSolicitud(admin, nueva.id).first()!!.estado)
    }

    @Test
    fun respuestaDelClienteDevuelveLaSolicitudARevision() = runBlocking {
        val pendiente = repo.observarSolicitudesVisibles(admin).first()
            .first { it.estado == EstadoSolicitud.PENDIENTE_INFORMACION }
        val carla = repo.autenticar("carla.muestra@demo.test", DatosSemilla.CLAVE_DEMO)!!

        val respondida = repo.ejecutarAccion(
            carla, pendiente.id, AccionSolicitud.RESPONDER_INFORMACION, "Adjuntamos textos de prueba"
        ).getOrThrow()
        assertEquals(EstadoSolicitud.EN_REVISION, respondida.estado)
    }

    @Test
    fun evidenciaInvalidaSeRechaza() = runBlocking {
        val nueva = repo.observarSolicitudesVisibles(ana).first().first { it.estado == EstadoSolicitud.RECIBIDA }
        assertTrue(repo.adjuntarEvidencia(ana, nueva.id, "video.mp4", "video/mp4", 1_000, "demo://video").isFailure)
        assertTrue(repo.adjuntarEvidencia(ana, nueva.id, "captura.png", "image/png", 1_000, "demo://captura").isSuccess)
        assertEquals(1, repo.observarEvidencias(nueva.id).first().size)
    }

    @Test
    fun conteoPorEstadoCoincideConLosRegistros() = runBlocking {
        val todas = repo.observarSolicitudesVisibles(admin).first()
        val conteo = repo.observarConteoPorEstado().first().associate { it.estado to it.cantidad }
        EstadoSolicitud.entries.forEach { estado ->
            assertEquals(estado.etiqueta, todas.count { it.estado == estado }, conteo[estado] ?: 0)
        }
    }
}
