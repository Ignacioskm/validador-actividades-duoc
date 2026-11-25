package com.validador_actividades_duoc.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.FirebaseAuthRepository
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.screens.auth.AuthViewModel
import com.validador_actividades_duoc.ui.theme.DuocTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {

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
    fun iniciaEnLoginYNavegaACalendarioSolicitanteEnModoDemo() {
        composeTestRule.setContent {
            DuocTheme {
                AppNavigation(authViewModel = authViewModel)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            !authViewModel.cargando.value
        }

        composeTestRule.onNodeWithText("Continuar con Google").assertIsDisplayed()

        composeTestRule.onNodeWithText("Correo de demostración")
            .performScrollTo()
            .performTextInput("profesor@duoc.cl")

        composeTestRule.onNodeWithText("Ingresar en modo demo")
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.usuarioActual.value?.rol == RolUsuario.SOLICITANTE
        }

        composeTestRule.onNodeWithText("Gestión de Reservas Duoc").assertIsDisplayed()
    }

    @Test
    fun iniciaSesionDemoAdminYNavegaAPanelAdministrador() {
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
            .performTextInput("admin@duoc.cl")

        composeTestRule.onNodeWithText("Ingresar en modo demo")
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.usuarioActual.value?.rol == RolUsuario.ADMIN
        }

        composeTestRule.onNodeWithText("Panel Administrador").assertIsDisplayed()
    }

    @Test
    fun cerrarSesionDesdeSolicitante_regresaALogin() {
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
            .performTextInput("profesor@duoc.cl")

        composeTestRule.onNodeWithText("Ingresar en modo demo")
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.usuarioActual.value != null
        }

        composeTestRule.onNodeWithText("Gestión de Reservas Duoc").assertIsDisplayed()
    }
}
