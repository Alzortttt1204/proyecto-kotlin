package com.example.solicitudes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Cliente o empresa solicitante (siempre con datos ficticios)
@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombreContacto: String,
    val empresa: String,
    val correo: String,
    val telefono: String,
    val servicios: List<String> = emptyList()   // se guarda con Convertidores
)
