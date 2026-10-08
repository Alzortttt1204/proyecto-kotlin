package com.example.solicitudes.ui.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solicitudes.viewmodel.AppViewModelProvider
import com.example.solicitudes.viewmodel.LoginViewModel

// Cuentas ficticias para el acceso rápido en la demostración (definidas en DatosSemilla)
private val cuentasDemo = listOf(
    "Cliente" to "ana.prueba@demo.test",
    "Administrador" to "coordinacion@demo.test",
    "Técnico" to "tecnico.esteban@demo.test"
)

// Pantalla de inicio de sesión (tarjeta A1)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var verClave by rememberSaveable { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Icon(
                Icons.Outlined.SupportAgent,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Text("Seguimiento de solicitudes", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                "Ingresa con tu cuenta para registrar o revisar solicitudes",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier.widthIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = estado.correo,
                    onValueChange = viewModel::onCorreoChange,
                    label = { Text("Correo") },
                    singleLine = true,
                    isError = estado.errores.correo != null,
                    supportingText = { estado.errores.correo?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.clave,
                    onValueChange = viewModel::onClaveChange,
                    label = { Text("Clave") },
                    singleLine = true,
                    isError = estado.errores.clave != null,
                    supportingText = { estado.errores.clave?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verClave = !verClave }) {
                            Icon(
                                if (verClave) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (verClave) "Ocultar clave" else "Mostrar clave"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.iniciarSesion() }),
                    modifier = Modifier.fillMaxWidth()
                )

                estado.mensajeError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                Button(
                    onClick = viewModel::iniciarSesion,
                    enabled = !estado.cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (estado.cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Iniciar sesión")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Cuentas de prueba (datos ficticios)", style = MaterialTheme.typography.labelLarge)
                cuentasDemo.forEach { (perfil, correo) ->
                    AssistChip(
                        onClick = { viewModel.usarCuentaDemo(correo) },
                        label = { Text("$perfil · $correo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
