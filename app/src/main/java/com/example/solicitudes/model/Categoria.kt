package com.example.solicitudes.model

// Categorías sugeridas por el caso (sección 3.2)
enum class Categoria(val etiqueta: String) {
    DESARROLLO_WEB("Desarrollo web"),
    MANTENIMIENTO_WEB("Mantenimiento web"),
    INCIDENTE_TECNICO("Incidente técnico"),
    HOSTING("Hosting"),
    SOPORTE_CORREO("Soporte de correo"),
    MARKETING_DIGITAL("Marketing digital"),
    SOLICITUD_CAMBIO("Solicitud de cambio"),
    CONSULTA("Consulta"),
    ACCESO_CONFIGURACION("Acceso o configuración"),
    OTRO("Otro requerimiento")
}
