package com.example.solicitudes.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solicitudes.model.Cliente
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.FiltrosSolicitud
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.model.Usuario

// Buscador por código o título (criterio: búsqueda y filtrado de solicitudes)
@Composable
fun Buscador(texto: String, onTexto: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTexto,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Buscar por código o título") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onTexto("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Borrar búsqueda")
                }
            }
        },
        singleLine = true
    )
}

/**
 * Fila de filtros con menús desplegables. Cada perfil muestra solo los filtros que le sirven:
 * el admin todos (estado, prioridad, cliente, responsable, atrasadas); el técnico y el cliente menos.
 */
@Composable
fun BarraFiltros(
    filtros: FiltrosSolicitud,
    onEstado: (EstadoSolicitud?) -> Unit,
    onPrioridad: ((Prioridad?) -> Unit)? = null,
    clientes: Map<Long, Cliente> = emptyMap(),
    onCliente: ((Long?) -> Unit)? = null,
    tecnicos: List<Usuario> = emptyList(),
    onResponsable: ((Long?) -> Unit)? = null,
    onSoloAtrasadas: ((Boolean) -> Unit)? = null,
    onLimpiar: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroMenu(
                etiqueta = "Estado",
                seleccion = filtros.estado?.etiqueta,
                opciones = EstadoSolicitud.entries.map { it.etiqueta to it },
                onSeleccion = onEstado
            )
            onPrioridad?.let {
                FiltroMenu(
                    etiqueta = "Prioridad",
                    seleccion = filtros.prioridad?.etiqueta,
                    opciones = Prioridad.entries.map { p -> p.etiqueta to p },
                    onSeleccion = it
                )
            }
            onCliente?.let {
                FiltroMenu(
                    etiqueta = "Cliente",
                    seleccion = filtros.clienteId?.let { id -> clientes[id]?.empresa },
                    opciones = clientes.values.sortedBy { c -> c.empresa }.map { c -> c.empresa to c.id },
                    onSeleccion = it
                )
            }
            onResponsable?.let {
                FiltroMenu(
                    etiqueta = "Responsable",
                    seleccion = filtros.responsableId?.let { id -> tecnicos.firstOrNull { t -> t.id == id }?.nombre },
                    opciones = tecnicos.map { t -> t.nombre to t.id },
                    onSeleccion = it
                )
            }
            onSoloAtrasadas?.let {
                FilterChip(
                    selected = filtros.soloAtrasadas,
                    onClick = { it(!filtros.soloAtrasadas) },
                    label = { Text("Atrasadas") }
                )
            }
        }
        if (filtros.hayFiltrosActivos) {
            TextButton(onClick = onLimpiar, modifier = Modifier.padding(start = 8.dp)) {
                Text("Quitar filtros")
            }
        }
    }
}

// Chip que abre un menú con las opciones; la primera opción ("Todos") quita el filtro
@Composable
private fun <T> FiltroMenu(
    etiqueta: String,
    seleccion: String?,
    opciones: List<Pair<String, T>>,
    onSeleccion: (T?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = seleccion != null,
            onClick = { abierto = true },
            label = { Text(seleccion?.let { "$etiqueta: $it" } ?: etiqueta) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text("Todos", style = MaterialTheme.typography.bodyLarge) },
                onClick = { onSeleccion(null); abierto = false }
            )
            opciones.forEach { (texto, valor) ->
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = { onSeleccion(valor); abierto = false }
                )
            }
        }
    }
}
