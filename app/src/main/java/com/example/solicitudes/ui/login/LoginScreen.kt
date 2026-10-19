package com.example.solicitudes.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val estado by viewModel.estado.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Gestión de solicitudes", style = MaterialTheme.typography.headlineSmall)
        Text("Usuario de prueba: demo / clave: 1234", style = MaterialTheme.typography.bodySmall)

        // Campo usuario
        OutlinedTextField(
            value = estado.usuario,
            onValueChange = viewModel::onUsuarioChange,
            label = { Text("Usuario") },
            singleLine = true,
            isError = estado.usuarioError != null,
            supportingText = {
                estado.usuarioError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo clave
        OutlinedTextField(
            value = estado.clave,
            onValueChange = viewModel::onClaveChange,
            label = { Text("Clave") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = estado.claveError != null,
            supportingText = {
                estado.claveError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Botón: ingresar
        Button(
            onClick = {
                if (viewModel.validarFormulario()) {
                    // Al entrar al menú, el login sale de la pila (atrás no vuelve al login)
                    navController.navigate("menu") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }
    }
}
