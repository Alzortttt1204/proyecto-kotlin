package com.example.solicitudes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.solicitudes.model.EventoHistorial
import kotlinx.coroutines.flow.Flow

// Solo se insertan y consultan eventos: el historial no se edita ni se elimina.
@Dao
interface EventoHistorialDao {

    @Insert
    suspend fun insertar(evento: EventoHistorial): Long

    // Historial cronológico (criterio 11)
    @Query("SELECT * FROM eventos_historial WHERE solicitudId = :solicitudId ORDER BY fecha ASC, id ASC")
    fun observarPorSolicitud(solicitudId: Long): Flow<List<EventoHistorial>>

    // Último paso a "Pendiente de información", para saber a qué estado volver
    // cuando el cliente responde
    @Query(
        """SELECT * FROM eventos_historial
           WHERE solicitudId = :solicitudId AND estadoNuevo = 'PENDIENTE_INFORMACION'
           ORDER BY fecha DESC, id DESC LIMIT 1"""
    )
    suspend fun ultimoPasoAPendiente(solicitudId: Long): EventoHistorial?
}
