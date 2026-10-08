package com.example.solicitudes.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.solicitudes.MainActivity
import com.example.solicitudes.R
import com.example.solicitudes.model.AccionSolicitud
import com.example.solicitudes.model.Solicitud

// Los ViewModels solo conocen esta interfaz (no dependen de Android y se pueden probar)
fun interface Avisos {
    fun avisar(titulo: String, texto: String)
}

/**
 * Notificaciones locales (tarjeta A8, criterio 28 del caso).
 * Como el MVP no tiene servidor, la notificación se muestra en el mismo dispositivo
 * en el que se hizo el cambio: es una notificación "simulada" permitida por el caso.
 */
class Notificador(private val context: Context) : Avisos {

    init {
        crearCanal()
    }

    override fun avisar(titulo: String, texto: String) {
        // En Android 13+ se necesita el permiso POST_NOTIFICATIONS; sin él no se muestra nada
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val abrirApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(siguienteId++, notificacion)
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID, "Cambios en solicitudes", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Avisos de nuevas solicitudes, asignaciones y cambios de estado" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    private companion object {
        const val CANAL_ID = "solicitudes"
        var siguienteId = 1
    }
}

// Texto de la notificación para cada acción relevante; null = la acción no genera aviso
object TextosAviso {

    fun para(accion: AccionSolicitud, solicitud: Solicitud, nombreResponsable: String? = null): Pair<String, String>? {
        val codigo = solicitud.codigo
        return when (accion) {
            AccionSolicitud.CREAR -> "Nueva solicitud recibida" to "$codigo · ${solicitud.titulo}"
            AccionSolicitud.ASIGNAR, AccionSolicitud.REASIGNAR ->
                "Solicitud asignada" to "$codigo asignada a ${nombreResponsable ?: "un técnico"}"
            AccionSolicitud.CAMBIAR_PRIORIDAD ->
                "Cambio de prioridad" to "$codigo ahora tiene prioridad ${solicitud.prioridad?.etiqueta}"
            AccionSolicitud.SOLICITAR_INFORMACION ->
                "Se necesita información" to "$codigo está pendiente de información del cliente"
            AccionSolicitud.RESPONDER_INFORMACION -> "Información recibida" to "El cliente respondió en $codigo"
            AccionSolicitud.COMENTAR, AccionSolicitud.REGISTRAR_AVANCE -> "Nuevo comentario" to "Hay novedades en $codigo"
            AccionSolicitud.INICIAR_TRABAJO -> "Solicitud en proceso" to "Se comenzó a trabajar en $codigo"
            AccionSolicitud.MARCAR_RESUELTA -> "Solicitud resuelta" to "$codigo fue resuelta. Confirma si quedó conforme."
            AccionSolicitud.CONFIRMAR_RESOLUCION, AccionSolicitud.CERRAR -> "Solicitud cerrada" to "$codigo fue cerrada"
            AccionSolicitud.REABRIR -> "Solicitud reabierta" to "El cliente pidió reabrir $codigo"
            AccionSolicitud.CANCELAR -> "Solicitud cancelada" to "$codigo fue cancelada"
            else -> null
        }
    }
}
