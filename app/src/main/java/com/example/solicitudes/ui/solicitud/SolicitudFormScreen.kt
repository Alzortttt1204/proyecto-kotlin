package com.example.solicitudes.ui.solicitud

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    // Recurso nativo 1 - Cámara: toma la foto y la guarda en la Uri entregada
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            uriFoto?.let { viewModel.onEvidenciaChange(it) }
        }
    }

    // Recurso nativo 2 - Galería: elige una imagen guardada en el dispositivo
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            // Permiso permanente para seguir viendo la imagen después de reiniciar la app
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.onEvidenciaChange(uri.toString())
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

    val editando = estado.solicitudEditando

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editando == null) "Nueva solicitud" else "Editar solicitud") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Datos que genera la app (solo se ven al consultar o editar)
            if (editando != null) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Código: ${editando.codigo}", style = MaterialTheme.typography.titleSmall)
                        Text("Estado: ${editando.estado}")
                        Text("Creada: ${editando.fechaHora}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Campo título
            OutlinedTextField(
                value = estado.titulo,
                onValueChange = viewModel::onTituloChange,
                label = { Text("Título") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Title, contentDescription = null) },
                trailingIcon = {
                    if (estado.tituloError != null) Icon(Icons.Filled.Error, contentDescription = "Error")
                },
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
                leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
                trailingIcon = {
                    if (estado.descripcionError != null) Icon(Icons.Filled.Error, contentDescription = "Error")
                },
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
                    leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) },
                    trailingIcon = {
                        // Con error muestra el ícono de error; si no, la flecha de la lista
                        if (estado.categoriaError != null) {
                            Icon(Icons.Filled.Error, contentDescription = "Error")
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriasAbiertas)
                        }
                    },
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
                placeholder = { Text("Ej: Sitio web, Hosting, Correo") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Build, contentDescription = null) },
                trailingIcon = {
                    if (estado.servicioError != null) Icon(Icons.Filled.Error, contentDescription = "Error")
                },
                isError = estado.servicioError != null,
                supportingText = {
                    estado.servicioError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Evidencia ficticia: foto con la cámara o imagen de la galería
            Text("Evidencia (opcional)", style = MaterialTheme.typography.titleSmall)

            // Animación: la foto aparece con un desvanecido y se expande al adjuntarla
            AnimatedVisibility(
                visible = estado.evidenciaUri != null,
                enter = fadeIn() + expandVertically()
            ) {
                estado.evidenciaUri?.let { FotoEvidencia(it) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Text("Cámara", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(
                    onClick = { galeria.launch(arrayOf("image/*")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Text("Galería", modifier = Modifier.padding(start = 8.dp))
                }
            }

            // Botón: guardar
            Button(
                onClick = {
                    if (viewModel.validarFormulario()) {
                        viewModel.guardar()
                    }
                },
                enabled = !estado.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
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
                .clip(RoundedCornerShape(12.dp))
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
