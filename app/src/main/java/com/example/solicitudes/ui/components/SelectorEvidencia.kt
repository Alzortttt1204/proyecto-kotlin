package com.example.solicitudes.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.solicitudes.model.EvidenciaPendiente
import com.example.solicitudes.ui.utils.ArchivosEvidencia
import kotlinx.coroutines.launch
import java.io.File

/**
 * Botones para adjuntar una evidencia desde archivos/galería o desde la cámara (guía 13).
 * Usa rememberLauncherForActivityResult con:
 *  - OpenDocument: elige una imagen o PDF (solo los tipos permitidos)
 *  - TakePicture: toma una foto y la guarda en la Uri entregada por FileProvider
 * Si el usuario cancela, no pasa nada (no se cae la app).
 */
@Composable
fun SelectorEvidencia(
    onEvidencia: (EvidenciaPendiente) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Ruta de la foto en curso; se guarda para sobrevivir si Android recrea la pantalla
    var rutaFoto by rememberSaveable { mutableStateOf<String?>(null) }

    val selectorArchivo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                ArchivosEvidencia.importar(context, uri)
                    .onSuccess(onEvidencia)
                    .onFailure { onError(it.message ?: "No se pudo adjuntar el archivo") }
            }
        }
    }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val archivo = rutaFoto?.let { File(it) }
        if (exito && archivo != null) {
            ArchivosEvidencia.desdeFoto(archivo, ArchivosEvidencia.uriDe(context, archivo))
                .onSuccess(onEvidencia)
                .onFailure { onError(it.message ?: "No se pudo guardar la foto") }
        } else {
            archivo?.delete()   // foto cancelada: se borra el archivo vacío
        }
        rutaFoto = null
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { selectorArchivo.launch(ArchivosEvidencia.tiposSelector) }) {
            Icon(Icons.Outlined.AttachFile, contentDescription = null)
            Text("Archivo o imagen", modifier = Modifier.padding(start = 4.dp))
        }
        OutlinedButton(onClick = {
            val (archivo, uri) = ArchivosEvidencia.nuevaFoto(context)
            rutaFoto = archivo.absolutePath
            camara.launch(uri)
        }) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
            Text("Cámara", modifier = Modifier.padding(start = 4.dp))
        }
    }
}

