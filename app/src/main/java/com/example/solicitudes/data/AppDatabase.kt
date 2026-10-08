package com.example.solicitudes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.solicitudes.data.dao.ClienteDao
import com.example.solicitudes.data.dao.EventoHistorialDao
import com.example.solicitudes.data.dao.EvidenciaDao
import com.example.solicitudes.data.dao.SolicitudDao
import com.example.solicitudes.data.dao.UsuarioDao
import com.example.solicitudes.model.Cliente
import com.example.solicitudes.model.EventoHistorial
import com.example.solicitudes.model.Evidencia
import com.example.solicitudes.model.Solicitud
import com.example.solicitudes.model.Usuario

// Base de datos local de la app (Room sobre SQLite).
// Permite que la demostración funcione sin servicio remoto (criterio 35).
@Database(
    entities = [Cliente::class, Usuario::class, Solicitud::class, EventoHistorial::class, Evidencia::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Convertidores::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun solicitudDao(): SolicitudDao
    abstract fun eventoHistorialDao(): EventoHistorialDao
    abstract fun evidenciaDao(): EvidenciaDao

    companion object {
        private const val NOMBRE_BD = "solicitudes.db"

        @Volatile
        private var instancia: AppDatabase? = null

        // Una sola instancia de la base de datos para toda la app
        fun getDatabase(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_BD
                ).build().also { instancia = it }
            }
    }
}
