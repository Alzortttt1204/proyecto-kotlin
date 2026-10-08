package com.example.solicitudes.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Evento del historial: cada acción sobre una solicitud deja uno.
// interno = true marca comentarios que el cliente no puede ver.
// El historial nunca se modifica ni se elimina, solo se agregan eventos.
@Entity(
    tableName = "eventos_historial",
    foreignKeys = [
        ForeignKey(entity = Solicitud::class, parentColumns = ["id"], childColumns = ["solicitudId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"])
    ],
    indices = [Index("solicitudId"), Index("usuarioId")]
)
data class EventoHistorial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val solicitudId: Long,
    val estadoAnterior: EstadoSolicitud?,
    val estadoNuevo: EstadoSolicitud,
    val accion: AccionSolicitud,
    val comentario: String? = null,
    val interno: Boolean = false,
    val rol: Rol,
    val usuarioId: Long,
    val fecha: Long,
    val evidenciaId: Long? = null
)
