package com.example.solicitudes.model

// Filtros de las listas de solicitudes (criterios 22 a 27 del caso).
// null en un campo significa "sin filtrar por ese campo".
data class FiltrosSolicitud(
    val texto: String = "",
    val estado: EstadoSolicitud? = null,
    val prioridad: Prioridad? = null,
    val clienteId: Long? = null,
    val responsableId: Long? = null,
    val soloAtrasadas: Boolean = false
) {
    val hayFiltrosActivos: Boolean
        get() = texto.isNotBlank() || estado != null || prioridad != null ||
            clienteId != null || responsableId != null || soloAtrasadas

    // Aplica todos los filtros a la lista. El texto busca en código y título.
    fun aplicar(solicitudes: List<Solicitud>, ahora: Long): List<Solicitud> {
        val busqueda = texto.trim()
        return solicitudes.filter { s ->
            (busqueda.isEmpty() ||
                s.codigo.contains(busqueda, ignoreCase = true) ||
                s.titulo.contains(busqueda, ignoreCase = true)) &&
                (estado == null || s.estado == estado) &&
                (prioridad == null || s.prioridad == prioridad) &&
                (clienteId == null || s.clienteId == clienteId) &&
                (responsableId == null || s.responsableId == responsableId) &&
                (!soloAtrasadas || ReglasSolicitud.estaAtrasada(s, ahora))
        }
    }
}
