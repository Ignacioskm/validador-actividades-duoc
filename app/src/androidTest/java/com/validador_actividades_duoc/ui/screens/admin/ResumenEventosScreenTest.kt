package com.validador_actividades_duoc.ui.screens.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.remote.RetrofitInstance
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.theme.DuocTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean

class ResumenEventosScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var reservaRepository: ReservaRepository
    private lateinit var inventarioRepository: InventarioRepository
    private lateinit var adminViewModel: AdminViewModel
    private lateinit var urlOriginal: String
    private var solicitanteId: Long = 0L
    private var validadorId: Long = 0L
    private var recursoId: Long = 0L

    @Before
    fun setUp() {
        urlOriginal = RetrofitInstance.getBaseUrl()
        RetrofitInstance.setBaseUrl("http://127.0.0.1:59999/")

        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        reservaRepository = ReservaRepository(db)
        inventarioRepository = InventarioRepository(db.inventarioDao())

        runBlocking {
            solicitanteId = db.usuarioDao().insertarUsuario(
                UsuarioEntity(
                    correo = "profesor@duoc.cl",
                    nombre = "Profesor Demo",
                    rol = RolUsuario.SOLICITANTE
                )
            )
            validadorId = db.usuarioDao().insertarUsuario(
                UsuarioEntity(
                    correo = "digitales@duoc.cl",
                    nombre = "Validador Servicios Digitales",
                    rol = RolUsuario.VALIDADOR,
                    departamento = Departamento.DIGITALES
                )
            )
            recursoId = db.inventarioDao().insertarOActualizarRecurso(
                RecursoInventarioEntity(
                    departamento = Departamento.DIGITALES,
                    nombreItem = "Proyector HDMI",
                    stockTotal = 4
                )
            )
        }

        adminViewModel = AdminViewModel(
            reservaRepository = reservaRepository,
            inventarioRepository = inventarioRepository
        )
    }

    @After
    fun tearDown() {
        db.close()
        RetrofitInstance.setBaseUrl(urlOriginal)
    }

    @Test
    fun renderizaEncabezadoPestanasVaciasYCerrarSesion() {
        var sesionCerrada = false

        composeTestRule.setContent {
            val navController = rememberNavController()
            DuocTheme {
                ResumenEventosScreen(
                    navController = navController,
                    onCerrarSesion = { sesionCerrada = true },
                    viewModel = adminViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Panel Administrador").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gestión General Duoc UC").assertIsDisplayed()
        composeTestRule.onNodeWithText("Por Confirmar (0)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Agenda (0)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Inventario Global").assertIsDisplayed()
        composeTestRule.onNodeWithText("No hay solicitudes pendientes de validación final.")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Cerrar sesión").performClick()
        assertTrue(sesionCerrada)
    }

    @Test
    fun confirmarSolicitudAprobada_laMueveAAgendaYPermiteFiltrarEnBuscador() {
        var reservaId = 0L

        runBlocking {
            reservaId = reservaRepository.crearReserva(
                ReservaEntity(
                    solicitanteId = solicitanteId,
                    nombreActividad = "Congreso de Tecnología",
                    fecha = LocalDate.now().plusDays(10).toString(),
                    bloqueInicio = 2,
                    bloqueFin = 3
                ),
                items = mapOf(recursoId to 1)
            ).getOrThrow()

            reservaRepository.resolverArea(
                reservaId = reservaId,
                departamento = Departamento.DIGITALES,
                aprobar = true,
                motivo = null,
                validadorId = validadorId
            ).getOrThrow()
        }

        composeTestRule.setContent {
            val navController = rememberNavController()
            DuocTheme {
                ResumenEventosScreen(
                    navController = navController,
                    onCerrarSesion = {},
                    viewModel = adminViewModel
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            adminViewModel.solicitudesPendientesAdmin.value.isNotEmpty()
        }

        composeTestRule.onNodeWithText("Congreso de Tecnología").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirmar Actividad").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            adminViewModel.eventosConfirmados.value.isNotEmpty()
        }

        val reservaConfirmada = runBlocking { db.reservaDao().obtenerPorId(reservaId)!! }
        assertEquals(EstadoReserva.CONFIRMADA, reservaConfirmada.reserva.estado)

        composeTestRule.onNodeWithText("Agenda (1)").performClick()
        composeTestRule.onNodeWithText("Congreso de Tecnología").assertIsDisplayed()

        composeTestRule.onNodeWithText("Buscar actividad o profesor...")
            .performTextInput("NoExiste")
        composeTestRule.onNodeWithText("No hay eventos confirmados.").assertIsDisplayed()
    }

    @Test
    fun rechazarSolicitudDesdeAdmin_exigeMotivoYMarcaComoRechazada() {
        var reservaId = 0L

        runBlocking {
            reservaId = reservaRepository.crearReserva(
                ReservaEntity(
                    solicitanteId = solicitanteId,
                    nombreActividad = "Charla Externa",
                    fecha = LocalDate.now().plusDays(10).toString(),
                    bloqueInicio = 4,
                    bloqueFin = 5
                ),
                items = mapOf(recursoId to 1)
            ).getOrThrow()

            reservaRepository.resolverArea(
                reservaId = reservaId,
                departamento = Departamento.DIGITALES,
                aprobar = true,
                motivo = null,
                validadorId = validadorId
            ).getOrThrow()
        }

        composeTestRule.setContent {
            val navController = rememberNavController()
            DuocTheme {
                ResumenEventosScreen(
                    navController = navController,
                    onCerrarSesion = {},
                    viewModel = adminViewModel
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            adminViewModel.solicitudesPendientesAdmin.value.isNotEmpty()
        }

        composeTestRule.onNodeWithText("Rechazar").performClick()
        composeTestRule.onNodeWithText("Rechazar Actividad").assertIsDisplayed()

        val botonConfirmarRechazo = composeTestRule.onNode(
            hasText("Rechazar") and hasAnyAncestor(isDialog())
        )
        botonConfirmarRechazo.assertIsNotEnabled()

        composeTestRule.onNodeWithText("Motivo del rechazo general")
            .performTextInput("Sin cupo institucional")

        botonConfirmarRechazo
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            adminViewModel.solicitudesPendientesAdmin.value.isEmpty()
        }

        val actualizada = runBlocking { db.reservaDao().obtenerPorId(reservaId)!! }
        assertEquals(EstadoReserva.RECHAZADA, actualizada.reserva.estado)
        assertEquals("Sin cupo institucional", actualizada.reserva.motivoRechazo)
    }

    @Test
    fun pestanaInventarioGlobal_muestraErrorYBotonReintentarSiSinConexion() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            DuocTheme {
                ResumenEventosScreen(
                    navController = navController,
                    onCerrarSesion = {},
                    viewModel = adminViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Inventario Global").performClick()

        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            !adminViewModel.cargandoInventario.value &&
                    adminViewModel.errorInventario.value != null
        }

        composeTestRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test
    fun pestanaInventarioGlobal_cargaListaPermiteEditarStockYCrearRecurso() {
        val activo = AtomicBoolean(true)
        val server = ServerSocket(0)
        val puerto = server.localPort
        RetrofitInstance.setBaseUrl("http://127.0.0.1:$puerto/")

        val hiloServidor = Thread {
            while (activo.get() && !server.isClosed) {
                try {
                    val socket = server.accept()
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
                    val requestLine = reader.readLine() ?: ""
                    var contentLength = 0
                    var header: String?
                    while (reader.readLine().also { header = it } != null && header!!.isNotEmpty()) {
                        if (header!!.lowercase().startsWith("content-length:")) {
                            contentLength = header!!.substringAfter(":").trim().toIntOrNull() ?: 0
                        }
                    }
                    if (contentLength > 0) {
                        val buffer = CharArray(contentLength)
                        reader.read(buffer, 0, contentLength)
                    }

                    val jsonBody = when {
                        requestLine.startsWith("PATCH ") ->
                            """{"id":1,"departamento":"DIGITALES","nombreItem":"Proyector HDMI","stockTotal":10,"activo":true}"""
                        requestLine.startsWith("POST ") ->
                            """{"id":2,"departamento":"DIGITALES","nombreItem":"Parlante Portátil","stockTotal":3,"activo":true}"""
                        else ->
                            """[{"id":1,"departamento":"DIGITALES","nombreItem":"Proyector HDMI","stockTotal":4,"activo":true}]"""
                    }

                    val bytes = jsonBody.toByteArray(StandardCharsets.UTF_8)
                    val responseHeaders =
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json; charset=utf-8\r\n" +
                                "Content-Length: ${bytes.size}\r\n" +
                                "Connection: close\r\n\r\n"

                    val out = socket.getOutputStream()
                    out.write(responseHeaders.toByteArray(StandardCharsets.UTF_8))
                    out.write(bytes)
                    out.flush()
                    socket.close()
                } catch (_: Exception) {
                }
            }
        }.apply { isDaemon = true; start() }

        try {
            val vmConRed = AdminViewModel(
                reservaRepository = reservaRepository,
                inventarioRepository = inventarioRepository
            )

            composeTestRule.setContent {
                val navController = rememberNavController()
                DuocTheme {
                    ResumenEventosScreen(
                        navController = navController,
                        onCerrarSesion = {},
                        viewModel = vmConRed
                    )
                }
            }

            composeTestRule.onNodeWithText("Inventario Global").performClick()

            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                vmConRed.inventarioGlobal.value.isNotEmpty()
            }

            composeTestRule.onNodeWithText("Proyector HDMI").assertIsDisplayed()
            composeTestRule.onNodeWithText("Stock total: 4 unid.").assertIsDisplayed()

            composeTestRule.onNodeWithContentDescription("Editar Stock").performClick()
            composeTestRule.onNodeWithText("Nuevo stock total").performTextClearance()
            composeTestRule.onNodeWithText("Nuevo stock total").performTextInput("10")
            composeTestRule.onNodeWithText("Guardar").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Agregar recurso", useUnmergedTree = true).performClick()
            composeTestRule.onNodeWithText("Agregar Recurso a Departamento").assertIsDisplayed()

            composeTestRule.onNodeWithText(Departamento.AUXILIARES.etiqueta).performClick()
            composeTestRule.onAllNodesWithText(Departamento.DIGITALES.etiqueta).onLast().performClick()

            composeTestRule.onNodeWithText("Nombre del ítem").performTextInput("Parlante Portátil")
            composeTestRule.onNodeWithText("Stock inicial").performTextClearance()
            composeTestRule.onNodeWithText("Stock inicial").performTextInput("3")
            composeTestRule.onNodeWithText("Crear Recurso").performClick()
            composeTestRule.waitForIdle()
        } finally {
            activo.set(false)
            server.close()
            hiloServidor.interrupt()
        }
    }

    @Test
    fun validarExcepcionalmente_abreDialogoYConfirmaReserva() {
        var reservaId = 0L

        runBlocking {
            reservaId = reservaRepository.crearReserva(
                ReservaEntity(
                    solicitanteId = solicitanteId,
                    nombreActividad = "Seminario Sin Validar Todo",
                    fecha = LocalDate.now().plusDays(12).toString(),
                    bloqueInicio = 1,
                    bloqueFin = 2
                ),
                items = mapOf(recursoId to 1)
            ).getOrThrow()

            reservaRepository.resolverArea(
                reservaId = reservaId,
                departamento = Departamento.DIGITALES,
                aprobar = true,
                motivo = null,
                validadorId = validadorId
            ).getOrThrow()
        }

        composeTestRule.setContent {
            val navController = rememberNavController()
            DuocTheme {
                ResumenEventosScreen(
                    navController = navController,
                    onCerrarSesion = {},
                    viewModel = adminViewModel
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            adminViewModel.solicitudesPendientesAdmin.value.isNotEmpty()
        }

        composeTestRule.onNodeWithText("Validar excepcionalmente")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Confirmación excepcional").assertIsDisplayed()

        composeTestRule.onNode(
            hasText("Cancelar") and hasAnyAncestor(isDialog())
        ).performClick()

        composeTestRule.onNodeWithText("Validar excepcionalmente")
            .performScrollTo()
            .performClick()

        composeTestRule.onNode(
            hasText("Sí, validar") and hasAnyAncestor(isDialog())
        ).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            adminViewModel.solicitudesPendientesAdmin.value.isEmpty()
        }

        val actualizada = runBlocking { db.reservaDao().obtenerPorId(reservaId)!! }
        assertEquals(EstadoReserva.CONFIRMADA, actualizada.reserva.estado)
    }

    @Test
    fun botonReservar_abreCalendarioAdminYPermiteVolverAlPanel() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            DuocTheme {
                ResumenEventosScreen(
                    navController = navController,
                    onCerrarSesion = {},
                    viewModel = adminViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Reservar").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Panel Administrador").fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Cerrar sesión").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Panel Administrador").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Panel Administrador").assertIsDisplayed()
    }
}