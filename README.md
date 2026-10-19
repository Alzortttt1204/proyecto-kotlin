# AppSolicitudes_GrupoX

App Android del **caso DSY1105-005D** (Duoc UC): *Gestión móvil y seguimiento de solicitudes de
clientes*. Esta **primera entrega** implementa solo el **módulo de Solicitudes**, como indican los
lineamientos: registrar, listar, consultar y editar solicitudes, con una foto de evidencia tomada
con la cámara.

> Nombre provisional: reemplazar `X` por el número de grupo en `settings.gradle.kts`
> (`rootProject.name`).

Todos los datos son **ficticios** (solicitudes, fotos y usuario de prueba).

## Tecnologías

Kotlin · Jetpack Compose · Material 3 · MVVM (ViewModel + UiState + StateFlow) ·
Navigation Compose · Room · Cámara (`TakePicture` + `FileProvider`).

## Cómo ejecutar

1. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
2. Ejecutar en un emulador o celular con Android 8.0 (API 26) o superior.
3. Ingresar con el usuario de prueba **demo** / clave **1234**.

## Estructura

```
app/src/main/java/com/example/solicitudes/
├── data/
│   ├── local/
│   │   ├── SolicitudEntity.kt      Tabla "solicitudes" de Room
│   │   ├── SolicitudDao.kt         insertar, actualizar, obtenerTodas (Flow)
│   │   └── AppDatabase.kt          Base de datos Room (getDatabase)
│   └── repository/
│       └── SolicitudRepository.kt  Acceso a los datos para el ViewModel
├── ui/
│   ├── login/                      LoginScreen + LoginViewModel + LoginUiState
│   ├── menu/                       MenuScreen
│   ├── solicitud/
│   │   ├── ListaSolicitudesScreen.kt
│   │   ├── SolicitudFormScreen.kt  Formulario (crear / editar) + cámara
│   │   ├── SolicitudUiState.kt
│   │   └── SolicitudViewModel.kt   Estado, validaciones y guardado en Room
│   ├── navigation/
│   │   └── AppNavigation.kt        NavHost: login → menu → lista → formulario
│   └── theme/
└── MainActivity.kt
```

## Cumplimiento de los lineamientos

| Criterio | Dónde se ve |
|---|---|
| Navegación | `MainActivity` → `AppNavigation` (`NavHost`) |
| Secuencia | Login → Menú → `ListaSolicitudesScreen` → `SolicitudFormScreen` |
| ListaSolicitudesScreen | Muestra código, título, categoría, estado y fecha de cada solicitud guardada |
| SolicitudFormScreen | Crea una solicitud nueva o edita una existente (al tocarla en la lista) |
| Formulario | Título, descripción, categoría (lista desplegable), servicio relacionado y foto |
| Validación | `validarFormulario()` en el ViewModel; errores con `isError` y `supportingText` |
| Gestión de estado | `SolicitudUiState` + `MutableStateFlow` / `StateFlow` |
| MVVM | Screen → `SolicitudViewModel` → `SolicitudRepository` → `SolicitudDao` → Room |
| Room | La solicitud queda guardada y sigue disponible al reiniciar la app |
| Reactividad | El DAO devuelve `Flow`: la lista se actualiza sola al registrar o editar |
| Recurso nativo | Cámara: la foto queda asociada a la solicitud y se ve al consultarla o editarla |

**Validaciones:** título obligatorio · descripción obligatoria con mínimo 10 caracteres ·
categoría obligatoria · servicio relacionado obligatorio.

**Al registrar:** se genera un código único (`SOL-XXXXXXXX`), el estado inicial es **Recibida** y
se guarda la fecha y hora.

## Cámara

1. En el formulario, **Tomar foto** crea un archivo vacío en la carpeta privada de la app
   (`files/evidencias/`) y obtiene su Uri con `FileProvider` (`res/xml/file_paths.xml`).
2. `rememberLauncherForActivityResult(ActivityResultContracts.TakePicture())` abre la cámara y
   guarda la foto en esa Uri.
3. La Uri se guarda en el `SolicitudUiState` (`evidenciaUri`) y, al guardar, en Room junto a la
   solicitud.
4. Al abrir la solicitud desde la lista, la foto se muestra en el formulario.

No se pide el permiso `CAMERA`: la foto la toma la app de cámara del sistema.
