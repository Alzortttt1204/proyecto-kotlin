package com.example.solicitudes.ui.screen.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solicitudes.model.AccionSolicitud
import com.example.solicitudes.model.DetalleSolicitudUiState
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.MensajeWhatsApp
import com.example.solicitudes.model.ReglasSolicitud
import com.example.solicitudes.model.Rol
import com.example.solicitudes.model.Solicitud
import com.example.solicitudes.ui.components.AccionDialog
import com.example.solicitudes.ui.components.AtrasadaChip
import com.example.solicitudes.ui.components.BarraSuperior
import com.example.solicitudes.ui.components.EstadoChip
import com.example.solicitudes.ui.components.EvidenciaItem
import com.example.solicitudes.ui.components.HistorialTimeline
import com.example.solicitudes.ui.components.PrioridadChip
import com.example.solicitudes.ui.components.SelectorEvidencia
import com.example.solicitudes.ui.components.textoBoton
import com.example.solicitudes.ui.utils.ArchivosEvidencia
import com.example.solicitudes.ui.utils.CompartirWhatsApp
import com.example.solicitudes.ui.utils.Formatos
import com.example.solicitudes.viewmodel.AppViewModelProvider
import com.example.solicitudes.viewmodel.DetalleSolicitudViewModel

// Acciones destructivas o de cierre: se muestran con botón de contorno para diferenciarlas
private val accionesSecundarias = setOf(AccionSolicitud.CANCELAR, AccionSolicitud.REABRIR, AccionSolicitud.COMENTAR)

/**
 * Detalle y seguimiento de una solicitud, para los tres perfiles.
 * Muestra solo las acciones que el perfil puede ejecutar en el estado actual.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetalleSolicitudScreen(
    onVolver: () -> Unit,
    viewModel: DetalleSolicitudViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val solicitud = estado.solicitud

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = solicitud?.codigo ?: "Solicitud", onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                estado.cargando -> CircularProgressIndicator(Modifier.padding(32.dp))
                solicitud == null -> Text(
                    "La solicitud no existe o no tienes permiso para verla.",
                    modifier = Modifier.padding(32.dp)
                )
                else -> Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Encabezado(solicitud, estado)

                    // El cliente puede compartir la referencia por WhatsApp (tarjeta A5)
                    if (estado.usuario?.rol == Rol.CLIENTE) {
                        OutlinedButton(onClick = {
                            CompartirWhatsApp.compartir(context, MensajeWhatsApp.construir(solicitud))
                        }) {
                            Icon(Icons.Outlined.Share, contentDescription = null)
                            Text("Compartir por WhatsApp", modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    if (estado.acciones.isNotEmpty()) {
                        Titulo("Acciones")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            estado.acciones.forEach { accion ->
                                if (accion in accionesSecundarias) {
                                    OutlinedButton(onClick = { viewModel.abrirAccion(accion) }) { Text(accion.textoBoton()) }
                                } else {
                                    FilledTonalButton(onClick = { viewModel.abrirAccion(accion) }) { Text(accion.textoBoton()) }
                                }
                            }
                        }
                    }

                    Titulo("Evidencias")
                    if (estado.evidencias.isEmpty()) Text("Sin evidencias adjuntas.", style = MaterialTheme.typography.bodyMedium)
                    estado.evidencias.forEach { ev ->
                        EvidenciaItem(
                            nombre = ev.nombreArchivo,
                            tipoMime = ev.tipoMime,
                            uri = ev.uri,
                            detalle = "${ArchivosEvidencia.tamanoLegible(ev.tamanoBytes)} · " +
                                "${estado.nombresUsuarios[ev.usuarioId] ?: "Usuario"} · ${Formatos.fechaHora(ev.fecha)}",
                            onAbrir = {
                                if (!ArchivosEvidencia.abrir(context, ev.uri, ev.tipoMime)) {
                                    viewModel.mostrarMensaje("No hay una app para abrir este archivo")
                                }
                            }
                        )
                    }
                    if (estado.puedeAdjuntar) {
                        SelectorEvidencia(onEvidencia = viewModel::adjuntarEvidencia, onError = viewModel::mostrarMensaje)
                    }

                    HorizontalDivider()
                    Titulo("Historial")
                    HistorialTimeline(estado.historial, estado.nombresUsuarios)
                }
            }
        }
    }

    // Diálogo de la acción elegida
    val accion = estado.accionEnCurso
    val usuario = estado.usuario
    if (accion != null && solicitud != null && usuario != null) {
        AccionDialog(
            accion = accion,
            rol = usuario.rol,
            solicitud = solicitud,
            tecnicos = estado.tecnicos,
            procesando = estado.procesando,
            onConfirmar = { texto, interno, responsableId, prioridad, categoria ->
                viewModel.ejecutar(accion, texto, interno, responsableId, prioridad, categoria)
            },
            onCancelar = viewModel::cerrarDialogo
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Encabezado(solicitud: Solicitud, estado: DetalleSolicitudUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(solicitud.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EstadoChip(solicitud.estado)
                PrioridadChip(solicitud.prioridad)
                if (ReglasSolicitud.estaAtrasada(solicitud, estado.ahora)) AtrasadaChip()
            }
            Text(solicitud.descripcion, style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider()
            Dato("Cliente", estado.cliente?.let { "${it.empresa} · ${it.nombreContacto}" })
            Dato("Categoría", solicitud.categoria.etiqueta)
            Dato("Servicio", solicitud.servicioRelacionado)
            Dato("Sitio o activo", solicitud.activoRelacionado)
            Dato("Responsable", solicitud.responsableId?.let { estado.nombresUsuarios[it] } ?: "Sin asignar")
            Dato("Creada", Formatos.fechaHora(solicitud.fechaCreacion))
            Dato("Última actualización", Formatos.fechaHora(solicitud.fechaActualizacion))
            if (estado.usuario?.rol != Rol.CLIENTE && !solicitud.estado.esFinal && solicitud.estado != EstadoSolicitud.RESUELTA) {
                Dato("Fecha compromiso", Formatos.fechaHora(ReglasSolicitud.fechaCompromiso(solicitud)))
            }
            solicitud.solucion?.let { Dato("Solución", it) }
            solicitud.fechaResolucion?.let { Dato("Resuelta el", Formatos.fechaHora(it)) }
            if (solicitud.confirmacionCierre) Dato("Cierre", "Confirmado por el cliente")
            solicitud.motivoCancelacion?.let { Dato("Motivo de cancelación", it) }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String?) {
    if (valor.isNullOrBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$etiqueta:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium)
}
