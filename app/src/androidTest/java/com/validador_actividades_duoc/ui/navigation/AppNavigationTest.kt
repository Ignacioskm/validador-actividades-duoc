package com.validador_actividades_duoc.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.remote.RetrofitInstance
import com.validador_actividades_duoc.data.repository.FirebaseAuthRepository
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.screens.auth.AuthViewModel
import com.validador_actividades_duoc.ui.theme.DuocTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var authViewModel: AuthViewModel
    private lateinit var urlOriginal: String

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

        authViewModel = AuthViewModel(
            usuarioDao = db.usuarioDao(),
            authRepository = FirebaseAuthRepository()
        )
    }

    @After
    fun tearDown() {
        RetrofitInstance.setBaseUrl(urlOriginal)
        db.close()
    }

    private fun iniciarDemo(correo: String) {
        composeTestRule.setContent {
            DuocTheme {
                AppNavigation(authViewModel = authViewModel)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            !authViewModel.cargando.value
        }

        composeTestRule.onNodeWithText("Correo de demostración")
            .performScrollTo()
            .performTextInput(correo)

        composeTestRule.onNodeWithText("Ingresar en modo demo")
            .performScrollTo()
            .performClick()
    }

    @Test
    fun iniciaEnLoginYNavegaACalendarioSolicitanteYCierraSesion() {
        iniciarDemo("profesor@duoc.cl")

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.usuarioActual.value?.rol == RolUsuario.SOLICITANTE
        }

        composeTestRule.onNodeWithText("Gestión de Reservas Duoc").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Cerrar sesión").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.usuarioActual.value == null &&
                    !authViewModel.cargando.value &&
                    composeTestRule.onAllNodesWithText("Continuar con Google")
                        .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Continuar con Google").assertIsDisplayed()
    }

    @Test
    fun iniciaSesionDemoAdminNavegaAPanelAdministradorYCierraSesion() {
        iniciarDemo("admin@duoc.cl")

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.usuarioActual.value?.rol == RolUsuario.ADMIN
        }

        composeTestRule.onNodeWithText("Panel Administrador").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Cerrar sesión").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.usuarioActual.value == null &&
                    !authViewModel.cargando.value &&
                    composeTestRule.onAllNodesWithText("Continuar con Google")
                        .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Continuar con Google").assertIsDisplayed()
    }

    @Test
    fun iniciaSesionDemoValidadorNavegaADashboardInventarioYCierraSesion() {
        iniciarDemo("digitales@duoc.cl")

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.usuarioActual.value?.rol == RolUsuario.VALIDADOR &&
                    composeTestRule.onAllNodesWithText("Gestionar inventario")
                        .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Gestionar inventario").performClick()

        composeTestRule.onNodeWithContentDescription("Volver").performClick()

        composeTestRule.onNodeWithText("Panel de validación").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Cerrar sesión").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.usuarioActual.value == null &&
                    !authViewModel.cargando.value &&
                    composeTestRule.onAllNodesWithText("Continuar con Google")
                        .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Continuar con Google").assertIsDisplayed()
    }
}