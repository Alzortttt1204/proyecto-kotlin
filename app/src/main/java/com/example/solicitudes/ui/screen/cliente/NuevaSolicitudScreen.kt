package com.example.solicitudes.ui.screen.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.solicitudes.model.Categoria
import com.example.solicitudes.model.MensajeWhatsApp
import com.example.solicitudes.model.ReglasEvidencia
import com.example.solicitudes.ui.components.BarraSuperior
import com.example.solicitudes.ui.components.EvidenciaItem
import com.example.solicitudes.ui.components.SelectorEvidencia
import com.example.solicitudes.ui.utils.ArchivosEvidencia
import com.example.solicitudes.ui.utils.CompartirWhatsApp
import com.example.solicitudes.viewmodel.AppViewModelProvider
import com.example.solicitudes.viewmodel.NuevaSolicitudViewModel

/**
 * Formulario para registrar una solicitud (tarjetas A3, A4 y A5).
 * Al enviarla muestra el código único y permite compartirlo por WhatsApp.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NuevaSolicitudScreen(
    onVolver: () -> Unit,
    onVerSolicitud: (Long) -> Unit,
    viewModel: NuevaSolicitudViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Nueva solicitud", onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = estado.titulo,
                    onValueChange = viewModel::onTituloChange,
                    label = { Text("Título") },
                    singleLine = true,
                    isError = estado.errores.titulo != null,
                    supportingText = {
                        Text(
                            estado.errores.titulo ?: "${estado.titulo.length}/${NuevaSolicitudViewModel.LARGO_MAX_TITULO}",
                            color = if (estado.errores.titulo != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.descripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    label = { Text("Describe tu necesidad") },
                    minLines = 4,
                    isError = estado.errores.descripcion != null,
                    supportingText = { estado.errores.descripcion?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Seccion("Categoría", estado.errores.categoria)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Categoria.entries.forEach { c ->
                        FilterChip(
                            selected = estado.categoria == c,
                            onClick = { viewModel.onCategoriaChange(c) },
                            label = { Text(c.etiqueta) }
                        )
                    }
                }

                Seccion("Servicio relacionado", estado.errores.servicio)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    estado.serviciosDelCliente.forEach { s ->
                        FilterChip(
                            selected = estado.servicio == s,
                            onClick = { viewModel.onServicioChange(s) },
                            label = { Text(s) }
                        )
                    }
                }

                OutlinedTextField(
                    value = estado.activo,
                    onValueChange = viewModel::onActivoChange,
                    label = { Text("Sitio web o activo relacionado (opcional)") },
                    placeholder = { Text("www.ejemplo.demo.test") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Seccion("Evidencias (opcional)", null)
                Text(
                    "Solo archivos de prueba: ${ReglasEvidencia.tiposPermitidos.values.joinToString(", ")}, máximo 5 MB. " +
                        "No adjuntes datos personales ni capturas de conversaciones reales.",
                    style = MaterialTheme.typography.bodySmall
                )
                estado.evidencias.forEach { ev ->
                    EvidenciaItem(
                        nombre = ev.nombreArchivo,
                        tipoMime = ev.tipoMime,
                        uri = ev.uri,
                        detalle = ArchivosEvidencia.tamanoLegible(ev.tamanoBytes),
                        onAbrir = { ArchivosEvidencia.abrir(context, ev.uri, ev.tipoMime) },
                        onQuitar = { viewModel.quitarEvidencia(ev) }
                    )
                }
                SelectorEvidencia(onEvidencia = viewModel::agregarEvidencia, onError = viewModel::mostrarMensaje)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = viewModel::guardarBorrador, modifier = Modifier.weight(1f)) {
                        Text("Guardar borrador")
                    }
                    Button(
                        onClick = viewModel::enviar,
                        enabled = !estado.enviando,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (estado.enviando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Enviar solicitud")
                    }
                }
            }
        }
    }

    // Confirmación con el código de seguimiento (criterios 4, 7 y 8)
    estado.creada?.let { creada ->
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) },
            title = { Text("Solicitud enviada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tu código de seguimiento es:")
                    Text(creada.codigo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Estado: ${creada.estado.etiqueta}. Puedes compartir la referencia por WhatsApp.")
                }
            },
            confirmButton = {
                TextButton(onClick = { CompartirWhatsApp.compartir(context, MensajeWhatsApp.construir(creada)) }) {
                    Text("Compartir por WhatsApp")
                }
            },
            dismissButton = {
                TextButton(onClick = { onVerSolicitud(creada.id) }) { Text("Ver solicitud") }
            }
        )
    }
}

@Composable
private fun Seccion(titulo: String, error: String?) {
    Column {
        Text(titulo, style = MaterialTheme.typography.titleSmall)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
}
