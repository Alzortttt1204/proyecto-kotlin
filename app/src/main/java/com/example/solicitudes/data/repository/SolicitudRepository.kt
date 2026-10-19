package com.example.solicitudes.data.repository

import com.example.solicitudes.data.local.SolicitudDao
import com.example.solicitudes.data.local.SolicitudEntity
import kotlinx.coroutines.flow.Flow

// Único punto de acceso a los datos para el ViewModel
class SolicitudRepository(private val dao: SolicitudDao) {

    fun obtenerTodas(): Flow<List<SolicitudEntity>> = dao.obtenerTodas()

    suspend fun insertar(solicitud: SolicitudEntity) = dao.insertar(solicitud)

    suspend fun actualizar(solicitud: SolicitudEntity) = dao.actualizar(solicitud)
}
