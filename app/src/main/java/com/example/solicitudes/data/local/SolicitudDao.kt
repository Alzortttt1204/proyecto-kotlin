package com.example.solicitudes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    @Insert
    suspend fun insertar(solicitud: SolicitudEntity)

    @Update
    suspend fun actualizar(solicitud: SolicitudEntity)

    // Flow: la lista se actualiza sola cada vez que cambia la tabla
    @Query("SELECT * FROM solicitudes ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<SolicitudEntity>>
}
