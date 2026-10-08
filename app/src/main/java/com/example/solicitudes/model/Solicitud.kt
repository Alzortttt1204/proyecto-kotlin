package com.example.solicitudes.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Solicitud de un cliente. Las fechas se guardan en milisegundos (epoch).
// Los enums (categoría, prioridad, estado) Room los guarda por su nombre.
@Entity(
    tableName = "solicitudes",
    foreignKeys = [
        ForeignKey(entity = Cliente::class, parentColumns = ["id"], childColumns = ["clienteId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["creadorId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["responsableId"])
    ],
    indices = [
        Index(value = ["codigo"], unique = true),
        Index("clienteId"),
        Index("creadorId"),
        Index("responsableId")
    ]
)
data class Solicitud(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val codigo: String,
    val clienteId: Long,
    val creadorId: Long,
    val titulo: String,
    val descripcion: String,
    val categoria: Categoria,
    val servicioRelacionado: String,
    val activoRelacionado: String? = null,
    val prioridad: Prioridad? = null,          // la define el administrador
    val estado: EstadoSolicitud = EstadoSolicitud.RECIBIDA,
    val responsableId: Long? = null,            // técnico asignado
    val fechaCreacion: Long,
    val fechaActualizacion: Long = fechaCreacion,
    val fechaResolucion: Long? = null,
    val solucion: String? = null,
    val confirmacionCierre: Boolean = false,
    val motivoCancelacion: String? = null
)
