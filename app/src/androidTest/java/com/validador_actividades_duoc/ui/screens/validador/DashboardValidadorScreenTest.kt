package com.validador_actividades_duoc.ui.screens.validador

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
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento
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

class DashboardValidadorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var reservaRepository: ReservaRepository
    private lateinit var inventarioRepository: InventarioRepository
    private lateinit var validadorDigitales: UsuarioEntity
    private lateinit var viewModel: ValidadorViewModel
    private var solicitanteId: Long = 0L

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
            val validadorId = db.usuarioDao().insertarUsuario(
                UsuarioEntity(
                    correo = "digitales@duoc.cl",
                    nombre = "Validador Servicios Digitales",
                    rol = RolUsuario.VALIDADOR,
                    departamento = Departamento.DIGITALES
                )
            )
            validadorDigitales = db.usuarioDao().obtenerUsuarioPorId(validadorId)!!

            solicitanteId = db.usuarioDao().insertarUsuario(
                UsuarioEntity(
                    correo = "profesor@duoc.cl",
                    nombre = "Profesor Demo",
                    rol = RolUsuario.SOLICITANTE
                )
            )
        }

        viewModel = ValidadorViewModel(
            reservaRepository = reservaRepository,
            inventarioRepository = inventarioRepository,
            validador = validadorDigitales
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun renderizaEstadoVacioYNavegacionAInventarioYCerrarSesion() {
        var irAInventario = false
        var sesionCerrada = false

        composeTestRule.setContent {
            DuocTheme {
                DashboardValidadorScreen(
                    viewModel = viewModel,
                    onCerrarSesion = { sesionCerrada = true },
                    onIrAInventarioClick = { irAInventario = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Panel de validación").assertIsDisplayed()
        composeTestRule.onNodeWithText(Departamento.DIGITALES.etiqueta).assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pendientes por revisar").assertIsDisplayed()
        composeTestRule.onNodeWithText("No tienes solicitudes pendientes.").assertIsDisplayed()

        composeTestRule.onNodeWithText("Gestionar inventario").performClick()
        assertTrue(irAInventario)

        composeTestRule.onNodeWithContentDescription("Cerrar sesión").performClick()
        assertTrue(sesionCerrada)
    }

    @Test
    fun muestraSoloItemsDeSuDepartamentoYAprobarActualizaEstado() {
        var reservaId = 0L

        runBlocking {
            val microId = db.inventarioDao().insertarOActualizarRecurso(
                RecursoInventarioEntity(
                    departamento = Departamento.DIGITALES,
                    nombreItem = "Micrófono inalámbrico",
                    stockTotal = 6
                )
            )
            val guardiaId = db.inventarioDao().insertarOActualizarRecurso(
                RecursoInventarioEntity(
                    departamento = Departamento.PORTERIA,
                    nombreItem = "Guardia de seguridad",
                    stockTotal = 4
                )
            )

            reservaId = reservaRepository.crearReserva(
                ReservaEntity(
                    solicitanteId = solicitanteId,
                    nombreActividad = "Seminario de Ingeniería",
                    fecha = LocalDate.now().plusDays(10).toString(),
                    bloqueInicio = 2,
                    bloqueFin = 4
                ),
                items = mapOf(microId to 2, guardiaId to 1)
            ).getOrThrow()
        }

        composeTestRule.setContent {
            DuocTheme {
                DashboardValidadorScreen(
                    viewModel = viewModel,
                    onCerrarSesion = {},
                    onIrAInventarioClick = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.solicitudes.value.isNotEmpty()
        }

        composeTestRule.onNodeWithText("Seminario de Ingeniería").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solicita: Profesor Demo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Micrófono inalámbrico").assertIsDisplayed()
        composeTestRule.onNodeWithText("×2").assertIsDisplayed()

        composeTestRule.onNodeWithText("Guardia de seguridad").assertDoesNotExist()

        composeTestRule.onNodeWithText("Aprobar").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.solicitudes.value.isEmpty()
        }

        composeTestRule.onNodeWithText("No tienes solicitudes pendientes.").assertIsDisplayed()

        val actualizada = runBlocking { db.reservaDao().obtenerPorId(reservaId)!! }
        val reqDigitales = actualizada.requerimientos.first { it.recurso.departamento == Departamento.DIGITALES }
        val reqPorteria = actualizada.requerimientos.first { it.recurso.departamento == Departamento.PORTERIA }

        assertEquals(EstadoRequerimiento.APROBADO, reqDigitales.requerimiento.estadoValidacion)
        assertEquals(EstadoRequerimiento.PENDIENTE, reqPorteria.requerimiento.estadoValidacion)
        assertEquals(EstadoReserva.PENDIENTE, actualizada.reserva.estado)
    }

    @Test
    fun rechazarSolicitud_exigeMotivoEnDialogoYRegistraRechazo() {
        var reservaId = 0L

        runBlocking {
            val proyectorId = db.inventarioDao().insertarOActualizarRecurso(
                RecursoInventarioEntity(
                    departamento = Departamento.DIGITALES,
                    nombreItem = "Proyector",
                    stockTotal = 3
                )
            )

            reservaId = reservaRepository.crearReserva(
                ReservaEntity(
                    solicitanteId = solicitanteId,
                    nombreActividad = "Taller de Robótica",
                    fecha = LocalDate.now().plusDays(10).toString(),
                    bloqueInicio = 5,
                    bloqueFin = 6
                ),
                items = mapOf(proyectorId to 1)
            ).getOrThrow()
        }

        composeTestRule.setContent {
            DuocTheme {
                DashboardValidadorScreen(
                    viewModel = viewModel,
                    onCerrarSesion = {},
                    onIrAInventarioClick = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.solicitudes.value.isNotEmpty()
        }

        composeTestRule.onNodeWithText("Rechazar").performClick()

        composeTestRule.onNodeWithText("Rechazar solicitud").assertIsDisplayed()

        val botonConfirmarDialogo = composeTestRule.onNode(
            hasText("Rechazar") and hasAnyAncestor(isDialog())
        )
        botonConfirmarDialogo.assertIsNotEnabled()

        composeTestRule.onNodeWithText("Motivo del rechazo")
            .performTextInput("Equipos en mantención")

        botonConfirmarDialogo
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.solicitudes.value.isEmpty()
        }

        val actualizada = runBlocking { db.reservaDao().obtenerPorId(reservaId)!! }
        val reqDigitales = actualizada.requerimientos.first().requerimiento

        assertEquals(EstadoRequerimiento.RECHAZADO, reqDigitales.estadoValidacion)
        assertEquals("Equipos en mantención", reqDigitales.motivoRechazo)
        assertEquals(EstadoReserva.PENDIENTE_ADMIN, actualizada.reserva.estado)
    }
}