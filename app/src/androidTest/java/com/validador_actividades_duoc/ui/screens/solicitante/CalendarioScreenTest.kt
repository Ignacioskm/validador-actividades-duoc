package com.validador_actividades_duoc.ui.screens.solicitante

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.theme.DuocTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalendarioScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var viewModel: SolicitanteViewModel
    private var solicitanteId: Long = 0L

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        runBlocking {
            solicitanteId = db.usuarioDao().insertarUsuario(
                UsuarioEntity(
                    correo = "profesor@duoc.cl",
                    nombre = "Profesor Demo",
                    rol = RolUsuario.SOLICITANTE
                )
            )
        }

        viewModel = SolicitanteViewModel(
            reservaRepository = ReservaRepository(db),
            inventarioRepository = InventarioRepository(db.inventarioDao())
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun renderizaEncabezadoDisponibilidadYEstadoVacioDeSolicitudes() {
        composeTestRule.setContent {
            DuocTheme {
                CalendarioScreen(
                    solicitanteId = solicitanteId,
                    nombreUsuario = "Profesor Demo",
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Hola, Profesor Demo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disponibilidad").assertIsDisplayed()
        composeTestRule.onNodeWithText("B1").assertIsDisplayed()

        composeTestRule.onNode(hasScrollToIndexAction())
            .performScrollToNode(hasText("No tienes solicitudes registradas"))

        composeTestRule.onNodeWithText("Mis Solicitudes (0)").assertIsDisplayed()
        composeTestRule.onNodeWithText("No tienes solicitudes registradas").assertIsDisplayed()
    }

    @Test
    fun clicEnBloqueLibreYFab_disparanNavegacionAlWizard() {
        var fechaRecibida: LocalDate? = null
        var bloqueRecibido: Int? = -1

        composeTestRule.setContent {
            DuocTheme {
                CalendarioScreen(
                    solicitanteId = solicitanteId,
                    nombreUsuario = "Profesor Demo",
                    viewModel = viewModel,
                    onAbrirWizard = { fecha, bloque ->
                        fechaRecibida = fecha
                        bloqueRecibido = bloque
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("B1").performClick()
        assertEquals(LocalDate.now(), fechaRecibida)
        assertEquals(1, bloqueRecibido)

        composeTestRule.onNodeWithText("Nueva Solicitud").performClick()
        assertEquals(LocalDate.now(), fechaRecibida)
        assertNull(bloqueRecibido)
    }

    @Test
    fun bloqueConReserva_muestraNombreActividadYBotonCerrarSesionFunciona() {
        var sesionCerrada = false

        runBlocking {
            db.reservaDao().insertarReserva(
                ReservaEntity(
                    solicitanteId = solicitanteId,
                    nombreActividad = "Charla de Innovación",
                    fecha = LocalDate.now().toString(),
                    bloqueInicio = 1,
                    bloqueFin = 2
                )
            )
        }

        composeTestRule.setContent {
            DuocTheme {
                CalendarioScreen(
                    solicitanteId = solicitanteId,
                    nombreUsuario = "Profesor Demo",
                    viewModel = viewModel,
                    onCerrarSesion = { sesionCerrada = true }
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.uiState.value.misSolicitudes.isNotEmpty()
        }

        composeTestRule.onNodeWithText("Charla de Innovación").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Cerrar sesión").performClick()
        assertTrue(sesionCerrada)
    }
}