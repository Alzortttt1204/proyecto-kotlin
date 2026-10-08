package com.example.solicitudes.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.solicitudes.model.Usuario
import com.example.solicitudes.ui.screen.admin.InicioAdminScreen
import com.example.solicitudes.ui.screen.cliente.InicioClienteScreen
import com.example.solicitudes.ui.screen.cliente.NuevaSolicitudScreen
import com.example.solicitudes.ui.screen.detalle.DetalleSolicitudScreen
import com.example.solicitudes.ui.screen.login.LoginScreen
import com.example.solicitudes.ui.screen.tecnico.InicioTecnicoScreen
import com.example.solicitudes.viewmodel.AppViewModelProvider
import com.example.solicitudes.viewmodel.MainViewModel

// Define el NavHost con todas las pantallas y escucha los eventos de navegación
// que emite MainViewModel (guía 10).
@Composable
fun AppNavigation(
    mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val navController = rememberNavController()
    val usuario by mainViewModel.usuarioActual.collectAsStateWithLifecycle()

    // Escuchar eventos de navegación emitidos por el ViewModel
    LaunchedEffect(Unit) {
        mainViewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> navController.navigate(event.route) {
                    if (event.limpiarPila) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    } else {
                        event.popUpToRoute?.let { popUpTo(it) { inclusive = event.inclusive } }
                    }
                    launchSingleTop = event.singleTop
                }
                NavigationEvent.PopBackStack -> navController.popBackStack()
                NavigationEvent.NavigateUp -> navController.navigateUp()
            }
        }
    }

    // Permiso de notificaciones (Android 13+): se pide una vez al iniciar sesión (tarjeta A8)
    val permisoNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(usuario?.id) {
        if (usuario != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Si la app se reabre con una sesión activa, parte directo en el inicio del perfil.
    // remember: la pantalla inicial se decide una sola vez; los cambios de sesión
    // posteriores los maneja MainViewModel con eventos de navegación.
    val inicio = remember { usuario?.let { Screen.inicioPara(it.rol) } ?: Screen.Login }

    val abrirDetalle: (Long) -> Unit = { id -> mainViewModel.navigateTo(Screen.DetalleSolicitud(id).buildRoute()) }

    NavHost(navController = navController, startDestination = inicio.route) {

        composable(Screen.Login.route) {
            LoginScreen()
        }

        composable(Screen.InicioCliente.route) {
            ConSesion(usuario, mainViewModel) {
                InicioClienteScreen(
                    onNuevaSolicitud = { mainViewModel.navigateTo(Screen.NuevaSolicitud) },
                    onAbrirSolicitud = abrirDetalle
                )
            }
        }

        composable(Screen.InicioAdmin.route) {
            ConSesion(usuario, mainViewModel) {
                InicioAdminScreen(onAbrirSolicitud = abrirDetalle)
            }
        }

        composable(Screen.InicioTecnico.route) {
            ConSesion(usuario, mainViewModel) {
                InicioTecnicoScreen(onAbrirSolicitud = abrirDetalle)
            }
        }

        composable(Screen.NuevaSolicitud.route) {
            ConSesion(usuario, mainViewModel) {
                NuevaSolicitudScreen(
                    onVolver = mainViewModel::navigateBack,
                    // Al ver la solicitud recién creada, el formulario sale de la pila
                    onVerSolicitud = { id ->
                        mainViewModel.navigateTo(
                            Screen.DetalleSolicitud(id).buildRoute(),
                            popUpToRoute = Screen.NuevaSolicitud.route,
                            inclusive = true
                        )
                    }
                )
            }
        }

        composable(
            route = Screen.DetalleSolicitud.RUTA,
            arguments = listOf(navArgument(Screen.DetalleSolicitud.ARG_ID) { type = NavType.LongType })
        ) {
            ConSesion(usuario, mainViewModel) {
                DetalleSolicitudScreen(onVolver = mainViewModel::navigateBack)
            }
        }
    }
}

// Las pantallas protegidas solo se dibujan con una sesión iniciada;
// si se perdió (por ejemplo, Android cerró la app en segundo plano) se vuelve al login.
@Composable
private fun ConSesion(usuario: Usuario?, mainViewModel: MainViewModel, contenido: @Composable () -> Unit) {
    if (usuario != null) {
        contenido()
    } else {
        LaunchedEffect(Unit) { mainViewModel.irAlLogin() }
    }
}
