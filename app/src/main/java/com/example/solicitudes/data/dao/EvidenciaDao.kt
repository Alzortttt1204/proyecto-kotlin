package com.example.solicitudes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.solicitudes.model.Evidencia
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Insert
    suspend fun insertar(evidencia: Evidencia): Long

    @Query("SELECT * FROM evidencias WHERE solicitudId = :solicitudId ORDER BY fecha ASC")
    fun observarPorSolicitud(solicitudId: Long): Flow<List<Evidencia>>
}
