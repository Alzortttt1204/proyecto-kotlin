package com.example.solicitudes.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fila de una evidencia: miniatura (si es imagen) o ícono de documento, nombre y detalle.
 * @param onAbrir abre el archivo con la app del teléfono.
 * @param onQuitar si no es null muestra el botón para quitarla (solo antes de enviar).
 */
@Composable
fun EvidenciaItem(
    nombre: String,
    tipoMime: String,
    uri: String,
    detalle: String,
    onAbrir: () -> Unit,
    onQuitar: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAbrir)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Miniatura(uri, tipoMime)
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            onQuitar?.let {
                IconButton(onClick = it) { Icon(Icons.Outlined.Close, contentDescription = "Quitar $nombre") }
            }
        }
    }
}

@Composable
private fun Miniatura(uri: String, tipoMime: String) {
    val context = LocalContext.current
    // La imagen se decodifica reducida y fuera del hilo principal para no trabar la pantalla
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = if (!tipoMime.startsWith("image/")) null else withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use { entrada ->
                    BitmapFactory.decodeStream(entrada, null, BitmapFactory.Options().apply { inSampleSize = 8 })
                }
            }.getOrNull()
        }
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        val imagen = bitmap
        if (imagen != null) {
            Image(imagen.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp))
        } else {
            Icon(
                if (tipoMime.startsWith("image/")) Icons.Outlined.Image else Icons.Outlined.Description,
                contentDescription = null
            )
        }
    }
}
