# AppSolicitudes_GrupoX

App Android para el **caso semestral DSY1105-005D** (Duoc UC): *Gestión móvil y seguimiento de
solicitudes de clientes*. Permite registrar, clasificar, asignar, atender y cerrar solicitudes que
llegan por WhatsApp, con trazabilidad completa y control de acceso por perfil.

> Nombre provisional: reemplazar `X` por el número de grupo en `settings.gradle.kts`
> (`rootProject.name`) y en `app/src/main/res/values/strings.xml` cuando se asigne.

Todos los datos son **ficticios**: no hay nombres, correos, teléfonos ni conversaciones reales, y
no hay integración real con WhatsApp (solo se abre la app con un mensaje ya escrito).

## Tecnologías

Kotlin · Jetpack Compose · Material 3 · MVVM (ViewModel + StateFlow) · Navigation Compose ·
Room (SQLite) · Window Size Classes · ActivityResult (galería y cámara) + FileProvider ·
Notificaciones locales · JUnit.

Funciona **sin backend**: toda la información vive en Room y la app carga datos de prueba la
primera vez que se abre.

## Cómo ejecutar

1. Abrir la carpeta del proyecto en Android Studio y esperar la sincronización de Gradle.
2. Ejecutar en un emulador o celular con Android 8.0 (API 26) o superior.
3. Desde la terminal también se puede: `gradlew assembleDebug`.

### Cuentas de prueba

En el login hay botones de acceso rápido que completan correo y clave.

| Perfil | Correo |
|---|---|
| Cliente | `ana.prueba@demo.test`, `bruno.ejemplo@demo.test`, `carla.muestra@demo.test` |
| Administrador | `coordinacion@demo.test` |
| Técnico | `tecnico.esteban@demo.test`, `tecnico.fernanda@demo.test` |

La clave de prueba (la misma para todas) está en `data/DatosSemilla.kt` (`CLAVE_DEMO`).
En la base de datos solo se guarda su hash SHA-256.

## Estructura

```
app/src/main/java/com/example/solicitudes/
├── model/          Entidades (Room), enums y reglas de negocio puras:
│                   FlujoSolicitud (estados), ReglasAcceso (permisos), ReglasSolicitud
│                   (código y atrasos), ReglasEvidencia, FiltrosSolicitud, ResumenSolicitudes
│                   + UiState / Errores de cada pantalla (patrón guía 11)
├── data/           AppDatabase, DAOs, convertidores y DatosSemilla (datos ficticios)
├── repository/     SolicitudesRepository (única puerta a los datos + historial),
│                   SesionRepository, BorradorRepository, Credenciales
├── viewmodel/      Un ViewModel por pantalla + AppViewModelProvider (fábrica)
├── navigation/     Screen (sealed class), NavigationEvent y AppNavigation (guía 10)
├── notificaciones/ Notificador (avisos locales)
└── ui/
    ├── screen/     login, cliente, admin, tecnico, detalle
    ├── components/ EstadoChip, SolicitudCard, HistorialTimeline, AccionDialog, filtros…
    ├── theme/      Tema Material 3 y colores de cada estado
    └── utils/      WindowSizeUtils (guía 9), ArchivosEvidencia (guía 13), WhatsApp, fechas
```

**Regla de oro:** toda modificación de una solicitud pasa por `SolicitudesRepository.ejecutarAccion()`,
que valida el permiso del perfil, calcula el nuevo estado con `FlujoSolicitud` y registra el
evento en el historial (fecha, usuario, perfil, acción y comentario).

## Flujo de estados

```
Recibida → En revisión → Asignada → En proceso → Resuelta → Cerrada
   │            │            │           │           │          │
   └────────────┴──► Pendiente de información ◄──────┘          │
                       (el cliente responde y vuelve)            │
   Cancelada (admin, con motivo)          Reapertura (cliente) ──┘ → En revisión
```

| Perfil | Acciones principales |
|---|---|
| Cliente | Crear solicitud, adjuntar evidencia, compartir por WhatsApp, comentar, responder información, confirmar resolución, pedir reapertura |
| Administrador | Revisar, clasificar, priorizar, asignar/reasignar, pedir información, comentar (interno o visible), cerrar, cancelar, ver resumen |
| Técnico | Iniciar trabajo, registrar avance, comentar, adjuntar evidencia, pedir información, registrar solución |

## Material de clase aplicado

| Guía / unidad | Dónde está |
|---|---|
| Guía 8 · Pantalla base | `Scaffold`, `TopAppBar` (`BarraSuperior`), tarjetas y listas en `ui/` |
| Guía 9 · Adaptabilidad | `ui/utils/WindowSizeUtils.kt`, `MainActivity` (`calculateWindowSizeClass`), listas de 1-3 columnas |
| Guía 10 · Navegación | `navigation/` + `MainViewModel` (eventos con `SharedFlow`), `NavigationBar` del admin |
| Guía 11 · Formularios | `LoginUiState`/`LoginErrores`, `NuevaSolicitudUiState`/`NuevaSolicitudErrores`, `validarFormulario()` |
| Guía 13 · Cámara y galería | `ui/components/SelectorEvidencia.kt`, `ui/utils/ArchivosEvidencia.kt`, `res/xml/file_paths.xml` |
| 2.5.1 · Persistencia | `data/` (Room) y `repository/` |

## Pruebas

- **Unitarias** (lógica de negocio, sin emulador): `gradlew testDebugUnitTest`
  Flujo de estados, permisos por perfil, atrasos, código único, evidencias, filtros, resumen y
  mensaje de WhatsApp.
- **Instrumentadas** (Room en memoria, requieren emulador o celular): `gradlew connectedDebugAndroidTest`
  Datos semilla, login, visibilidad por perfil, comentarios internos, flujo completo con historial,
  acciones no permitidas y coherencia del resumen con la base de datos.

## APK release firmado

1. Copiar `keystore.properties.example` como `keystore.properties` y completar los datos de la llave
   (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`).
2. Ejecutar `gradlew assembleRelease`. El APK queda en `app/build/outputs/apk/release/app-release.apk`.

`keystore.properties` y la carpeta `keystore/` **nunca se suben a git** (están en `.gitignore`).
Sin `keystore.properties` el release se genera sin firmar.

## Demostración sugerida (flujo completo, criterio 40)

1. **Cliente** (Ana): Nueva solicitud → validaciones → adjuntar evidencia → Enviar → código
   `SOL-AAAA-NNNN` → Compartir por WhatsApp.
2. **Administrador**: Bandeja → filtro *Nuevas* → abrir → Iniciar revisión → Definir prioridad →
   Asignar técnico → pestaña *Resumen* (cantidades por estado).
3. **Técnico** (Fernanda): Mis asignaciones → Iniciar trabajo → Registrar avance → Comentario
   interno → Registrar solución.
4. **Cliente**: ve el historial (sin el comentario interno) → Confirmar resolución → Cerrada.
   Opcional: Solicitar reapertura.

## Organización del equipo

Tablero Trello: *DSY1105 · App Seguimiento de Solicitudes* (Fase 0 común, Persona A: login +
cliente, Persona B: admin + técnico).
