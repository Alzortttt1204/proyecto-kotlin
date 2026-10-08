package com.example.solicitudes.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EstadoRevisionEvidencia(val etiqueta: String) {
    PENDIENTE("Pendiente de revisión"),
    REVISADA("Revisada")
}

// Archivo de prueba adjunto a una solicitud (imagen o documento ficticio)
@Entity(
    tableName = "evidencias",
    foreignKeys = [
        ForeignKey(entity = Solicitud::class, parentColumns = ["id"], childColumns = ["solicitudId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"])
    ],
    indices = [Index("solicitudId"), Index("usuarioId")]
)
data class Evidencia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val solicitudId: Long,
    val nombreArchivo: String,
    val tipoMime: String,
    val tamanoBytes: Long,
    val uri: String,
    val usuarioId: Long,
    val fecha: Long,
    val descripcion: String? = null,
    val estadoRevision: EstadoRevisionEvidencia = EstadoRevisionEvidencia.PENDIENTE
)
