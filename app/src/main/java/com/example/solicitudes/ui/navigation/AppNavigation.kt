package com.example.solicitudes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.solicitudes.ui.login.LoginScreen
import com.example.solicitudes.ui.login.LoginViewModel
import com.example.solicitudes.ui.menu.MenuScreen
import com.example.solicitudes.ui.solicitud.ListaSolicitudesScreen
import com.example.solicitudes.ui.solicitud.SolicitudFormScreen
import com.example.solicitudes.ui.solicitud.SolicitudViewModel

// Secuencia: Login -> Menú -> ListaSolicitudesScreen -> SolicitudFormScreen
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Un único SolicitudViewModel que comparten el menú, la lista y el formulario
    val loginViewModel: LoginViewModel = viewModel()
    val solicitudViewModel: SolicitudViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(navController, loginViewModel)
        }
        composable("menu") {
            MenuScreen(navController, solicitudViewModel)
        }
        composable("lista") {
            ListaSolicitudesScreen(navController, solicitudViewModel)
        }
        composable("formulario") {
            SolicitudFormScreen(navController, solicitudViewModel)
        }
    }
}
