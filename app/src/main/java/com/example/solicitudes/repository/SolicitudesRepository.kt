package com.example.solicitudes.repository

import androidx.room.withTransaction
import com.example.solicitudes.data.AppDatabase
import com.example.solicitudes.data.dao.ConteoPorEstado
import com.example.solicitudes.model.AccionSolicitud
import com.example.solicitudes.model.Categoria
import com.example.solicitudes.model.Cliente
import com.example.solicitudes.model.EventoHistorial
import com.example.solicitudes.model.Evidencia
import com.example.solicitudes.model.FlujoSolicitud
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.model.ReglasAcceso
import com.example.solicitudes.model.ReglasEvidencia
import com.example.solicitudes.model.ReglasSolicitud
import com.example.solicitudes.model.Rol
import com.example.solicitudes.model.Solicitud
import com.example.solicitudes.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId

/**
 * Único punto de acceso a los datos para los ViewModels.
 *
 * Toda modificación de una solicitud pasa por aquí para que:
 *  - se respeten los permisos de cada perfil (ReglasAcceso / FlujoSolicitud), y
 *  - cada cambio deje un evento en el historial con fecha, usuario y acción.
 *
 * Las operaciones devuelven Result: si algo no está permitido, el ViewModel
 * puede mostrar exceptionOrNull()?.message al usuario.
 *
 * @param reloj fuente de la hora actual (se puede reemplazar en pruebas y datos semilla).
 */
class SolicitudesRepository(
    private val db: AppDatabase,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    private val clienteDao = db.clienteDao()
    private val usuarioDao = db.usuarioDao()
    private val solicitudDao = db.solicitudDao()
    private val eventoDao = db.eventoHistorialDao()
    private val evidenciaDao = db.evidenciaDao()

    // ---------------------------------------------------------------------
    // Usuarios y sesión
    // ---------------------------------------------------------------------

    // Devuelve el usuario si el correo y la clave coinciden y la cuenta está activa
    suspend fun autenticar(correo: String, clave: String): Usuario? {
        val usuario = usuarioDao.obtenerPorCorreo(correo.trim().lowercase()) ?: return null
        return usuario.takeIf { it.activo && Credenciales.coincide(clave, it.claveHash) }
    }

    suspend fun obtenerUsuario(id: Long): Usuario? = usuarioDao.obtenerPorId(id)

    fun observarUsuarios(): Flow<List<Usuario>> = usuarioDao.observarTodos()

    fun observarTecnicos(): Flow<List<Usuario>> = usuarioDao.observarPorRol(Rol.TECNICO)

    suspend fun obtenerCliente(id: Long): Cliente? = clienteDao.obtenerPorId(id)

    fun observarClientes(): Flow<List<Cliente>> = clienteDao.observarTodos()

    // ---------------------------------------------------------------------
    // Consultas de solicitudes (ya filtradas según el perfil)
    // ---------------------------------------------------------------------

    // Cliente: solo las suyas. Técnico: solo las asignadas. Admin: todas.
    fun observarSolicitudesVisibles(usuario: Usuario): Flow<List<Solicitud>> = when (usuario.rol) {
        Rol.ADMIN -> solicitudDao.observarTodas()
        Rol.CLIENTE -> solicitudDao.observarPorCliente(usuario.clienteId ?: -1L)
        Rol.TECNICO -> solicitudDao.observarPorResponsable(usuario.id)
    }

    // Emite null si la solicitud no existe o el usuario no tiene permiso para verla
    fun observarSolicitud(usuario: Usuario, solicitudId: Long): Flow<Solicitud?> =
        solicitudDao.observarPorId(solicitudId).map { solicitud ->
            solicitud?.takeIf { ReglasAcceso.puedeVer(usuario, it) }
        }

    // Historial cronológico; el cliente no recibe los comentarios internos
    fun observarHistorial(usuario: Usuario, solicitudId: Long): Flow<List<EventoHistorial>> =
        eventoDao.observarPorSolicitud(solicitudId).map { eventos ->
            eventos.filter { ReglasAcceso.puedeVerEvento(usuario.rol, it) }
        }

    fun observarEvidencias(solicitudId: Long): Flow<List<Evidencia>> =
        evidenciaDao.observarPorSolicitud(solicitudId)

    fun observarConteoPorEstado(): Flow<List<ConteoPorEstado>> = solicitudDao.observarConteoPorEstado()

    // ---------------------------------------------------------------------
    // Crear solicitud (perfil cliente)
    // ---------------------------------------------------------------------

    suspend fun crearSolicitud(
        usuario: Usuario,
        titulo: String,
        descripcion: String,
        categoria: Categoria,
        servicioRelacionado: String,
        activoRelacionado: String? = null
    ): Result<Solicitud> = runCatching {
        require(FlujoSolicitud.puedeCrear(usuario.rol)) { "Solo un cliente puede registrar solicitudes." }
        val clienteId = requireNotNull(usuario.clienteId) { "El usuario no está asociado a un cliente." }
        require(titulo.isNotBlank()) { "El título es obligatorio." }
        require(descripcion.isNotBlank()) { "La descripción es obligatoria." }

        val ahora = reloj()
        db.withTransaction {
            // 1) Se inserta para obtener el id autogenerado
            val id = solicitudDao.insertar(
                Solicitud(
                    codigo = "",
                    clienteId = clienteId,
                    creadorId = usuario.id,
                    titulo = titulo.trim(),
                    descripcion = descripcion.trim(),
                    categoria = categoria,
                    servicioRelacionado = servicioRelacionado.trim(),
                    activoRelacionado = activoRelacionado?.trim()?.ifBlank { null },
                    estado = FlujoSolicitud.estadoInicial,
                    fechaCreacion = ahora
                )
            )
            // 2) Con el id se genera el código único de seguimiento
            val anio = Instant.ofEpochMilli(ahora).atZone(ZoneId.systemDefault()).year
            val creada = solicitudDao.obtenerPorId(id)!!
                .copy(codigo = ReglasSolicitud.generarCodigo(id, anio))
            solicitudDao.actualizar(creada)

            // 3) Primer evento del historial
            eventoDao.insertar(
                EventoHistorial(
                    solicitudId = id,
                    estadoAnterior = null,
                    estadoNuevo = creada.estado,
                    accion = AccionSolicitud.CREAR,
                    rol = usuario.rol,
                    usuarioId = usuario.id,
                    fecha = ahora
                )
            )
            creada
        }
    }

    // ---------------------------------------------------------------------
    // Acciones sobre una solicitud (los tres perfiles)
    // ---------------------------------------------------------------------

    /**
     * Ejecuta una acción del flujo y la registra en el historial.
     *
     * @param texto comentario, motivo de cancelación o solución, según la acción.
     * @param interno true para comentarios que el cliente no debe ver (solo admin y técnico).
     * @param responsableId técnico a asignar (ASIGNAR / REASIGNAR).
     * @param prioridad nueva prioridad (CAMBIAR_PRIORIDAD).
     * @param categoria nueva categoría (CLASIFICAR).
     */
    suspend fun ejecutarAccion(
        usuario: Usuario,
        solicitudId: Long,
        accion: AccionSolicitud,
        texto: String? = null,
        interno: Boolean = false,
        responsableId: Long? = null,
        prioridad: Prioridad? = null,
        categoria: Categoria? = null
    ): Result<Solicitud> = runCatching {
        require(accion != AccionSolicitud.CREAR) { "Para crear una solicitud usa crearSolicitud()." }
        val textoLimpio = texto?.trim()?.ifBlank { null }
        require(!accion.requiereTexto || textoLimpio != null) {
            "Debes ingresar un comentario para \"${accion.etiqueta}\"."
        }
        require(!interno || usuario.rol != Rol.CLIENTE) { "El cliente no puede registrar comentarios internos." }

        db.withTransaction {
            val actual = requireNotNull(solicitudDao.obtenerPorId(solicitudId)) { "La solicitud no existe." }
            require(ReglasAcceso.puedeEjecutar(usuario, actual, accion)) {
                "No tienes permiso para \"${accion.etiqueta}\" en una solicitud ${actual.estado.etiqueta.lowercase()}."
            }

            val antesDePendiente = if (accion == AccionSolicitud.RESPONDER_INFORMACION) {
                eventoDao.ultimoPasoAPendiente(solicitudId)?.estadoAnterior
            } else null

            val nuevoEstado = FlujoSolicitud.estadoDestino(usuario.rol, accion, actual.estado, antesDePendiente)
            val ahora = reloj()

            // Cambios propios de cada acción, además del estado.
            // "detalle" deja en el historial el dato que cambió (responsable, prioridad, categoría).
            var actualizada = actual.copy(estado = nuevoEstado, fechaActualizacion = ahora)
            var detalle: String? = null
            when (accion) {
                AccionSolicitud.ASIGNAR, AccionSolicitud.REASIGNAR -> {
                    val tecnico = responsableId?.let { usuarioDao.obtenerPorId(it) }
                    require(tecnico != null && tecnico.rol == Rol.TECNICO && tecnico.activo) {
                        "Debes elegir un técnico activo como responsable."
                    }
                    require(accion != AccionSolicitud.REASIGNAR || tecnico.id != actual.responsableId) {
                        "La solicitud ya está asignada a ese técnico."
                    }
                    actualizada = actualizada.copy(responsableId = tecnico.id)
                    detalle = "Responsable: ${tecnico.nombre}"
                }
                AccionSolicitud.CAMBIAR_PRIORIDAD -> {
                    val nueva = requireNotNull(prioridad) { "Debes elegir una prioridad." }
                    actualizada = actualizada.copy(prioridad = nueva)
                    detalle = "Prioridad: ${nueva.etiqueta}"
                }
                AccionSolicitud.CLASIFICAR -> {
                    val nueva = requireNotNull(categoria) { "Debes elegir una categoría." }
                    actualizada = actualizada.copy(categoria = nueva)
                    detalle = "Categoría: ${nueva.etiqueta}"
                }
                AccionSolicitud.MARCAR_RESUELTA -> {
                    actualizada = actualizada.copy(solucion = textoLimpio, fechaResolucion = ahora)
                }
                AccionSolicitud.CONFIRMAR_RESOLUCION -> {
                    actualizada = actualizada.copy(confirmacionCierre = true)
                }
                AccionSolicitud.CANCELAR -> {
                    actualizada = actualizada.copy(motivoCancelacion = textoLimpio)
                }
                AccionSolicitud.REABRIR -> {
                    // La solución anterior queda en el historial; la solicitud vuelve a revisión
                    actualizada = actualizada.copy(confirmacionCierre = false, fechaResolucion = null)
                }
                else -> Unit
            }
            solicitudDao.actualizar(actualizada)

            eventoDao.insertar(
                EventoHistorial(
                    solicitudId = solicitudId,
                    estadoAnterior = actual.estado,
                    estadoNuevo = nuevoEstado,
                    accion = accion,
                    comentario = listOfNotNull(detalle, textoLimpio).joinToString(" · ").ifBlank { null },
                    interno = interno,
                    rol = usuario.rol,
                    usuarioId = usuario.id,
                    fecha = ahora
                )
            )
            actualizada
        }
    }

    // ---------------------------------------------------------------------
    // Evidencias
    // ---------------------------------------------------------------------

    suspend fun adjuntarEvidencia(
        usuario: Usuario,
        solicitudId: Long,
        nombreArchivo: String,
        tipoMime: String?,
        tamanoBytes: Long,
        uri: String,
        descripcion: String? = null
    ): Result<Evidencia> = runCatching {
        ReglasEvidencia.validar(tipoMime, tamanoBytes)?.let { error -> throw IllegalArgumentException(error) }

        db.withTransaction {
            val actual = requireNotNull(solicitudDao.obtenerPorId(solicitudId)) { "La solicitud no existe." }
            require(ReglasAcceso.puedeEjecutar(usuario, actual, AccionSolicitud.ADJUNTAR_EVIDENCIA)) {
                "No puedes adjuntar evidencias en una solicitud ${actual.estado.etiqueta.lowercase()}."
            }
            val ahora = reloj()
            val evidencia = Evidencia(
                solicitudId = solicitudId,
                nombreArchivo = nombreArchivo,
                tipoMime = tipoMime!!,
                tamanoBytes = tamanoBytes,
                uri = uri,
                usuarioId = usuario.id,
                fecha = ahora,
                descripcion = descripcion?.trim()?.ifBlank { null }
            )
            val id = evidenciaDao.insertar(evidencia)
            solicitudDao.actualizar(actual.copy(fechaActualizacion = ahora))
            eventoDao.insertar(
                EventoHistorial(
                    solicitudId = solicitudId,
                    estadoAnterior = actual.estado,
                    estadoNuevo = actual.estado,
                    accion = AccionSolicitud.ADJUNTAR_EVIDENCIA,
                    comentario = nombreArchivo,
                    rol = usuario.rol,
                    usuarioId = usuario.id,
                    fecha = ahora,
                    evidenciaId = id
                )
            )
            evidencia.copy(id = id)
        }
    }

}
