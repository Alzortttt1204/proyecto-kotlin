package com.example.solicitudes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.solicitudes.model.Rol
import com.example.solicitudes.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Usuario?

    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun observarTodos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE rol = :rol AND activo = 1 ORDER BY nombre")
    fun observarPorRol(rol: Rol): Flow<List<Usuario>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}
