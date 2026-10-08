package com.example.solicitudes.ui.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.solicitudes.model.EvidenciaPendiente
import com.example.solicitudes.model.ReglasEvidencia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manejo de archivos de evidencia (guía 13: galería y cámara con FileProvider).
 *
 * Los archivos elegidos se COPIAN a la carpeta privada de la app (files/evidencias),
 * porque el permiso para leer un archivo de la galería puede perderse al cerrar la app.
 */
object ArchivosEvidencia {

    private const val CARPETA = "evidencias"

    // Tipos que se ofrecen al abrir el selector de archivos
    val tiposSelector: Array<String> = ReglasEvidencia.tiposPermitidos.keys.toTypedArray()

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { mkdirs() }

    fun uriDe(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    // Archivo vacío donde la cámara guardará la foto (TakePicture necesita la Uri antes)
    fun nuevaFoto(context: Context): Pair<File, Uri> {
        val archivo = File(carpeta(context), "foto_${System.currentTimeMillis()}.jpg")
        return archivo to uriDe(context, archivo)
    }

    // Foto ya tomada por la cámara
    fun desdeFoto(archivo: File, uri: Uri): Result<EvidenciaPendiente> = runCatching {
        ReglasEvidencia.validar("image/jpeg", archivo.length())?.let { throw IllegalArgumentException(it) }
        EvidenciaPendiente(archivo.name, "image/jpeg", archivo.length(), uri.toString())
    }

    // Archivo elegido con el selector: valida formato y tamaño ANTES de copiarlo
    suspend fun importar(context: Context, origen: Uri): Result<EvidenciaPendiente> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val tipo = resolver.getType(origen)
            var nombre = "archivo"
            var tamano = -1L
            resolver.query(origen, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        nombre = cursor.getString(0) ?: nombre
                        if (!cursor.isNull(1)) tamano = cursor.getLong(1)
                    }
                }
            ReglasEvidencia.validar(tipo, tamano)?.let { throw IllegalArgumentException(it) }

            val destino = File(carpeta(context), "${System.currentTimeMillis()}_${nombre.replace(Regex("[^A-Za-z0-9._-]"), "_")}")
            resolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: error("No se pudo leer el archivo")
            EvidenciaPendiente(nombre, tipo!!, destino.length(), uriDe(context, destino).toString())
        }
    }

    // Abre la evidencia con la app del teléfono que corresponda (visor de imágenes o PDF)
    fun abrir(context: Context, uri: String, tipoMime: String): Boolean = try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse(uri), tipoMime)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    fun tamanoLegible(bytes: Long): String = when {
        bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }
}
