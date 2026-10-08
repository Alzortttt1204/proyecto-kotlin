package com.example.solicitudes.data

import androidx.room.withTransaction
import com.example.solicitudes.model.AccionSolicitud
import com.example.solicitudes.model.Categoria
import com.example.solicitudes.model.Cliente
import com.example.solicitudes.model.Prioridad
import com.example.solicitudes.model.Rol
import com.example.solicitudes.model.Solicitud
import com.example.solicitudes.model.Usuario
import com.example.solicitudes.repository.Credenciales
import com.example.solicitudes.repository.SolicitudesRepository

/**
 * Datos ficticios para la demostración (sección 7 del caso).
 * Ningún nombre, correo ni teléfono es real: los correos usan el dominio
 * reservado ".test" y los teléfonos son de prueba.
 *
 * Cuentas de prueba (todas con la clave CLAVE_DEMO):
 *  - Clientes:  ana.prueba@demo.test, bruno.ejemplo@demo.test, carla.muestra@demo.test
 *  - Admin:     coordinacion@demo.test
 *  - Técnicos:  tecnico.esteban@demo.test, tecnico.fernanda@demo.test
 *
 * Las solicitudes de ejemplo se crean pasando por el repositorio, igual que en la app,
 * para que su historial sea coherente y cubra todos los estados.
 */
object DatosSemilla {

    const val CLAVE_DEMO = "demo123"

    private const val MINUTO = 60_000L
    private const val HORA = 60 * MINUTO
    private const val DIA = 24 * HORA

    // Inserta los datos solo si la base de datos está vacía
    suspend fun poblarSiVacia(db: AppDatabase, ahora: Long = System.currentTimeMillis()) {
        if (db.usuarioDao().contar() > 0) return

        // Reloj simulado: permite que cada solicitud tenga fechas en el pasado
        var tiempo = ahora - 30 * DIA
        val repo = SolicitudesRepository(db) { tiempo }

        db.withTransaction {
            // ---------- Clientes / empresas ficticias ----------
            val panaderia = db.clienteDao().insertar(
                Cliente(
                    nombreContacto = "Ana Prueba", empresa = "Panadería El Trigal (ficticia)",
                    correo = "contacto@trigal.demo.test", telefono = "+56 9 0000 0001",
                    servicios = listOf("Sitio web", "Correo corporativo")
                )
            )
            val ferreteria = db.clienteDao().insertar(
                Cliente(
                    nombreContacto = "Bruno Ejemplo", empresa = "Ferretería Tornillo Feliz (ficticia)",
                    correo = "contacto@tornillo.demo.test", telefono = "+56 9 0000 0002",
                    servicios = listOf("Hosting", "Correo corporativo", "Certificado SSL")
                )
            )
            val veterinaria = db.clienteDao().insertar(
                Cliente(
                    nombreContacto = "Carla Muestra", empresa = "Veterinaria Patitas (ficticia)",
                    correo = "contacto@patitas.demo.test", telefono = "+56 9 0000 0003",
                    servicios = listOf("Sitio web", "Marketing digital")
                )
            )

            // ---------- Usuarios ----------
            suspend fun usuario(nombre: String, correo: String, rol: Rol, clienteId: Long? = null): Usuario {
                val u = Usuario(
                    nombre = nombre, correo = correo, rol = rol, clienteId = clienteId,
                    claveHash = Credenciales.hash(CLAVE_DEMO), fechaCreacion = tiempo
                )
                return u.copy(id = db.usuarioDao().insertar(u))
            }

            val ana = usuario("Ana Prueba", "ana.prueba@demo.test", Rol.CLIENTE, panaderia)
            val bruno = usuario("Bruno Ejemplo", "bruno.ejemplo@demo.test", Rol.CLIENTE, ferreteria)
            val carla = usuario("Carla Muestra", "carla.muestra@demo.test", Rol.CLIENTE, veterinaria)
            val admin = usuario("Daniela Coordinadora", "coordinacion@demo.test", Rol.ADMIN)
            val esteban = usuario("Esteban Técnico", "tecnico.esteban@demo.test", Rol.TECNICO)
            val fernanda = usuario("Fernanda Soporte", "tecnico.fernanda@demo.test", Rol.TECNICO)

            // ---------- Ayudantes para armar el historial ----------
            suspend fun crear(
                horasAtras: Long, cliente: Usuario, titulo: String, descripcion: String,
                categoria: Categoria, servicio: String, activo: String? = null
            ): Solicitud {
                tiempo = ahora - horasAtras * HORA
                return repo.crearSolicitud(cliente, titulo, descripcion, categoria, servicio, activo).getOrThrow()
            }

            suspend fun paso(
                minutosDespues: Long, quien: Usuario, solicitud: Solicitud, accion: AccionSolicitud,
                texto: String? = null, interno: Boolean = false,
                responsable: Usuario? = null, prioridad: Prioridad? = null
            ) {
                tiempo += minutosDespues * MINUTO
                repo.ejecutarAccion(
                    quien, solicitud.id, accion, texto, interno,
                    responsableId = responsable?.id, prioridad = prioridad
                ).getOrThrow()
            }

            // 1) Recibida: solicitud nueva sin revisar
            crear(
                2, ana, "Actualizar horario de atención en el sitio",
                "Cambiamos el horario de los sábados, hay que actualizarlo en la página de contacto.",
                Categoria.MANTENIMIENTO_WEB, "Sitio web", "www.trigal.demo.test"
            )

            // 2) En revisión con prioridad alta
            crear(
                30, bruno, "El correo corporativo no envía mensajes",
                "Desde ayer los correos enviados quedan en la bandeja de salida.",
                Categoria.SOPORTE_CORREO, "Correo corporativo"
            ).also {
                paso(40, admin, it, AccionSolicitud.REVISAR)
                paso(5, admin, it, AccionSolicitud.CAMBIAR_PRIORIDAD, prioridad = Prioridad.ALTA)
            }

            // 3) Pendiente de información del cliente
            crear(
                50, carla, "Agregar sección de servicios al sitio",
                "Queremos una nueva sección con los servicios de peluquería canina.",
                Categoria.DESARROLLO_WEB, "Sitio web", "www.patitas.demo.test"
            ).also {
                paso(60, admin, it, AccionSolicitud.REVISAR)
                paso(10, admin, it, AccionSolicitud.CAMBIAR_PRIORIDAD, prioridad = Prioridad.MEDIA)
                paso(5, admin, it, AccionSolicitud.SOLICITAR_INFORMACION, "¿Pueden enviar los textos y fotos de prueba para la sección?")
            }

            // 4) Asignada y atrasada (urgente, creada hace más de 24 h)
            crear(
                100, ana, "El sitio web carga muy lento",
                "La página de inicio tarda más de 20 segundos en cargar.",
                Categoria.INCIDENTE_TECNICO, "Sitio web", "www.trigal.demo.test"
            ).also {
                paso(30, admin, it, AccionSolicitud.CAMBIAR_PRIORIDAD, prioridad = Prioridad.URGENTE)
                paso(5, admin, it, AccionSolicitud.ASIGNAR, responsable = esteban)
            }

            // 5) En proceso con avance y comentario interno
            crear(
                20, bruno, "Renovar certificado SSL",
                "El navegador avisa que el certificado del sitio vence pronto.",
                Categoria.HOSTING, "Certificado SSL", "www.tornillo.demo.test"
            ).also {
                paso(15, admin, it, AccionSolicitud.CAMBIAR_PRIORIDAD, prioridad = Prioridad.ALTA)
                paso(5, admin, it, AccionSolicitud.ASIGNAR, responsable = esteban)
                paso(60, esteban, it, AccionSolicitud.INICIAR_TRABAJO)
                paso(30, esteban, it, AccionSolicitud.REGISTRAR_AVANCE, "Certificado nuevo generado en el entorno de prueba.")
                paso(10, esteban, it, AccionSolicitud.COMENTAR, "Falta validar el dominio con el proveedor.", interno = true)
            }

            // 6) Resuelta, esperando confirmación del cliente
            crear(
                80, carla, "Publicación para campaña de vacunación",
                "Necesitamos una publicación para redes sociales sobre la campaña de vacunación.",
                Categoria.MARKETING_DIGITAL, "Marketing digital"
            ).also {
                paso(20, admin, it, AccionSolicitud.CAMBIAR_PRIORIDAD, prioridad = Prioridad.MEDIA)
                paso(5, admin, it, AccionSolicitud.ASIGNAR, responsable = fernanda)
                paso(120, fernanda, it, AccionSolicitud.INICIAR_TRABAJO)
                paso(240, fernanda, it, AccionSolicitud.MARCAR_RESUELTA, "Publicación diseñada y enviada para revisión del cliente.")
            }

            // 7) Cerrada con conformidad del cliente
            crear(
                200, ana, "Crear cuenta de correo para nueva persona",
                "Se incorporó una persona al equipo y necesita una cuenta de correo.",
                Categoria.ACCESO_CONFIGURACION, "Correo corporativo"
            ).also {
                paso(30, admin, it, AccionSolicitud.CAMBIAR_PRIORIDAD, prioridad = Prioridad.BAJA)
                paso(5, admin, it, AccionSolicitud.ASIGNAR, responsable = fernanda)
                paso(60, fernanda, it, AccionSolicitud.INICIAR_TRABAJO)
                paso(45, fernanda, it, AccionSolicitud.MARCAR_RESUELTA, "Cuenta creada con datos de prueba y enviada al cliente.")
                paso(300, ana, it, AccionSolicitud.CONFIRMAR_RESOLUCION)
            }

            // 8) Cancelada con motivo
            crear(
                150, bruno, "Consulta por cambio de plan de hosting",
                "Queremos saber si conviene cambiar a un plan con más espacio.",
                Categoria.CONSULTA, "Hosting"
            ).also {
                paso(60, admin, it, AccionSolicitud.REVISAR)
                paso(10, admin, it, AccionSolicitud.CANCELAR, "El cliente resolvió la consulta por otro medio (registro duplicado).")
            }
        }
    }
}
