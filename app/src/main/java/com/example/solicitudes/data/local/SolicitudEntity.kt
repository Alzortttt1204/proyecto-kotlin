package com.example.solicitudes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val servicio: String,
    val estado: String = "Recibida",
    val fechaHora: String,
    val evidenciaUri: String? = null
)
