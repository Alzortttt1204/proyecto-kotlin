package com.example.solicitudes.ui.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Formatos de fecha que se muestran en la interfaz (las fechas se guardan en milisegundos)
object Formatos {

    private val localeChile = Locale.forLanguageTag("es-CL")
    private val fechaHora = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", localeChile)
    private val fecha = DateTimeFormatter.ofPattern("dd-MM-yyyy", localeChile)
    private val hora = DateTimeFormatter.ofPattern("HH:mm", localeChile)

    fun fechaHora(millis: Long): String = fechaHora.format(aZona(millis))

    fun fecha(millis: Long): String = fecha.format(aZona(millis))

    fun hora(millis: Long): String = hora.format(aZona(millis))

    private fun aZona(millis: Long) = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
}
