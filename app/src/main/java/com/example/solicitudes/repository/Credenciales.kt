package com.example.solicitudes.repository

import java.security.MessageDigest

// Manejo de las claves de las cuentas ficticias. Solo se guarda el hash SHA-256
// para no dejar claves en texto plano en la base de datos.
// (Nivel académico: una app productiva usaría un algoritmo con sal como bcrypt o Argon2.)
object Credenciales {

    fun hash(clave: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(clave.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun coincide(clave: String, claveHash: String): Boolean =
        claveHash.isNotEmpty() && hash(clave) == claveHash
}
