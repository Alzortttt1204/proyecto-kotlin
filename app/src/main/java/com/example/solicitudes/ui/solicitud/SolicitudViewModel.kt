package com.example.solicitudes.ui.solicitud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.solicitudes.data.local.AppDatabase
import com.example.solicitudes.data.local.SolicitudEntity
import com.example.solicitudes.data.repository.SolicitudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class SolicitudViewModel(app: Application) : AndroidViewModel(app) {

    private val repositorio = SolicitudRepository(AppDatabase.getDatabase(app).solicitudDao())

    // Categorías del caso
    val categorias = listOf(
        "Desarrollo web",
        "Mantenimiento web",
        "Incidente técnico",
        "Hosting",
        "Soporte de correo",
        "Marketing digital",
        "Solicitud de cambio",
        "Consulta",
        "Acceso o configuración",
        "Otro requerimiento"
    )

    // Solicitudes guardadas en Room; la lista se actualiza sola al registrar o editar
    val solicitudes: StateFlow<List<SolicitudEntity>> = repositorio.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado interno mutable
    private val _estado = MutableStateFlow(SolicitudUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<SolicitudUiState> = _estado

    // Actualiza el campo título y limpia su error
    fun onTituloChange(valor: String) {
        _estado.update { it.copy(titulo = valor, tituloError = null) }
    }

    // Actualiza el campo descripción y limpia su error
    fun onDescripcionChange(valor: String) {
        _estado.update { it.copy(descripcion = valor, descripcionError = null) }
    }

    // Actualiza la categoría y limpia su error
    fun onCategoriaChange(valor: String) {
        _estado.update { it.copy(categoria = valor, categoriaError = null) }
    }

    // Actualiza el servicio relacionado y limpia su error
    fun onServicioChange(valor: String) {
        _estado.update { it.copy(servicio = valor, servicioError = null) }
    }

    // Guarda la Uri de la foto tomada con la cámara
    fun onEvidenciaChange(uri: String) {
        _estado.update { it.copy(evidenciaUri = uri) }
    }

    // Deja el formulario vacío para registrar una solicitud nueva
    fun nuevaSolicitud() {
        _estado.value = SolicitudUiState()
    }

    // Carga en el formulario los datos de una solicitud guardada para consultarla o editarla
    fun editarSolicitud(solicitud: SolicitudEntity) {
        _estado.value = SolicitudUiState(
            titulo = solicitud.titulo,
            descripcion = solicitud.descripcion,
            categoria = solicitud.categoria,
            servicio = solicitud.servicio,
            evidenciaUri = solicitud.evidenciaUri,
            solicitudEditando = solicitud
        )
    }

    // REGLAS DEL NEGOCIO
    fun validarFormulario(): Boolean {
        val estadoActual = _estado.value

        val tituloError = if (estadoActual.titulo.isBlank()) "El título es obligatorio" else null

        val descripcionError = when {
            estadoActual.descripcion.isBlank() -> "La descripción es obligatoria"
            estadoActual.descripcion.trim().length < MIN_DESCRIPCION ->
                "Debe tener al menos $MIN_DESCRIPCION caracteres"
            else -> null
        }

        val categoriaError = if (estadoActual.categoria.isBlank()) "Selecciona una categoría" else null

        val servicioError = if (estadoActual.servicio.isBlank()) "El servicio relacionado es obligatorio" else null

        _estado.update {
            it.copy(
                tituloError = tituloError,
                descripcionError = descripcionError,
                categoriaError = categoriaError,
                servicioError = servicioError
            )
        }

        return listOfNotNull(tituloError, descripcionError, categoriaError, servicioError).isEmpty()
    }

    // Guarda en Room: inserta si es nueva o actualiza si se está editando
    fun guardar() {
        val a = _estado.value
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val editando = a.solicitudEditando
            if (editando == null) {
                repositorio.insertar(
                    SolicitudEntity(
                        codigo = "SOL-" + UUID.randomUUID().toString().take(8).uppercase(),
                        titulo = a.titulo.trim(),
                        descripcion = a.descripcion.trim(),
                        categoria = a.categoria,
                        servicio = a.servicio.trim(),
                        estado = "Recibida",
                        fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        evidenciaUri = a.evidenciaUri
                    )
                )
            } else {
                repositorio.actualizar(
                    editando.copy(
                        titulo = a.titulo.trim(),
                        descripcion = a.descripcion.trim(),
                        categoria = a.categoria,
                        servicio = a.servicio.trim(),
                        evidenciaUri = a.evidenciaUri
                    )
                )
            }
            _estado.update { it.copy(guardando = false, guardadoExitosamente = true) }
        }
    }

    companion object {
        const val MIN_DESCRIPCION = 10
    }
}
