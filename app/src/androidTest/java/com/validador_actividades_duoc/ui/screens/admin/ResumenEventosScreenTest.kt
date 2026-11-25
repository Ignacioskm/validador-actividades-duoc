package com.validador_actividades_duoc.ui.screens.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
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
import java.time.LocalDate

class ResumenEventosScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var reservaRepository: ReservaRepository
    private lateinit var inventarioRepository: InventarioRepository
    private lateinit var adminViewModel: AdminViewModel
    private var solicitanteId: Long = 0L
    private var validadorId: Long = 0L
    private var recursoId: Long = 0L

    @Before
    fun setUp() {
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
}