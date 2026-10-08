package com.example.solicitudes.model

// Mensaje estructurado para compartir una solicitud por WhatsApp (criterios 7 y 8).
// Solo incluye código, título, categoría y una referencia de seguimiento ficticia:
// nunca la descripción, datos del cliente ni información sensible.
object MensajeWhatsApp {

    // Dominio reservado ".test": el enlace es ficticio y no apunta a un servicio real
    private const val URL_SEGUIMIENTO = "https://seguimiento.demo.test/solicitud/"

    fun referencia(codigo: String): String = URL_SEGUIMIENTO + codigo

    fun construir(solicitud: Solicitud): String = buildString {
        appendLine("Solicitud registrada")
        appendLine("Código: ${solicitud.codigo}")
        appendLine("Título: ${solicitud.titulo}")
        appendLine("Categoría: ${solicitud.categoria.etiqueta}")
        appendLine("Estado: ${solicitud.estado.etiqueta}")
        append("Seguimiento: ${referencia(solicitud.codigo)}")
    }
}
