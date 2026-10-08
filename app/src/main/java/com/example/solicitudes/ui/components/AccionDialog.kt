package com.example.solicitudes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.solicitudes.model.AccionSolicitud
import com.example.solicitudes.model.Categoria
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.model.Rol
import com.example.solicitudes.model.Solicitud
import com.example.solicitudes.model.Usuario

// Texto del botón que abre cada acción en la pantalla de detalle
fun AccionSolicitud.textoBoton(): String = when (this) {
    AccionSolicitud.REVISAR -> "Iniciar revisión"
    AccionSolicitud.CLASIFICAR -> "Clasificar"
    AccionSolicitud.CAMBIAR_PRIORIDAD -> "Definir prioridad"
    AccionSolicitud.SOLICITAR_INFORMACION -> "Pedir información"
    AccionSolicitud.RESPONDER_INFORMACION -> "Responder información"
    AccionSolicitud.ASIGNAR -> "Asignar técnico"
    AccionSolicitud.REASIGNAR -> "Reasignar"
    AccionSolicitud.INICIAR_TRABAJO -> "Iniciar trabajo"
    AccionSolicitud.REGISTRAR_AVANCE -> "Registrar avance"
    AccionSolicitud.COMENTAR -> "Comentar"
    AccionSolicitud.ADJUNTAR_EVIDENCIA -> "Adjuntar evidencia"
    AccionSolicitud.MARCAR_RESUELTA -> "Registrar solución"
    AccionSolicitud.CONFIRMAR_RESOLUCION -> "Confirmar resolución"
    AccionSolicitud.CERRAR -> "Cerrar solicitud"
    AccionSolicitud.CANCELAR -> "Cancelar solicitud"
    AccionSolicitud.REABRIR -> "Solicitar reapertura"
    AccionSolicitud.CREAR -> "Crear"
}

// Etiqueta del campo de texto según la acción
private fun AccionSolicitud.etiquetaTexto(): String = when (this) {
    AccionSolicitud.SOLICITAR_INFORMACION -> "¿Qué información necesitas?"
    AccionSolicitud.RESPONDER_INFORMACION -> "Tu respuesta"
    AccionSolicitud.REGISTRAR_AVANCE -> "Avance realizado"
    AccionSolicitud.MARCAR_RESUELTA -> "Solución aplicada"
    AccionSolicitud.CANCELAR -> "Motivo de la cancelación"
    AccionSolicitud.REABRIR -> "¿Por qué necesitas reabrirla?"
    else -> "Comentario"
}

// Explicación que se muestra en las acciones que solo requieren confirmación
private fun AccionSolicitud.confirmacion(): String = when (this) {
    AccionSolicitud.REVISAR -> "La solicitud pasará a \"En revisión\"."
    AccionSolicitud.INICIAR_TRABAJO -> "La solicitud pasará a \"En proceso\"."
    AccionSolicitud.CONFIRMAR_RESOLUCION -> "Confirmas que la solución te deja conforme. La solicitud quedará cerrada."
    AccionSolicitud.CERRAR -> "La solicitud quedará cerrada."
    else -> ""
}

/**
 * Diálogo único para todas las acciones del flujo. Muestra solo los campos que la acción necesita:
 * texto (comentario, motivo o solución), técnico, prioridad, categoría o la opción "interno".
 */
@Composable
fun AccionDialog(
    accion: AccionSolicitud,
    rol: Rol,
    solicitud: Solicitud,
    tecnicos: List<Usuario>,
    procesando: Boolean,
    onConfirmar: (texto: String?, interno: Boolean, responsableId: Long?, prioridad: Prioridad?, categoria: Categoria?) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by rememberSaveable { mutableStateOf("") }
    var interno by rememberSaveable { mutableStateOf(false) }
    var responsableId by rememberSaveable { mutableStateOf(solicitud.responsableId) }
    var prioridad by rememberSaveable { mutableStateOf(solicitud.prioridad) }
    var categoria by rememberSaveable { mutableStateOf(solicitud.categoria) }

    val pideTecnico = accion == AccionSolicitud.ASIGNAR || accion == AccionSolicitud.REASIGNAR
    // Solo comentarios y avances pueden ser internos; una petición de información debe verla el cliente
    val permiteInterno = rol != Rol.CLIENTE &&
        (accion == AccionSolicitud.COMENTAR || accion == AccionSolicitud.REGISTRAR_AVANCE)

    val valido = when {
        accion.requiereTexto && texto.isBlank() -> false
        pideTecnico -> responsableId != null && (accion != AccionSolicitud.REASIGNAR || responsableId != solicitud.responsableId)
        accion == AccionSolicitud.CAMBIAR_PRIORIDAD -> prioridad != null
        else -> true
    }

    AlertDialog(
        onDismissRequest = { if (!procesando) onCancelar() },
        title = { Text(accion.textoBoton()) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("${solicitud.codigo} · ${solicitud.titulo}", style = MaterialTheme.typography.bodyMedium)
                accion.confirmacion().takeIf { it.isNotEmpty() }?.let { Text(it) }

                when {
                    pideTecnico -> {
                        if (tecnicos.isEmpty()) Text("No hay técnicos activos.")
                        tecnicos.forEach { t ->
                            OpcionRadio(t.nombre, responsableId == t.id) { responsableId = t.id }
                        }
                    }
                    accion == AccionSolicitud.CAMBIAR_PRIORIDAD ->
                        Prioridad.entries.forEach { p -> OpcionRadio(p.etiqueta, prioridad == p) { prioridad = p } }
                    accion == AccionSolicitud.CLASIFICAR ->
                        Categoria.entries.forEach { c -> OpcionRadio(c.etiqueta, categoria == c) { categoria = c } }
                }

                if (accion.requiereTexto || pideTecnico) {
                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = it },
                        label = { Text(if (accion.requiereTexto) accion.etiquetaTexto() else "Comentario (opcional)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (permiteInterno) {
                    // Toda la fila es tocable (no solo la casilla) para que sea fácil de marcar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(value = interno, role = Role.Checkbox, onValueChange = { interno = it }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = interno, onCheckedChange = null)
                        Text("Comentario interno (el cliente no lo verá)")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido && !procesando,
                onClick = {
                    onConfirmar(
                        texto.ifBlank { null },
                        interno,
                        if (pideTecnico) responsableId else null,
                        if (accion == AccionSolicitud.CAMBIAR_PRIORIDAD) prioridad else null,
                        if (accion == AccionSolicitud.CLASIFICAR) categoria else null
                    )
                }
            ) { Text(if (procesando) "Guardando…" else "Confirmar") }
        },
        dismissButton = {
            TextButton(enabled = !procesando, onClick = onCancelar) { Text("Volver") }
        }
    )
}

@Composable
private fun OpcionRadio(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionado, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionado, onClick = null)
        Text(texto)
    }
}
