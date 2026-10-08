package com.example.solicitudes.model

// Límites para los archivos de prueba (sección 5.1 "Archivos y evidencias" del caso)
object ReglasEvidencia {

    const val TAMANO_MAXIMO_BYTES: Long = 5L * 1024 * 1024   // 5 MB

    val tiposPermitidos = mapOf(
        "image/jpeg" to "JPG",
        "image/png" to "PNG",
        "application/pdf" to "PDF"
    )

    /**
     * Valida un archivo antes de adjuntarlo.
     * Igual que UsuarioErrores de la guía 11: devuelve null si es válido,
     * o el mensaje de error que se debe mostrar al usuario.
     */
    fun validar(tipoMime: String?, tamanoBytes: Long): String? = when {
        tipoMime == null || tipoMime !in tiposPermitidos ->
            "Formato no permitido. Solo se aceptan ${tiposPermitidos.values.joinToString(", ")}."
        tamanoBytes <= 0 -> "El archivo está vacío."
        tamanoBytes > TAMANO_MAXIMO_BYTES -> "El archivo supera el máximo de 5 MB."
        else -> null
    }
}
