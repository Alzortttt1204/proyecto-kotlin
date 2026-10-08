package com.example.solicitudes.ui.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

/**
 * Comparte el mensaje estructurado de una solicitud (tarjeta A5).
 * Solo abre WhatsApp con el texto ya escrito: el usuario elige el chat y decide si lo envía.
 * No se leen conversaciones ni se usan cuentas o credenciales de WhatsApp (sección 3.4 del caso).
 * Si WhatsApp no está instalado (por ejemplo en el emulador) se abre el menú para compartir de Android.
 */
object CompartirWhatsApp {

    private val paquetesWhatsApp = listOf("com.whatsapp", "com.whatsapp.w4b")

    fun compartir(context: Context, mensaje: String) {
        val base = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, mensaje)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        for (paquete in paquetesWhatsApp) {
            try {
                context.startActivity(Intent(base).setPackage(paquete))
                return
            } catch (_: ActivityNotFoundException) {
                // Se prueba con el siguiente paquete o con el menú general
            }
        }
        context.startActivity(
            Intent.createChooser(base, "Compartir solicitud").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
