package com.validador_actividades_duoc.ui.screens.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.repository.FirebaseAuthRepository
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.theme.DuocTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AppDatabase
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setUp() {
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
        db.close()
    }

    @Test
    fun renderizaElementosInicialesYBotonDemoDeshabilitadoSiVacio() {
        composeTestRule.setContent {
            DuocTheme {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginExitoso = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            !authViewModel.cargando.value
        }

        composeTestRule.onNodeWithContentDescription("Logo VALI-DUOC")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Gestión de actividades institucionales")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Continuar con Google")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Acceso de demostración")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Ingresar en modo demo")
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun iniciarSesionDemo_conCorreoInvalido_muestraMensajeError() {
        composeTestRule.setContent {
            DuocTheme {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginExitoso = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            !authViewModel.cargando.value
        }

        composeTestRule.onNodeWithText("Correo de demostración")
            .performScrollTo()
            .performTextInput("invalido@duoc.cl")

        composeTestRule.onNodeWithText("Ingresar en modo demo")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.errorMessage.value != null
        }

        composeTestRule.onNodeWithText("Este correo no está habilitado para demostración")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun iniciarSesionDemo_conAdminValido_invocaOnLoginExitosoConRolAdmin() {
        var rolAutenticado: RolUsuario? = null

        composeTestRule.setContent {
            DuocTheme {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginExitoso = { rol -> rolAutenticado = rol }
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            !authViewModel.cargando.value
        }

        composeTestRule.onNodeWithText("Correo de demostración")
            .performScrollTo()
            .performTextInput("admin@duoc.cl")

        composeTestRule.onNodeWithText("Ingresar en modo demo")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            rolAutenticado != null
        }

        assertEquals(RolUsuario.ADMIN, rolAutenticado)
    }
}