package com.example.solicitudes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solicitudes.model.AccionSolicitud
import com.example.solicitudes.model.Categoria
import com.example.solicitudes.model.Cliente
import com.example.solicitudes.model.DetalleSolicitudUiState
import com.example.solicitudes.model.EvidenciaPendiente
import com.example.solicitudes.model.FlujoSolicitud
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.model.ReglasAcceso
import com.example.solicitudes.model.Rol
import com.example.solicitudes.navigation.Screen
import com.example.solicitudes.notificaciones.Avisos
import com.example.solicitudes.notificaciones.TextosAviso
import com.example.solicitudes.repository.SesionRepository
import com.example.solicitudes.repository.SolicitudesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Detalle de una solicitud, compartido por los tres perfiles:
 *  - Cliente: estado, historial, responder información, confirmar o reabrir (A6, A7)
 *  - Admin: revisar, clasificar, priorizar, asignar, pedir información, cerrar o cancelar (B2, B3)
 *  - Técnico: iniciar trabajo, avances, comentarios, evidencias y solución (B7)
 * Las acciones disponibles salen de FlujoSolicitud, así la pantalla solo muestra lo permitido.
 */
class DetalleSolicitudViewModel(
    savedStateHandle: SavedStateHandle,
    private val repositorio: SolicitudesRepository,
    private val sesion: SesionRepository,
    private val avisos: Avisos
) : ViewModel() {

    private val solicitudId: Long = checkNotNull(savedStateHandle[Screen.DetalleSolicitud.ARG_ID])
    private val usuario = sesion.usuarioRequerido()

    private val _estado = MutableStateFlow(DetalleSolicitudUiState(usuario = usuario))
    val estado: StateFlow<DetalleSolicitudUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repositorio.observarSolicitud(usuario, solicitudId),
                repositorio.observarHistorial(usuario, solicitudId),
                repositorio.observarEvidencias(solicitudId),
                repositorio.observarUsuarios()
            ) { solicitud, historial, evidencias, usuarios ->
                val cliente: Cliente? = solicitud?.let { repositorio.obtenerCliente(it.clienteId) }
                _estado.value.copy(
                    cargando = false,
                    solicitud = solicitud,
                    cliente = cliente,
                    // Si no puede ver la solicitud tampoco ve su historial ni evidencias
                    historial = if (solicitud == null) emptyList() else historial,
                    evidencias = if (solicitud == null) emptyList() else evidencias,
                    nombresUsuarios = usuarios.associate { it.id to it.nombre },
                    tecnicos = usuarios.filter { it.rol == Rol.TECNICO && it.activo },
                    acciones = solicitud?.let { s ->
                        FlujoSolicitud.accionesDisponibles(usuario.rol, s.estado)
                            .filter { it != AccionSolicitud.ADJUNTAR_EVIDENCIA }
                            .filter { ReglasAcceso.puedeEjecutar(usuario, s, it) }
                    }.orEmpty(),
                    puedeAdjuntar = solicitud != null &&
                        ReglasAcceso.puedeEjecutar(usuario, solicitud, AccionSolicitud.ADJUNTAR_EVIDENCIA),
                    ahora = System.currentTimeMillis()
                )
            }.collect { nuevo ->
                // Se conservan los campos que maneja la pantalla (diálogo abierto, mensaje)
                _estado.update { actual ->
                    nuevo.copy(
                        accionEnCurso = actual.accionEnCurso,
                        procesando = actual.procesando,
                        mensaje = actual.mensaje
                    )
                }
            }
        }
    }

    fun abrirAccion(accion: AccionSolicitud) = _estado.update { it.copy(accionEnCurso = accion) }

    fun cerrarDialogo() = _estado.update { it.copy(accionEnCurso = null) }

    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    fun mostrarMensaje(mensaje: String) = _estado.update { it.copy(mensaje = mensaje) }

    fun ejecutar(
        accion: AccionSolicitud,
        texto: String? = null,
        interno: Boolean = false,
        responsableId: Long? = null,
        prioridad: Prioridad? = null,
        categoria: Categoria? = null
    ) {
        if (_estado.value.procesando) return
        _estado.update { it.copy(procesando = true) }
        viewModelScope.launch {
            repositorio.ejecutarAccion(
                usuario, solicitudId, accion, texto, interno, responsableId, prioridad, categoria
            ).onSuccess { actualizada ->
                val responsable = actualizada.responsableId?.let { _estado.value.nombresUsuarios[it] }
                // Los comentarios internos no generan aviso (el cliente no debe enterarse)
                if (!interno) {
                    TextosAviso.para(accion, actualizada, responsable)?.let { (t, m) -> avisos.avisar(t, m) }
                }
                _estado.update {
                    it.copy(procesando = false, accionEnCurso = null, mensaje = "${accion.etiqueta}: listo")
                }
            }.onFailure { error ->
                _estado.update {
                    it.copy(procesando = false, mensaje = error.message ?: "No se pudo completar la acción")
                }
            }
        }
    }

    fun adjuntarEvidencia(evidencia: EvidenciaPendiente) {
        viewModelScope.launch {
            repositorio.adjuntarEvidencia(
                usuario, solicitudId, evidencia.nombreArchivo, evidencia.tipoMime, evidencia.tamanoBytes, evidencia.uri
            ).onSuccess {
                _estado.update { it.copy(mensaje = "Evidencia adjuntada: ${evidencia.nombreArchivo}") }
            }.onFailure { error ->
                _estado.update { it.copy(mensaje = error.message ?: "No se pudo adjuntar la evidencia") }
            }
        }
    }
}
