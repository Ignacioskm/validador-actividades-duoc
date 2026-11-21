# Validador de Actividades Duoc UC

Aplicación Android para gestionar la solicitud, validación y confirmación de actividades en Duoc UC, considerando los recursos de cada departamento involucrado. Incluye autenticación con Firebase (Google Sign-In) y consumo de una API remota para mostrar publicaciones.

## Flujo principal

1. Un **solicitante** crea una actividad indicando fecha, bloques horarios y recursos necesarios.
2. La solicitud se divide automáticamente por **departamento dueño de cada recurso**.
3. Cada **departamento** aprueba o rechaza únicamente sus recursos.
4. Cuando todas las áreas responden, el **administrador** confirma o rechaza la actividad.
5. Cada departamento gestiona el stock de sus recursos; el administrador lo hace de forma global.

> Proyecto académico. Funciona principalmente de forma **local en el dispositivo** (Room/SQLite), con autenticación vía Firebase y consumo opcional de API remota para publicaciones.

---

## Tabla de contenidos

- [Funcionalidades por rol](#funcionalidades-por-rol)
- [Novedades recientes](#novedades-recientes)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Modelo de datos local](#modelo-de-datos-local)
- [Modelo de datos remoto (Posts)](#modelo-de-datos-remoto-posts)
- [Requisitos](#requisitos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Datos de prueba](#datos-de-prueba)
- [Limitaciones](#limitaciones)

---

## Funcionalidades por rol

### Solicitante

- Inicio de sesión con correo institucional o Google (Firebase).
- Consulta de calendario y solicitudes propias.
- Creación de solicitud en 3 pasos:
  1. Nombre de actividad, fecha y rango de bloques horarios.
  2. Selección de recursos e insumos agrupados por departamento.
  3. Resumen, adjuntos y confirmación.
- Visualización de stock disponible por fecha y bloques.
- Adjuntar imágenes o documentos (PDF, DOC, DOCX; máximo 20 archivos, 5 MB cada uno).
- Notificaciones cuando la actividad es confirmada o rechazada.

### Validador de departamento

- Panel con solicitudes que involucran recursos de **su departamento**.
- Aprobación o rechazo de la petición (motivo obligatorio al rechazar).
- Acceso solo a ítems de su área.
- Gestión del inventario de su área: crear, editar stock y desactivar lógicamente recursos.
- Notificación al recibir nueva solicitud para su área.

### Administrador

- Panel general con tres pestañas:
  - **Por Confirmar:** solicitudes con respuesta de todas las áreas.
  - **Agenda:** actividades confirmadas con buscador.
  - **Inventario Global:** recursos de todos los departamentos.
- Confirmación o rechazo final de la actividad.
- Edición de stock y creación de recursos en cualquier departamento.
- Revalidación de stock antes de confirmar.
- Creación de solicitudes desde el panel administrativo.

---

## Novedades recientes

### Autenticación con Firebase (Google Sign-In)

Se integra `FirebaseAuthRepository` para iniciar sesión y cerrar sesión usando Google Sign-In a través de `Credential Manager` y `Google ID Token`. La tabla de usuarios incluye la columna `firebaseUid` para vincular cuentas de Firebase con registros locales.

### Consumo de API remota (Retrofit)

Se agregó una capa remota con Retrofit + Gson que consume `https://jsonplaceholder.typicode.com/posts` para obtener listado de publicaciones. Incluye:

- `ApiService` — interfaz con endpoint `@GET("/posts")`.
- `RetrofitInstance` — configuración del cliente Retrofit.
- `PostRepository` — repositorio que expone `getPosts()`.
- `PostViewModel` y `PostScreen` — vista y viewmodel que muestran los posts en tarjetas.

### Migración de base de datos

Se agregó `MIGRATION_3_4` que añade la columna `firebaseUid` a la tabla `usuarios` y crea un índice único sobre ella.

---

## Arquitectura

Arquitectura por capas inspirada en MVVM con flujo unidireccional vía `StateFlow`:

```text
Compose UI → ViewModel → Repository → { Room DAO → SQLite | Retrofit → API remota }
```

- **UI:** Jetpack Compose + Material 3, pantallas organizadas por rol y funcionalidad.
- **Presentación:** ViewModels por rol/flujo (`AuthViewModel`, `SolicitanteViewModel`, `ValidadorViewModel`, `AdminViewModel`, `PostViewModel`).
- **Dominio:** modelos de datos, reglas de negocio y bloques horarios.
- **Datos locales:** repositorios que encapsulan transacciones y DAOs de Room.
- **Datos remotos:** Retrofit + Gson para consumo de API externa.
- **Autenticación:** Firebase Auth + Google Sign-In mediante `Credential Manager`.
- **Inyección de dependencias:** manual mediante `AppContainer`.
- **Navegación:** Navigation Compose con rutas por rol.

---

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Persistencia local | Room 2.8.4 (KSP) |
| Base de datos | SQLite |
| Asincronía | Coroutines + Flow |
| Red / API | Retrofit 2.11.0 + Gson Converter |
| Autenticación | Firebase Auth, Google Sign-In, Credential Manager |
| Build | Gradle + Android Gradle Plugin |
| SDK | `minSdk 30`, `targetSdk 37`, `compileSdk 37` |
| Java | Java 11 |

---

## Estructura del proyecto

```text
validador-actividades-duoc/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/validador_actividades_duoc/
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── AdjuntoStorage.kt
│       │   │   │   ├── local/
│       │   │   │   │   ├── AppContainer.kt
│       │   │   │   │   ├── AppDataBase.kt
│       │   │   │   │   ├── Converters.kt
│       │   │   │   │   ├── dao/
│       │   │   │   │   └── entity/
│       │   │   │   ├── remote/
│       │   │   │   │   ├── ApiService.kt
│       │   │   │   │   └── RetrofitInstance.kt
│       │   │   │   └── repository/
│       │   │   │       ├── FireBaseAuthRepository.kt
│       │   │   │       ├── InventarioRepository.kt
│       │   │   │       ├── PostRepository.kt
│       │   │   │       └── ReservaRepository.kt
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   │   ├── Adjuntos.kt
│       │   │   │   │   ├── Departamento.kt
│       │   │   │   │   ├── Estados.kt
│       │   │   │   │   ├── Post.kt
│       │   │   │   │   └── RolUsuario.kt
│       │   │   │   └── util/
│       │   │   │       ├── BloqueHorario.kt
│       │   │   │       └── ReglasNegocio.kt
│       │   │   ├── notifications/
│       │   │   │   ├── NotificacionesHelper.kt
│       │   │   │   └── NotificadorEventos.kt
│       │   │   └── ui/
│       │   │       ├── components/
│       │   │   │   ├── navigation/
│       │   │   │   │   └── AppNavigation.kt
│       │   │   │   ├── screens/
│       │   │   │   │   ├── admin/
│       │   │   │   │   ├── auth/
│       │   │   │   │   ├── posts/
│       │   │   │   │   │   ├── PostScreen.kt
│       │   │   │   │   │   └── PostViewModel.kt
│       │   │   │   │   ├── solicitante/
│       │   │   │   │   └── validador/
│       │   │   │   └── theme/
│       │   │   └── res/
│       │   ├── test/
│       │   └── androidTest/
│       ├── gradle/
│       │   └── libs.versions.toml
│       ├── build.gradle.kts
│       ├── settings.gradle.kts
│       ├── gradle.properties
│       └── README.md
```

---

## Modelo de datos local

Base de datos `validador_duoc_db` con Room y cinco entidades:

| Tabla | Campos principales | Descripción |
|---|---|---|
| `usuarios` | `id`, `correo`, `nombre`, `rol`, `departamento`, `firebaseUid` | Usuarios de la app. `firebaseUid` vincula con Firebase Auth. |
| `reservas` | `id`, `solicitanteId`, `nombreActividad`, `fecha`, `bloqueInicio`, `bloqueFin`, `estado`, `motivoRechazo`, `fechaCreacion` | Actividad solicitada. |
| `requerimientos` | `id`, `reservaId`, `recursoId`, `cantidadSolicitada`, `estadoValidacion`, `motivoRechazo`, `validadorId`, `fechaResolucion` | Recurso pedido y su validación por área. |
| `recursos_inventario` | `id`, `departamento`, `nombreItem`, `stockTotal`, `activo` | Catálogo de recursos por departamento. |
| `adjuntos` | `id`, `reservaId`, `tipo`, `nombreArchivo`, `rutaLocal`, `mimeType`, `tamanoBytes` | Archivos adjuntos de la solicitud. |

### Relaciones

```text
usuarios 1 ─── N reservas
reservas 1 ─── N requerimientos
reservas 1 ─── N adjuntos
recursos_inventario 1 ─── N requerimientos
```

---

## Modelo de datos remoto (Posts)

Entidad `Post` consumida desde `jsonplaceholder.typicode.com`:

| Campo | Tipo | Descripción |
|---|---|---|
| `userId` | Int | ID del usuario autor. |
| `id` | Int | ID del post. |
| `title` | String | Título del post. |
| `body` | String | Contenido del post. |

---

## Requisitos

- Android Studio con soporte para AGP 9.x.
- JDK 17 o superior.
- Android SDK 37.
- Emulador o dispositivo con Android 11 (API 30) o superior.
- `google-services.json` configurado en `app/` para Firebase.
- Conexión a internet para descargar dependencias y consumir la API remota.

---

## Instalación y ejecución

1. Clona el repositorio:

   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd validador-actividades-duoc
   ```

2. Abre el proyecto en Android Studio.

3. Sincroniza Gradle:

   ```bash
   # Linux / macOS
   ./gradlew build

   # Windows
   .\gradlew.bat build
   ```

4. Selecciona un emulador o dispositivo y presiona **Run ▶**.

5. En la primera ejecución se siembran automáticamente usuarios, recursos y una solicitud de ejemplo.

### Compilar APK de depuración

```bash
# Linux / macOS
./gradlew assembleDebug

# Windows
.\gradlew.bat assembleDebug
```

El APK se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Instalación por ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Datos de prueba

Inicio de sesión simplificado con correo institucional (sin contraseña). Si el correo no existe, se crea como `SOLICITANTE`.

| Rol | Correo | Departamento |
|---|---|---|
| Administrador | `admin@duoc.cl` | — |
| Solicitante | `profesor@duoc.cl` | — |
| Validador | `auxiliares@duoc.cl` | Servicios Auxiliares |
| Validador | `porteria@duoc.cl` | Portería y Accesos |
| Validador | `digitales@duoc.cl` | Servicios Digitales |
| Validador | `comunicacion@duoc.cl` | Comunicaciones |

También se puede autenticar con Google mediante Firebase.

### Recursos iniciales

| Departamento | Recursos |
|---|---|
| Servicios Auxiliares | Mesas plegables (20), Toldos (3) |
| Portería y Accesos | Guardias de seguridad (6), Estacionamiento para proveedor (5) |
| Servicios Digitales | Micrófonos inalámbricos (6), Proyectores (4), Computadores (3) |
| Comunicaciones | Registro de fotos y video (2), Difusión en RSS (5) |

Actividad de ejemplo: **“Encuentro de Innovación (DEMO)”**.

---

## Limitaciones

- Base de datos principalmente local; no hay sincronización en tiempo real entre dispositivos.
- Las notificaciones son locales y dependen de que la app esté en ejecución.
- Migraciones de Room básicas; en producción se requieren migraciones formales.
- No existe pantalla para administrar roles o departamentos desde la UI.
- Falta cobertura de pruebas unitarias de reglas de negocio y pruebas de interfaz.
- Los adjuntos se almacenan en almacenamiento privado de la app sin cifrado.
