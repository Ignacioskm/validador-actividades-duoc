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
import androidx.compose.ui.test.performTextClearance
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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GestionInventarioScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var viewModel: ValidadorViewModel

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        val validador = runBlocking {
            val id = db.usuarioDao().insertarUsuario(
                UsuarioEntity(
                    correo = "digitales@duoc.cl",
                    nombre = "Validador Servicios Digitales",
                    rol = RolUsuario.VALIDADOR,
                    departamento = Departamento.DIGITALES
                )
            )
            db.usuarioDao().obtenerUsuarioPorId(id)!!
        }

        viewModel = ValidadorViewModel(
            reservaRepository = ReservaRepository(db),
            inventarioRepository = InventarioRepository(db.inventarioDao()),
            validador = validador
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun renderizaEstadoVacioYBotonVolverFunciona() {
        var volvio = false

        composeTestRule.setContent {
            DuocTheme {
                GestionInventarioScreen(
                    viewModel = viewModel,
                    onVolver = { volvio = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Inventario del área").assertIsDisplayed()
        composeTestRule.onNodeWithText(Departamento.DIGITALES.etiqueta).assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recursos registrados").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aún no tienes recursos. Usa el botón + para agregar.")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Volver").performClick()
        assertTrue(volvio)
    }

    @Test
    fun agregarRecurso_validaCamposYLoRegistraEnInventario() {
        composeTestRule.setContent {
            DuocTheme {
                GestionInventarioScreen(
                    viewModel = viewModel,
                    onVolver = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Agregar recurso", useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithText("Nuevo recurso").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Nombre del recurso")
            .performTextInput("Notebook Dell")

        composeTestRule.onNodeWithText("Stock total")
            .performTextClearance()
        composeTestRule.onNodeWithText("Stock total")
            .performTextInput("5")

        composeTestRule.onNodeWithText("Guardar")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.inventario.value.size == 1
        }

        composeTestRule.onNodeWithText("Notebook Dell").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stock total: 5 unidades").assertIsDisplayed()
        assertEquals(5, viewModel.inventario.value.first().stockTotal)
    }

    @Test
    fun editarStockYRetirarRecurso_actualizanListaEnPantalla() {
        runBlocking {
            db.inventarioDao().insertarOActualizarRecurso(
                RecursoInventarioEntity(
                    departamento = Departamento.DIGITALES,
                    nombreItem = "Parlante Bluetooth",
                    stockTotal = 3
                )
            )
        }

        composeTestRule.setContent {
            DuocTheme {
                GestionInventarioScreen(
                    viewModel = viewModel,
                    onVolver = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.inventario.value.isNotEmpty()
        }

        composeTestRule.onNodeWithText("Parlante Bluetooth").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stock total: 3 unidades").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Editar stock").performClick()
        composeTestRule.onNodeWithText("Stock de Parlante Bluetooth").assertIsDisplayed()

        composeTestRule.onNodeWithText("Stock total")
            .performTextClearance()
        composeTestRule.onNodeWithText("Stock total")
            .performTextInput("8")

        composeTestRule.onNodeWithText("Actualizar")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.inventario.value.first().stockTotal == 8
        }

        composeTestRule.onNodeWithText("Stock total: 8 unidades").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Retirar recurso").performClick()
        composeTestRule.onNodeWithText("Retirar recurso").assertIsDisplayed()

        composeTestRule.onNode(
            hasText("Retirar") and hasAnyAncestor(isDialog())
        ).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.inventario.value.isEmpty()
        }

        composeTestRule.onNodeWithText("Aún no tienes recursos. Usa el botón + para agregar.")
            .assertIsDisplayed()
    }
}