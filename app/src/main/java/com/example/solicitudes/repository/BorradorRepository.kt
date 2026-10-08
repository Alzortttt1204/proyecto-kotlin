package com.example.solicitudes.repository

import android.content.Context
import androidx.core.content.edit
import com.example.solicitudes.model.Categoria

// Datos de una solicitud que el cliente guardó temporalmente sin enviar
data class Borrador(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: Categoria? = null,
    val servicio: String = "",
    val activo: String = ""
) {
    val estaVacio: Boolean
        get() = titulo.isBlank() && descripcion.isBlank() && categoria == null &&
            servicio.isBlank() && activo.isBlank()
}

/**
 * "Guardar temporalmente la solicitud" (sección 3.1 del caso).
 * Se guarda un borrador por usuario en SharedPreferences; las evidencias no se incluyen.
 */
class BorradorRepository(context: Context) {

    private val prefs = context.getSharedPreferences("borradores_solicitud", Context.MODE_PRIVATE)

    fun obtener(usuarioId: Long): Borrador = Borrador(
        titulo = prefs.getString(clave(usuarioId, "titulo"), "") ?: "",
        descripcion = prefs.getString(clave(usuarioId, "descripcion"), "") ?: "",
        categoria = prefs.getString(clave(usuarioId, "categoria"), null)
            ?.let { nombre -> Categoria.entries.firstOrNull { it.name == nombre } },
        servicio = prefs.getString(clave(usuarioId, "servicio"), "") ?: "",
        activo = prefs.getString(clave(usuarioId, "activo"), "") ?: ""
    )

    fun guardar(usuarioId: Long, borrador: Borrador) = prefs.edit {
        putString(clave(usuarioId, "titulo"), borrador.titulo)
        putString(clave(usuarioId, "descripcion"), borrador.descripcion)
        putString(clave(usuarioId, "categoria"), borrador.categoria?.name)
        putString(clave(usuarioId, "servicio"), borrador.servicio)
        putString(clave(usuarioId, "activo"), borrador.activo)
    }

    fun borrar(usuarioId: Long) = prefs.edit {
        listOf("titulo", "descripcion", "categoria", "servicio", "activo")
            .forEach { remove(clave(usuarioId, it)) }
    }

    private fun clave(usuarioId: Long, campo: String) = "u${usuarioId}_$campo"
}
