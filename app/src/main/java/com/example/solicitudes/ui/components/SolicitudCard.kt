package com.example.solicitudes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.solicitudes.model.Categoria
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.model.ReglasSolicitud
import com.example.solicitudes.model.Solicitud
import com.example.solicitudes.ui.theme.AppSolicitudesTheme
import com.example.solicitudes.ui.utils.Formatos

/**
 * Tarjeta de una solicitud para las listas de cliente, administrador y técnico.
 *
 * @param nombreCliente empresa solicitante (útil en las listas del admin y del técnico).
 * @param nombreResponsable técnico asignado (útil en la bandeja del admin).
 * @param ahora hora actual en milisegundos, para marcar si está atrasada.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SolicitudCard(
    solicitud: Solicitud,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    nombreCliente: String? = null,
    nombreResponsable: String? = null,
    ahora: Long = System.currentTimeMillis()
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = solicitud.codigo,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = solicitud.categoria.etiqueta,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = solicitud.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EstadoChip(solicitud.estado)
                PrioridadChip(solicitud.prioridad)
                if (ReglasSolicitud.estaAtrasada(solicitud, ahora)) AtrasadaChip()
            }

            nombreCliente?.let {
                Text("Cliente: $it", style = MaterialTheme.typography.bodyMedium)
            }
            nombreResponsable?.let {
                Text("Responsable: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = "Actualizada: ${Formatos.fechaHora(solicitud.fechaActualizacion)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SolicitudCardPreview() {
    val ahora = System.currentTimeMillis()
    AppSolicitudesTheme {
        SolicitudCard(
            solicitud = Solicitud(
                id = 4,
                codigo = "SOL-2026-0004",
                clienteId = 1,
                creadorId = 1,
                titulo = "El sitio web carga muy lento",
                descripcion = "La página de inicio tarda más de 20 segundos en cargar.",
                categoria = Categoria.INCIDENTE_TECNICO,
                servicioRelacionado = "Sitio web",
                prioridad = Prioridad.URGENTE,
                estado = EstadoSolicitud.ASIGNADA,
                fechaCreacion = ahora - 100 * 3_600_000L
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
            nombreCliente = "Panadería El Trigal (ficticia)",
            nombreResponsable = "Esteban Técnico",
            ahora = ahora
        )
    }
}
