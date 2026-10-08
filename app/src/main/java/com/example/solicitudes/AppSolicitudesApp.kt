package com.example.solicitudes

import android.app.Application
import com.example.solicitudes.data.AppDatabase
import com.example.solicitudes.data.DatosSemilla
import com.example.solicitudes.notificaciones.Notificador
import com.example.solicitudes.repository.BorradorRepository
import com.example.solicitudes.repository.SesionRepository
import com.example.solicitudes.repository.SolicitudesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Clase Application: se crea una sola vez al abrir la app.
 * Guarda la base de datos, los repositorios, la sesión y el notificador para que
 * todos los ViewModels usen la misma instancia (se entregan con viewmodel/AppViewModelProvider.kt).
 */
class AppSolicitudesApp : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val repositorio: SolicitudesRepository by lazy { SolicitudesRepository(database) }

    val sesion: SesionRepository by lazy { SesionRepository() }

    val borradores: BorradorRepository by lazy { BorradorRepository(this) }

    val notificador: Notificador by lazy { Notificador(this) }

    override fun onCreate() {
        super.onCreate()
        // Carga los datos ficticios la primera vez que se abre la app
        scope.launch { DatosSemilla.poblarSiVacia(database) }
    }
}
