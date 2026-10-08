package com.example.solicitudes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.solicitudes.model.EstadoSolicitud
import com.example.solicitudes.model.Solicitud
import kotlinx.coroutines.flow.Flow

// Resultado de una consulta agrupada: cuántas solicitudes hay en cada estado
data class ConteoPorEstado(
    val estado: EstadoSolicitud,
    val cantidad: Int
)

@Dao
interface SolicitudDao {

    @Insert
    suspend fun insertar(solicitud: Solicitud): Long

    @Update
    suspend fun actualizar(solicitud: Solicitud)

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Solicitud?

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    fun observarPorId(id: Long): Flow<Solicitud?>

    // Las más recientes primero
    @Query("SELECT * FROM solicitudes ORDER BY fechaActualizacion DESC")
    fun observarTodas(): Flow<List<Solicitud>>

    @Query("SELECT * FROM solicitudes WHERE clienteId = :clienteId ORDER BY fechaActualizacion DESC")
    fun observarPorCliente(clienteId: Long): Flow<List<Solicitud>>

    @Query("SELECT * FROM solicitudes WHERE responsableId = :responsableId ORDER BY fechaActualizacion DESC")
    fun observarPorResponsable(responsableId: Long): Flow<List<Solicitud>>

    // Para el panel resumen del administrador (criterio 29)
    @Query("SELECT estado, COUNT(*) AS cantidad FROM solicitudes GROUP BY estado")
    fun observarConteoPorEstado(): Flow<List<ConteoPorEstado>>

    @Query("SELECT COUNT(*) FROM solicitudes")
    suspend fun contar(): Int
}
