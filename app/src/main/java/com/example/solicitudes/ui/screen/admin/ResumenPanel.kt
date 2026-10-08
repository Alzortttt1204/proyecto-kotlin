package com.example.solicitudes.ui.screen.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.ui.components.EstadoChip
import com.example.solicitudes.ui.theme.coloresDe
import com.example.solicitudes.ui.utils.LocalAnchoVentana
import com.example.solicitudes.ui.utils.esPantallaAncha
import com.example.solicitudes.viewmodel.AppViewModelProvider
import com.example.solicitudes.viewmodel.ResumenViewModel

/**
 * Panel resumen del administrador (tarjeta B5, criterios 29 y 30).
 * Las cantidades por estado vienen de una consulta GROUP BY en Room, por eso
 * coinciden con los registros guardados. En pantallas anchas usa más columnas (guía 9).
 */
@Composable
fun ResumenPanel(
    onVerEstado: (EstadoSolicitud) -> Unit,
    onVerAtrasadas: () -> Unit,
    onVerResponsable: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResumenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val resumen = estado.resumen ?: return
    val columnas = if (esPantallaAncha(LocalAnchoVentana.current)) 4 else 2
    val ancho = GridItemSpan(columnas)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item(span = { ancho }) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Indicador("Total", resumen.total.toString(), Modifier.weight(1f))
                Indicador(
                    "Atrasadas", resumen.atrasadas.toString(), Modifier.weight(1f),
                    destacado = resumen.atrasadas > 0, onClick = onVerAtrasadas
                )
            }
        }

        item(span = { ancho }) { Titulo("Por estado") }
        items(EstadoSolicitud.entries) { e ->
            val cantidad = estado.conteoBaseDeDatos[e] ?: 0
            Card(onClick = { onVerEstado(e) }) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoChip(e)
                    Text(cantidad.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        item(span = { ancho }) { Titulo("Por prioridad") }
        item(span = { ancho }) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (Prioridad.entries.reversed() + listOf<Prioridad?>(null)).forEach { p ->
                        FilaCantidad(
                            texto = p?.etiqueta ?: "Sin prioridad",
                            cantidad = resumen.porPrioridad[p] ?: 0,
                            total = resumen.total,
                            color = p?.let { coloresDe(it).contenido } ?: MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        item(span = { ancho }) { Titulo("Carga vigente por responsable") }
        item(span = { ancho }) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val vigentes = resumen.porResponsable.values.sum()
                    resumen.porResponsable.entries
                        .sortedByDescending { it.value }
                        .forEach { (id, cantidad) ->
                            FilaCantidad(
                                texto = id?.let { estado.nombresUsuarios[it] } ?: "Sin asignar",
                                cantidad = cantidad,
                                total = vigentes,
                                color = MaterialTheme.colorScheme.primary,
                                onClick = id?.let { { onVerResponsable(it) } }
                            )
                        }
                    if (resumen.porResponsable.isEmpty()) Text("No hay solicitudes vigentes.")
                }
            }
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun Indicador(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    destacado: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val colores = if (destacado) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    } else CardDefaults.cardColors()
    val contenido: @Composable () -> Unit = {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge)
            Text(valor, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        }
    }
    if (onClick != null) Card(onClick = onClick, modifier = modifier, colors = colores) { contenido() }
    else Card(modifier = modifier, colors = colores) { contenido() }
}

// Fila con nombre, cantidad y una barra proporcional al total
@Composable
private fun FilaCantidad(texto: String, cantidad: Int, total: Int, color: Color, onClick: (() -> Unit)? = null) {
    val fila = Modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    Column(modifier = fila, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(texto, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(cantidad.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else cantidad.toFloat() / total },
            color = color,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

