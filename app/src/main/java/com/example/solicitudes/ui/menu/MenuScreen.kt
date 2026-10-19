package com.example.solicitudes.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.solicitudes.ui.solicitud.SolicitudViewModel

@Composable
fun MenuScreen(
    navController: NavController,
    viewModel: SolicitudViewModel
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Menú principal", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = { navController.navigate("lista") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver solicitudes")
        }

        Button(
            onClick = {
                viewModel.nuevaSolicitud()
                navController.navigate("formulario")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nueva solicitud")
        }

        OutlinedButton(
            onClick = {
                navController.navigate("login") {
                    popUpTo("menu") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}
