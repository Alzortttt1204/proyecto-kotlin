package com.example.solicitudes.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.solicitudes.AppSolicitudesApp

/**
 * Fábrica de ViewModels de la app. Room necesita un Context, así que los ViewModels
 * reciben el repositorio y la sesión desde AppSolicitudesApp a través de esta fábrica.
 *
 * Uso en una pantalla:
 *   val vm: MiViewModel = viewModel(factory = AppViewModelProvider.Factory)
 *
 * Para agregar un ViewModel nuevo, agrega un initializer { ... } con su constructor.
 */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            MainViewModel(app().repositorio, app().sesion, app().notificador)
        }
        initializer {
            LoginViewModel(app().repositorio, app().sesion)
        }
        initializer {
            ListaSolicitudesViewModel(app().repositorio, app().sesion)
        }
        initializer {
            ResumenViewModel(app().repositorio, app().sesion)
        }
        initializer {
            NuevaSolicitudViewModel(app().repositorio, app().sesion, app().borradores, app().notificador)
        }
        initializer {
            // createSavedStateHandle() trae el argumento "solicitudId" de la ruta de navegación
            DetalleSolicitudViewModel(createSavedStateHandle(), app().repositorio, app().sesion, app().notificador)
        }
    }
}

// Obtiene la instancia de AppSolicitudesApp desde los CreationExtras del ViewModel
fun CreationExtras.app(): AppSolicitudesApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AppSolicitudesApp
