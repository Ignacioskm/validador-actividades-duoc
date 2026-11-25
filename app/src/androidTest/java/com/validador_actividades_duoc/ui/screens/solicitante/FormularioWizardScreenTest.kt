package com.validador_actividades_duoc.ui.screens.solicitante

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.theme.DuocTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class FormularioWizardScreenTest {

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
            db.inventarioDao().insertarOActualizarRecurso(
                RecursoInventarioEntity(
                    departamento = Departamento.DIGITALES,
                    nombreItem = "Proyector HDMI",
                    stockTotal = 4
                )
            )
        }

        viewModel = SolicitanteViewModel(
            reservaRepository = ReservaRepository(db),
            inventarioRepository = InventarioRepository(db.inventarioDao())
        )
        viewModel.inicializarSolicitante(solicitanteId)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun paso1_bloqueaSiguienteSiNombreVacioOAnticipacionMenorA48Horas() {
        composeTestRule.setContent {
            DuocTheme {
                FormularioWizardScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Paso 1 de 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Campo obligatorio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Siguiente").assertIsNotEnabled()

        viewModel.prepararWizard(fecha = LocalDate.now(), bloque = 1)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Nombre de la Actividad")
            .performTextInput("Actividad Urgente")

        composeTestRule.onNodeWithText(
            "Reserva bloqueada. Debe solicitar las reservas con un mínimo de 48 horas de anticipación."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Siguiente").assertIsNotEnabled()
    }

    @Test
    fun flujoCompleto3Pasos_creaReservaEnRoomYRegresaAlCalendario() {
        var volvioAlCalendario = false

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.wizardState.value.recursosDisponibles.isNotEmpty()
        }

        composeTestRule.setContent {
            DuocTheme {
                FormularioWizardScreen(
                    viewModel = viewModel,
                    onVolverCalendario = { volvioAlCalendario = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Nombre de la Actividad")
            .performTextInput("Feria Tecnológica Duoc")

        composeTestRule.onNodeWithText("Siguiente")
            .assertIsEnabled()
            .performClick()

        composeTestRule.onNodeWithText("Paso 2 de 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Siguiente").assertIsNotEnabled()

        composeTestRule.onNodeWithText("DIGITALES").performClick()
        composeTestRule.onNodeWithContentDescription("Aumentar cantidad de Proyector HDMI")
            .performClick()

        composeTestRule.onNodeWithText("Siguiente")
            .assertIsEnabled()
            .performClick()

        composeTestRule.onNodeWithText("Paso 3 de 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Feria Tecnológica Duoc").assertIsDisplayed()
        composeTestRule.onNodeWithText("Proyector HDMI").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 unid.").assertIsDisplayed()

        composeTestRule.onNodeWithText("Enviar Solicitud").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.wizardState.value.guardadoExitoso
        }

        composeTestRule.onNodeWithText("Solicitud Registrada").assertIsDisplayed()
        composeTestRule.onNodeWithText("Volver al Inicio").performClick()

        assertTrue(volvioAlCalendario)

        val reservasGuardadas = runBlocking { db.reservaDao().observarTodas().first() }
        assertEquals(1, reservasGuardadas.size)
        assertEquals("Feria Tecnológica Duoc", reservasGuardadas.first().reserva.nombreActividad)
    }
}