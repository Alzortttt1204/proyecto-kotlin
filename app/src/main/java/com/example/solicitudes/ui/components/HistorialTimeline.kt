package com.example.solicitudes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.solicitudes.model.AccionSolicitud
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.EventoHistorial
import com.example.solicitudes.model.Rol
import com.example.solicitudes.ui.theme.AppSolicitudesTheme
import com.example.solicitudes.ui.theme.coloresDe
import com.example.solicitudes.ui.utils.Formatos

/**
 * Historial cronológico de una solicitud (criterio 11 del caso).
 *
 * Usa Column (no LazyColumn) para poder ponerlo dentro de la pantalla de detalle,
 * que ya tiene su propio scroll. Los eventos deben venir ya filtrados por el
 * repositorio (el cliente no recibe los comentarios internos).
 *
 * @param nombresUsuarios id de usuario -> nombre, para mostrar quién hizo cada acción.
 */
@Composable
fun HistorialTimeline(
    eventos: List<EventoHistorial>,
    nombresUsuarios: Map<Long, String>,
    modifier: Modifier = Modifier
) {
    if (eventos.isEmpty()) {
        Text("Sin movimientos registrados.", modifier = modifier, style = MaterialTheme.typography.bodyMedium)
        return
    }
    Column(modifier = modifier) {
        eventos.forEachIndexed { indice, evento ->
            EventoItem(
                evento = evento,
                nombreUsuario = nombresUsuarios[evento.usuarioId] ?: "Usuario ${evento.usuarioId}",
                esUltimo = indice == eventos.lastIndex
            )
        }
    }
}

@Composable
private fun EventoItem(evento: EventoHistorial, nombreUsuario: String, esUltimo: Boolean) {
    val colorPunto = coloresDe(evento.estadoNuevo).contenido
    // IntrinsicSize.Min permite que la línea vertical mida lo mismo que el texto del evento
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Punto y línea de la línea de tiempo
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .background(colorPunto, CircleShape)
            )
            if (!esUltimo) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = evento.accion.etiqueta,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            // Cambio de estado, solo si hubo
            if (evento.estadoAnterior != null && evento.estadoAnterior != evento.estadoNuevo) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EstadoChip(evento.estadoAnterior)
                    Text("→", style = MaterialTheme.typography.bodyMedium)
                    EstadoChip(evento.estadoNuevo)
                }
            } else if (evento.estadoAnterior == null) {
                EstadoChip(evento.estadoNuevo)
            }
            evento.comentario?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            if (evento.interno) {
                Text(
                    "Comentario interno (no visible para el cliente)",
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Text(
                text = "$nombreUsuario · ${evento.rol.etiqueta} · ${Formatos.fechaHora(evento.fecha)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistorialTimelinePreview() {
    val t = System.currentTimeMillis() - 5 * 3_600_000L
    val eventos = listOf(
        EventoHistorial(1, 5, null, EstadoSolicitud.RECIBIDA, AccionSolicitud.CREAR, rol = Rol.CLIENTE, usuarioId = 2, fecha = t),
        EventoHistorial(
            2, 5, EstadoSolicitud.RECIBIDA, EstadoSolicitud.ASIGNADA, AccionSolicitud.ASIGNAR,
            comentario = "Responsable: Esteban Técnico", rol = Rol.ADMIN, usuarioId = 4, fecha = t + 1_800_000
        ),
        EventoHistorial(
            3, 5, EstadoSolicitud.ASIGNADA, EstadoSolicitud.EN_PROCESO, AccionSolicitud.INICIAR_TRABAJO,
            rol = Rol.TECNICO, usuarioId = 5, fecha = t + 3_600_000
        ),
        EventoHistorial(
            4, 5, EstadoSolicitud.EN_PROCESO, EstadoSolicitud.EN_PROCESO, AccionSolicitud.COMENTAR,
            comentario = "Falta validar el dominio con el proveedor.", interno = true,
            rol = Rol.TECNICO, usuarioId = 5, fecha = t + 5_400_000
        )
    )
    AppSolicitudesTheme {
        HistorialTimeline(
            eventos = eventos,
            nombresUsuarios = mapOf(2L to "Bruno Ejemplo", 4L to "Daniela Coordinadora", 5L to "Esteban Técnico"),
            modifier = Modifier.padding(16.dp)
        )
    }
}
