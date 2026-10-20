# AppSolicitudes_Grupo8

App Android del **caso DSY1105-005D** (Duoc UC): *Gestión móvil y seguimiento de solicitudes de
clientes*. Implementa el **módulo de Solicitudes**: registrar, listar, consultar y editar
solicitudes, con una imagen de evidencia tomada con la cámara o elegida desde la galería.

Todos los datos son **ficticios** (solicitudes, fotos y usuario de prueba).

## Integrantes

- Bastian Alarcon
- Ignacio Hidalgo

Planificación en Trello: https://trello.com/b/a03BejlX/dsy1105-app-solicitudes-ev-parcial-2

### Distribución de tareas

| Integrante | Tareas |
|---|---|
| Bastian Alarcon | Configuración del proyecto, persistencia con Room, repositorio y SolicitudViewModel, cámara, navegación |
| Ignacio Hidalgo | Login con validaciones, menú principal, lista de solicitudes, formulario de solicitud, galería |

## Tecnologías

Kotlin · Jetpack Compose · Material 3 · MVVM (ViewModel + UiState + StateFlow) ·
Navigation Compose · Room · Cámara (`TakePicture` + `FileProvider`) · Galería (`OpenDocument`) ·
Animaciones de Compose.

## Funcionalidades

- **Login** con usuario de prueba y validación de campos.
- **Menú principal** para ir a la lista, crear una solicitud o cerrar sesión.
- **Lista de solicitudes** guardadas en Room: código, título, categoría, estado y fecha.
- **Registro de solicitud**: título, descripción, categoría y servicio relacionado. Se genera un
  código único (`SOL-XXXXXXXX`), el estado inicial es **Recibida** y se guarda la fecha y hora.
- **Consulta y edición**: al tocar una solicitud se abre el formulario con sus datos y su foto.
- **Evidencia ficticia**: foto con la **cámara** o imagen de la **galería**, guardada con la
  solicitud.
- **Validaciones** con ícono y mensaje de error en cada campo.
- **Animaciones** en la navegación, la lista y la foto de evidencia.

## Cómo ejecutar

1. Clonar el repositorio y abrir la carpeta en Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar en un emulador o celular con Android 8.0 (API 26) o superior.
4. Ingresar con el usuario de prueba **demo** / clave **1234**.

## Pruebas realizadas

Probadas en emulador Android (Medium Phone, API 37).

| # | Prueba | Resultado esperado | Estado |
|---|---|---|---|
| 1 | Login con clave incorrecta | Se queda en el login y muestra el error | OK |
| 2 | Login con demo / 1234 | Entra al menú principal | OK |
| 3 | Guardar formulario vacío | Muestra 4 errores (uno por campo) | OK |
| 4 | Registrar solicitud válida | Aparece en la lista con código SOL- y estado Recibida | OK |
| 5 | Editar el título | Cambia en la lista y mantiene el mismo código | OK |
| 6 | Foto con la cámara | La foto se ve en el formulario | OK |
| 7 | Imagen de la galería | La imagen se ve en el formulario | OK |
| 8 | Cerrar y reabrir la app | Se mantienen las solicitudes y las imágenes | OK |

## Estructura (MVVM)

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
│   │   ├── SolicitudFormScreen.kt  Formulario (crear / editar) + cámara y galería
│   │   ├── SolicitudUiState.kt
│   │   └── SolicitudViewModel.kt   Estado, validaciones y guardado en Room
│   ├── navigation/
│   │   └── AppNavigation.kt        NavHost: login → menu → lista → formulario
│   └── theme/
└── MainActivity.kt
```

Flujo de datos: Screen → `SolicitudViewModel` → `SolicitudRepository` → `SolicitudDao` → Room.
Las pantallas solo muestran el estado y envían eventos; las validaciones están en
`validarFormulario()` de cada ViewModel.

## Validaciones

| Formulario | Campo | Regla |
|---|---|---|
| Login | Usuario | Obligatorio |
| Login | Clave | Obligatoria y debe coincidir con el usuario de prueba |
| Solicitud | Título | Obligatorio |
| Solicitud | Descripción | Obligatoria, mínimo 10 caracteres |
| Solicitud | Categoría | Obligatoria |
| Solicitud | Servicio relacionado | Obligatorio |

Cada campo con error se marca en rojo (`isError`), muestra un ícono de error y el mensaje debajo
(`supportingText`). El error se borra al volver a escribir.

## Recursos nativos

1. **Cámara**: el botón *Cámara* crea un archivo en la carpeta privada de la app
   (`files/evidencias/`), obtiene su Uri con `FileProvider` (`res/xml/file_paths.xml`) y abre la
   cámara con `ActivityResultContracts.TakePicture()`. No se pide el permiso `CAMERA` porque la
   foto la toma la app de cámara del sistema.
2. **Galería**: el botón *Galería* abre el selector de imágenes del dispositivo con
   `ActivityResultContracts.OpenDocument()`. Se toma un permiso de lectura permanente
   (`takePersistableUriPermission`) para seguir viendo la imagen al reiniciar la app.

En ambos casos la Uri se guarda en Room junto a la solicitud y la imagen se muestra al consultarla
o editarla.

## Animaciones

| Dónde | Animación | Archivo |
|---|---|---|
| Cambio de pantalla | Deslizamiento + desvanecido (`slideInHorizontally` + `fadeIn`) | `AppNavigation.kt` |
| Lista de solicitudes | Las tarjetas aparecen y se reacomodan (`Modifier.animateItem()`) | `ListaSolicitudesScreen.kt` |
| Foto de evidencia | Aparece con desvanecido y expansión (`AnimatedVisibility`) | `SolicitudFormScreen.kt` |
