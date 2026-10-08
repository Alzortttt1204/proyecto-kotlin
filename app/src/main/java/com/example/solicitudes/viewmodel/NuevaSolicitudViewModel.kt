package com.example.solicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solicitudes.model.Categoria
import com.example.solicitudes.model.EvidenciaPendiente
import com.example.solicitudes.model.NuevaSolicitudErrores
import com.example.solicitudes.model.NuevaSolicitudUiState
import com.example.solicitudes.notificaciones.Avisos
import com.example.solicitudes.notificaciones.TextosAviso
import com.example.solicitudes.model.AccionSolicitud
import com.example.solicitudes.repository.Borrador
import com.example.solicitudes.repository.BorradorRepository
import com.example.solicitudes.repository.SesionRepository
import com.example.solicitudes.repository.SolicitudesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Formulario "Nueva solicitud" del cliente (tarjetas A3, A4 y A5).
 * Valida los campos (patrón de la guía 11), permite guardar un borrador,
 * adjuntar evidencias y, al enviar, crea la solicitud con su código único.
 */
class NuevaSolicitudViewModel(
    private val repositorio: SolicitudesRepository,
    private val sesion: SesionRepository,
    private val borradores: BorradorRepository,
    private val avisos: Avisos
) : ViewModel() {

    private val _estado = MutableStateFlow(NuevaSolicitudUiState())
    val estado: StateFlow<NuevaSolicitudUiState> = _estado.asStateFlow()

    private val usuario = sesion.usuarioRequerido()

    init {
        // Recupera el borrador guardado y los servicios contratados por el cliente
        val borrador = borradores.obtener(usuario.id)
        _estado.update {
            it.copy(
                titulo = borrador.titulo,
                descripcion = borrador.descripcion,
                categoria = borrador.categoria,
                servicio = borrador.servicio,
                activo = borrador.activo,
                mensaje = if (borrador.estaVacio) null else "Se cargó tu borrador guardado"
            )
        }
        viewModelScope.launch {
            val servicios = usuario.clienteId?.let { repositorio.obtenerCliente(it) }?.servicios.orEmpty()
            _estado.update { it.copy(serviciosDelCliente = servicios + OTRO_SERVICIO) }
        }
    }

    fun onTituloChange(valor: String) = _estado.update {
        it.copy(titulo = valor, errores = it.errores.copy(titulo = null))
    }

    fun onDescripcionChange(valor: String) = _estado.update {
        it.copy(descripcion = valor, errores = it.errores.copy(descripcion = null))
    }

    fun onCategoriaChange(valor: Categoria) = _estado.update {
        it.copy(categoria = valor, errores = it.errores.copy(categoria = null))
    }

    fun onServicioChange(valor: String) = _estado.update {
        it.copy(servicio = valor, errores = it.errores.copy(servicio = null))
    }

    fun onActivoChange(valor: String) = _estado.update { it.copy(activo = valor) }

    fun agregarEvidencia(evidencia: EvidenciaPendiente) = _estado.update {
        it.copy(evidencias = it.evidencias + evidencia, mensaje = "Evidencia agregada: ${evidencia.nombreArchivo}")
    }

    fun quitarEvidencia(evidencia: EvidenciaPendiente) = _estado.update {
        it.copy(evidencias = it.evidencias - evidencia)
    }

    fun mostrarMensaje(mensaje: String) = _estado.update { it.copy(mensaje = mensaje) }

    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    // REGLAS DEL NEGOCIO
    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val errores = NuevaSolicitudErrores(
            titulo = when {
                actual.titulo.isBlank() -> "Ingresa un título"
                actual.titulo.trim().length < 5 -> "El título debe tener al menos 5 caracteres"
                actual.titulo.length > LARGO_MAX_TITULO -> "Máximo $LARGO_MAX_TITULO caracteres"
                else -> null
            },
            descripcion = when {
                actual.descripcion.isBlank() -> "La descripción es obligatoria"
                actual.descripcion.trim().length < 10 -> "Agrega un poco más de detalle (mínimo 10 caracteres)"
                else -> null
            },
            categoria = if (actual.categoria == null) "Selecciona una categoría" else null,
            servicio = if (actual.servicio.isBlank()) "Selecciona el servicio relacionado" else null
        )
        _estado.update { it.copy(errores = errores) }
        return listOf(errores.titulo, errores.descripcion, errores.categoria, errores.servicio).all { it == null }
    }

    fun guardarBorrador() {
        val actual = _estado.value
        borradores.guardar(
            usuario.id,
            Borrador(actual.titulo, actual.descripcion, actual.categoria, actual.servicio, actual.activo)
        )
        _estado.update {
            it.copy(mensaje = if (it.evidencias.isEmpty()) "Borrador guardado" else "Borrador guardado (sin las evidencias)")
        }
    }

    fun enviar() {
        if (!validarFormulario() || _estado.value.enviando) return
        val actual = _estado.value
        _estado.update { it.copy(enviando = true) }
        viewModelScope.launch {
            repositorio.crearSolicitud(
                usuario = usuario,
                titulo = actual.titulo,
                descripcion = actual.descripcion,
                categoria = actual.categoria!!,
                servicioRelacionado = actual.servicio,
                activoRelacionado = actual.activo
            ).onSuccess { creada ->
                // Las evidencias se adjuntan una vez que la solicitud ya tiene id
                val fallidas = actual.evidencias.filter { ev ->
                    repositorio.adjuntarEvidencia(
                        usuario, creada.id, ev.nombreArchivo, ev.tipoMime, ev.tamanoBytes, ev.uri
                    ).isFailure
                }
                borradores.borrar(usuario.id)
                TextosAviso.para(AccionSolicitud.CREAR, creada)?.let { (t, m) -> avisos.avisar(t, m) }
                _estado.update {
                    it.copy(
                        enviando = false,
                        creada = creada,
                        mensaje = if (fallidas.isEmpty()) null else "No se pudieron adjuntar ${fallidas.size} evidencias"
                    )
                }
            }.onFailure { error ->
                _estado.update { it.copy(enviando = false, mensaje = error.message ?: "No se pudo enviar la solicitud") }
            }
        }
    }

    companion object {
        const val LARGO_MAX_TITULO = 80
        const val OTRO_SERVICIO = "Otro"
    }
}
