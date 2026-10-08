package com.example.solicitudes.model

// Estado de las listas de solicitudes de cliente, administrador y técnico
data class ListaSolicitudesUiState(
    val usuario: Usuario? = null,
    val solicitudes: List<Solicitud> = emptyList(),   // ya filtradas
    val totalVisibles: Int = 0,                        // antes de aplicar filtros
    val filtros: FiltrosSolicitud = FiltrosSolicitud(),
    val clientes: Map<Long, Cliente> = emptyMap(),
    val tecnicos: List<Usuario> = emptyList(),
    val nombresUsuarios: Map<Long, String> = emptyMap(),
    val ahora: Long = 0L
)

// Estado del panel resumen del administrador (tarjeta B5)
data class ResumenUiState(
    val resumen: ResumenSolicitudes? = null,
    val conteoBaseDeDatos: Map<EstadoSolicitud, Int> = emptyMap(),  // consulta GROUP BY en Room
    val nombresUsuarios: Map<Long, String> = emptyMap()
)

// Estado de la pantalla de detalle (tarjetas A6, A7, B2, B3 y B7)
data class DetalleSolicitudUiState(
    val cargando: Boolean = true,
    val usuario: Usuario? = null,
    val solicitud: Solicitud? = null,                  // null tras cargar = no existe o sin permiso
    val cliente: Cliente? = null,
    val historial: List<EventoHistorial> = emptyList(),
    val evidencias: List<Evidencia> = emptyList(),
    val nombresUsuarios: Map<Long, String> = emptyMap(),
    val tecnicos: List<Usuario> = emptyList(),
    val acciones: List<AccionSolicitud> = emptyList(),
    val puedeAdjuntar: Boolean = false,
    val accionEnCurso: AccionSolicitud? = null,        // acción cuyo diálogo está abierto
    val procesando: Boolean = false,
    val mensaje: String? = null,
    val ahora: Long = 0L
)
