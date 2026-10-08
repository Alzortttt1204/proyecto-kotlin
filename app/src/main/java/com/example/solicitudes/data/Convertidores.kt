package com.example.solicitudes.data

import androidx.room.TypeConverter

// Room solo guarda tipos simples. Este convertidor transforma la lista de
// servicios de un cliente en un texto separado por "|" y viceversa.
class Convertidores {

    @TypeConverter
    fun desdeLista(valores: List<String>): String = valores.joinToString(SEPARADOR)

    @TypeConverter
    fun haciaLista(texto: String): List<String> =
        if (texto.isBlank()) emptyList() else texto.split(SEPARADOR)

    private companion object {
        const val SEPARADOR = "|"
    }
}
