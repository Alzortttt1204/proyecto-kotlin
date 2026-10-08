package com.example.solicitudes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.solicitudes.model.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    @Insert
    suspend fun insertar(cliente: Cliente): Long

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Cliente?

    @Query("SELECT * FROM clientes ORDER BY empresa")
    fun observarTodos(): Flow<List<Cliente>>
}
