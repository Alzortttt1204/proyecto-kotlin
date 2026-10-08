package com.example.solicitudes.model

// Control de acceso por perfil (sección 5.1 "Seguridad" del caso)
object ReglasAcceso {

    // Cliente: solo sus propias solicitudes. Técnico: solo las asignadas a él. Admin: todas.
    fun puedeVer(usuario: Usuario, solicitud: Solicitud): Boolean = when (usuario.rol) {
        Rol.ADMIN -> true
        Rol.CLIENTE -> usuario.clienteId != null && solicitud.clienteId == usuario.clienteId
        Rol.TECNICO -> solicitud.responsableId == usuario.id
    }

    // El cliente no ve los comentarios internos de la empresa
    fun puedeVerEvento(rol: Rol, evento: EventoHistorial): Boolean =
        !(evento.interno && rol == Rol.CLIENTE)

    // Combina acceso a la solicitud y flujo de estados en una sola verificación
    fun puedeEjecutar(usuario: Usuario, solicitud: Solicitud, accion: AccionSolicitud): Boolean =
        usuario.activo &&
            puedeVer(usuario, solicitud) &&
            FlujoSolicitud.puedeEjecutar(usuario.rol, accion, solicitud.estado)
}
