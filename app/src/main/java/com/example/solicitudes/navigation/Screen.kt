package com.example.solicitudes.navigation

import com.example.solicitudes.model.Rol

// Sealed class para definir rutas tipo-safe en la navegación (guía 10)
sealed class Screen(val route: String) {

    // Rutas simples (sin argumentos)
    data object Login : Screen("login")
    data object InicioCliente : Screen("inicio_cliente")
    data object InicioAdmin : Screen("inicio_admin")
    data object InicioTecnico : Screen("inicio_tecnico")
    data object NuevaSolicitud : Screen("nueva_solicitud")

    // Ruta con argumento: detalle de una solicitud por su id
    data class DetalleSolicitud(val solicitudId: Long) : Screen(RUTA) {
        fun buildRoute(): String = route.replace("{$ARG_ID}", solicitudId.toString())

        companion object {
            const val ARG_ID = "solicitudId"
            const val RUTA = "detalle_solicitud/{solicitudId}"
        }
    }

    companion object {
        // Pantalla de inicio que corresponde a cada perfil
        fun inicioPara(rol: Rol): Screen = when (rol) {
            Rol.CLIENTE -> InicioCliente
            Rol.ADMIN -> InicioAdmin
            Rol.TECNICO -> InicioTecnico
        }
    }
}
