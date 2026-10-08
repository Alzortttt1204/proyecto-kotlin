package com.example.solicitudes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.Prioridad

// Par de colores para chips y etiquetas: fondo y texto con contraste suficiente
data class ColoresEtiqueta(val fondo: Color, val contenido: Color)

// Diferenciación visual de los estados (requisito 3.3 del caso).
// Cada estado tiene su color en modo claro y en modo oscuro.
@Composable
fun coloresDe(estado: EstadoSolicitud): ColoresEtiqueta {
    val oscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when (estado) {
        EstadoSolicitud.RECIBIDA ->
            if (oscuro) ColoresEtiqueta(Color(0xFF3A4048), Color(0xFFDDE3EA))
            else ColoresEtiqueta(Color(0xFFDDE3EA), Color(0xFF161C22))
        EstadoSolicitud.EN_REVISION ->
            if (oscuro) ColoresEtiqueta(Color(0xFF4F378B), Color(0xFFEADDFF))
            else ColoresEtiqueta(Color(0xFFEADDFF), Color(0xFF21005D))
        EstadoSolicitud.PENDIENTE_INFORMACION ->
            if (oscuro) ColoresEtiqueta(Color(0xFF5A4300), Color(0xFFFFE08A))
            else ColoresEtiqueta(Color(0xFFFFE08A), Color(0xFF3E2E00))
        EstadoSolicitud.ASIGNADA ->
            if (oscuro) ColoresEtiqueta(Color(0xFF004D60), Color(0xFFBFEBFF))
            else ColoresEtiqueta(Color(0xFFBFEBFF), Color(0xFF001F28))
        EstadoSolicitud.EN_PROCESO ->
            if (oscuro) ColoresEtiqueta(Color(0xFF00458F), Color(0xFFD7E3FF))
            else ColoresEtiqueta(Color(0xFFD7E3FF), Color(0xFF001B3E))
        EstadoSolicitud.RESUELTA ->
            if (oscuro) ColoresEtiqueta(Color(0xFF1E5127), Color(0xFFB8F0BC))
            else ColoresEtiqueta(Color(0xFFB8F0BC), Color(0xFF002107))
        EstadoSolicitud.CERRADA ->
            if (oscuro) ColoresEtiqueta(Color(0xFF2E2F33), Color(0xFFC6C6CA))
            else ColoresEtiqueta(Color(0xFFE2E2E6), Color(0xFF1A1C1E))
        EstadoSolicitud.CANCELADA ->
            if (oscuro) ColoresEtiqueta(Color(0xFF93000A), Color(0xFFFFDAD6))
            else ColoresEtiqueta(Color(0xFFFFDAD6), Color(0xFF410002))
    }
}

@Composable
fun coloresDe(prioridad: Prioridad): ColoresEtiqueta {
    val oscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when (prioridad) {
        Prioridad.BAJA ->
            if (oscuro) ColoresEtiqueta(Color(0xFF3A4048), Color(0xFFDDE3EA))
            else ColoresEtiqueta(Color(0xFFDDE3EA), Color(0xFF161C22))
        Prioridad.MEDIA ->
            if (oscuro) ColoresEtiqueta(Color(0xFF00458F), Color(0xFFD7E3FF))
            else ColoresEtiqueta(Color(0xFFD7E3FF), Color(0xFF001B3E))
        Prioridad.ALTA ->
            if (oscuro) ColoresEtiqueta(Color(0xFF6E3900), Color(0xFFFFDCC2))
            else ColoresEtiqueta(Color(0xFFFFDCC2), Color(0xFF2E1500))
        Prioridad.URGENTE ->
            if (oscuro) ColoresEtiqueta(Color(0xFF93000A), Color(0xFFFFDAD6))
            else ColoresEtiqueta(Color(0xFFFFDAD6), Color(0xFF410002))
    }
}
