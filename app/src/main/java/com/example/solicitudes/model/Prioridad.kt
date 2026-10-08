package com.example.solicitudes.model

// Prioridades sugeridas por el caso. plazoHoras es el tiempo de atención esperado
// que se usa para detectar solicitudes atrasadas (valor académico, no un SLA real).
enum class Prioridad(val etiqueta: String, val plazoHoras: Long) {
    BAJA("Baja", 120),
    MEDIA("Media", 72),
    ALTA("Alta", 48),
    URGENTE("Urgente", 24)
}
