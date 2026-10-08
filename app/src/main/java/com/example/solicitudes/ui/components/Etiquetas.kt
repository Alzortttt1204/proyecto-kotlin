package com.example.solicitudes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.ui.theme.AppSolicitudesTheme
import com.example.solicitudes.ui.theme.coloresDe

// Ícono de cada estado: así el estado no se distingue solo por el color (accesibilidad)
fun EstadoSolicitud.icono(): ImageVector = when (this) {
    EstadoSolicitud.RECIBIDA -> Icons.Outlined.Inbox
    EstadoSolicitud.EN_REVISION -> Icons.AutoMirrored.Outlined.Assignment
    EstadoSolicitud.PENDIENTE_INFORMACION -> Icons.AutoMirrored.Outlined.HelpOutline
    EstadoSolicitud.ASIGNADA -> Icons.Outlined.AssignmentInd
    EstadoSolicitud.EN_PROCESO -> Icons.Outlined.Build
    EstadoSolicitud.RESUELTA -> Icons.Outlined.CheckCircle
    EstadoSolicitud.CERRADA -> Icons.Outlined.Lock
    EstadoSolicitud.CANCELADA -> Icons.Outlined.Block
}

// Etiqueta base reutilizada por los chips de estado, prioridad y "atrasada"
@Composable
private fun Etiqueta(
    texto: String,
    icono: ImageVector,
    fondo: Color,
    contenido: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = fondo,
        contentColor = contenido
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(texto, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun EstadoChip(estado: EstadoSolicitud, modifier: Modifier = Modifier) {
    val colores = coloresDe(estado)
    Etiqueta(estado.etiqueta, estado.icono(), colores.fondo, colores.contenido, modifier)
}

// Si la solicitud aún no tiene prioridad (el admin no la definió) se muestra "Sin prioridad"
@Composable
fun PrioridadChip(prioridad: Prioridad?, modifier: Modifier = Modifier) {
    if (prioridad == null) {
        Etiqueta(
            "Sin prioridad", Icons.Outlined.Flag,
            MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, modifier
        )
    } else {
        val colores = coloresDe(prioridad)
        Etiqueta(prioridad.etiqueta, Icons.Outlined.Flag, colores.fondo, colores.contenido, modifier)
    }
}

@Composable
fun AtrasadaChip(modifier: Modifier = Modifier) {
    Etiqueta(
        "Atrasada", Icons.Outlined.Schedule,
        MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun EtiquetasPreview() {
    AppSolicitudesTheme {
        FlowRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EstadoSolicitud.entries.forEach { EstadoChip(it) }
            Prioridad.entries.forEach { PrioridadChip(it) }
            PrioridadChip(null)
            AtrasadaChip()
        }
    }
}
