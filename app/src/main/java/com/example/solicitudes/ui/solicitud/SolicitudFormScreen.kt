package com.example.solicitudes.ui.solicitud

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    navController: NavController,
    viewModel: SolicitudViewModel
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    var categoriasAbiertas by remember { mutableStateOf(false) }

    // Uri del archivo donde la cámara guarda la foto (sobrevive si se gira la pantalla)
    var uriFoto by rememberSaveable { mutableStateOf<String?>(null) }

    // Cámara del dispositivo: toma la foto y la guarda en la Uri entregada
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            uriFoto?.let { viewModel.onEvidenciaChange(it) }
        }
    }

    // Al guardar en Room se vuelve a la pantalla anterior
    LaunchedEffect(estado.guardadoExitosamente) {
        if (estado.guardadoExitosamente) {
            Toast.makeText(context, "Solicitud guardada", Toast.LENGTH_SHORT).show()
            viewModel.nuevaSolicitud()
            navController.popBackStack()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val editando = estado.solicitudEditando
        if (editando == null) {
            Text("Nueva solicitud", style = MaterialTheme.typography.headlineSmall)
        } else {
            Text("Editar solicitud", style = MaterialTheme.typography.headlineSmall)
            Text("Código: ${editando.codigo}")
            Text("Estado: ${editando.estado}")
            Text("Creada: ${editando.fechaHora}", style = MaterialTheme.typography.bodySmall)
        }

        // Campo título
        OutlinedTextField(
            value = estado.titulo,
            onValueChange = viewModel::onTituloChange,
            label = { Text("Título") },
            singleLine = true,
            isError = estado.tituloError != null,
            supportingText = {
                estado.tituloError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo descripción
        OutlinedTextField(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcionChange,
            label = { Text("Descripción") },
            minLines = 3,
            isError = estado.descripcionError != null,
            supportingText = {
                estado.descripcionError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo categoría (lista desplegable)
        ExposedDropdownMenuBox(
            expanded = categoriasAbiertas,
            onExpandedChange = { categoriasAbiertas = it }
        ) {
            OutlinedTextField(
                value = estado.categoria,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriasAbiertas) },
                isError = estado.categoriaError != null,
                supportingText = {
                    estado.categoriaError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoriasAbiertas,
                onDismissRequest = { categoriasAbiertas = false }
            ) {
                viewModel.categorias.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria) },
                        onClick = {
                            viewModel.onCategoriaChange(categoria)
                            categoriasAbiertas = false
                        }
                    )
                }
            }
        }

        // Campo servicio relacionado
        OutlinedTextField(
            value = estado.servicio,
            onValueChange = viewModel::onServicioChange,
            label = { Text("Servicio relacionado") },
            placeholder = { Text("Ej: Sitio web, Hosting, Correo corporativo") },
            singleLine = true,
            isError = estado.servicioError != null,
            supportingText = {
                estado.servicioError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Evidencia: foto ficticia tomada con la cámara
        Text("Evidencia (opcional)", style = MaterialTheme.typography.titleSmall)
        estado.evidenciaUri?.let { FotoEvidencia(it) }
        OutlinedButton(
            onClick = {
                val uri = crearUriFoto(context)
                uriFoto = uri.toString()
                try {
                    camara.launch(uri)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No hay una app de cámara disponible", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (estado.evidenciaUri == null) "Tomar foto" else "Tomar otra foto")
        }

        // Botones: cancelar y guardar
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    if (viewModel.validarFormulario()) {
                        viewModel.guardar()
                    }
                },
                enabled = !estado.guardando,
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }
    }
}

// Muestra la foto de evidencia guardada
@Composable
fun FotoEvidencia(uri: String) {
    val context = LocalContext.current
    // Se lee la imagen reducida (inSampleSize) para no usar tanta memoria
    val bitmap = remember(uri) {
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 4 })
            }
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto de evidencia",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    } else {
        Text("No se pudo cargar la foto", color = MaterialTheme.colorScheme.error)
    }
}

// Crea un archivo vacío en la carpeta privada de la app y devuelve su Uri (FileProvider)
fun crearUriFoto(context: Context): Uri {
    val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
    val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}
