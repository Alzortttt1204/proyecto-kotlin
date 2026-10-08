package com.example.solicitudes.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Usuario ficticio de la app. clienteId solo se usa en el perfil cliente
// para saber a qué empresa pertenece y qué solicitudes puede ver.
// La clave nunca se guarda en texto plano: solo su hash (ver repository/Credenciales.kt).
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val clienteId: Long? = null,
    val claveHash: String = "",
    val activo: Boolean = true,
    val fechaCreacion: Long = 0L,
    val fechaActualizacion: Long = fechaCreacion
)
