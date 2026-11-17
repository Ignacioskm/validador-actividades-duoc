package com.validador_actividades_duoc.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.screens.admin.AdminViewModel
import com.validador_actividades_duoc.ui.screens.admin.AdminViewModelFactory
import com.validador_actividades_duoc.ui.screens.admin.ResumenEventosScreen
import com.validador_actividades_duoc.ui.screens.auth.AuthViewModel
import com.validador_actividades_duoc.ui.screens.auth.LoginScreen
import com.validador_actividades_duoc.ui.screens.solicitante.FormularioWizardScreen
import com.validador_actividades_duoc.ui.screens.solicitante.CalendarioScreen
import com.validador_actividades_duoc.ui.screens.solicitante.SolicitanteViewModel
import com.validador_actividades_duoc.ui.screens.solicitante.SolicitanteViewModelFactory
import com.validador_actividades_duoc.ui.screens.validador.DashboardValidadorScreen
import com.validador_actividades_duoc.ui.screens.validador.GestionInventarioScreen
import com.validador_actividades_duoc.ui.screens.validador.ValidadorViewModel
import com.validador_actividades_duoc.ui.screens.validador.ValidadorViewModelFactory
// retrofit
import com.validador_actividades_duoc.ui.screens.posts.PostScreen
import com.validador_actividades_duoc.ui.screens.posts.PostViewModel

sealed class Ruta(val ruta: String) {
    object Login : Ruta("login")
    object Solicitante : Ruta("solicitante_calendario")

    object Formulario : Ruta("solicitante_formulario")
    object Validador : Ruta("validador_dashboard")
    object Admin : Ruta("admin_resumen")
    object GestionInventario : Ruta("validador_inventario")

    object Posts : Ruta("posts")   // ← agregar esto
}

@Composable
private fun rememberValidadorViewModel(validador: UsuarioEntity): ValidadorViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        key = "validador_${validador.id}",
        factory = ValidadorViewModelFactory(context, validador)
    )
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val context = LocalContext.current.applicationContext
    val navController = rememberNavController()

    val cerrarSesion: () -> Unit = {
        authViewModel.cerrarSesion()
        navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
    }

    NavHost(navController = navController, startDestination = Ruta.Login.ruta) {

        composable(Ruta.Login.ruta) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = { rol ->
                    val destino = when (rol) {
                        RolUsuario.SOLICITANTE -> Ruta.Solicitante.ruta
                        RolUsuario.VALIDADOR -> Ruta.Validador.ruta
                        RolUsuario.ADMIN -> Ruta.Admin.ruta
                    }
                    navController.navigate(destino) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                }
            )
        }


        composable(Ruta.Solicitante.ruta) { backStackEntry ->
            val usuario by authViewModel.usuarioActual.collectAsState()
            val solicitante = usuario ?: return@composable

            if(solicitante.rol != RolUsuario.SOLICITANTE){
                Text("Acceso no autorizado")
            }

            val solicitanteViewModel: SolicitanteViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = SolicitanteViewModelFactory(
                    AppContainer.provideReservaRepository(context),
                    AppContainer.provideInventarioRepository(context)
                )
            )

            CalendarioScreen(
                solicitanteId = solicitante.id,
                nombreUsuario = solicitante.nombre,
                viewModel = solicitanteViewModel,
                onAbrirWizard = {fecha, bloque ->
                    solicitanteViewModel.prepararWizard(fecha, bloque)
                    navController.navigate(Ruta.Formulario.ruta)
                },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(Ruta.Formulario.ruta) { backStackEntry ->
            val usuario by authViewModel.usuarioActual.collectAsState()
            val solicitante = usuario ?: return@composable

            val viewModelOwner = remember(navController.currentBackStackEntry) {
                runCatching {
                    navController.getBackStackEntry(Ruta.Solicitante.ruta)
                }.getOrDefault(backStackEntry)
            }
            val solicitanteViewModel: SolicitanteViewModel = viewModel(
                viewModelStoreOwner = viewModelOwner,
                factory = SolicitanteViewModelFactory(
                    AppContainer.provideReservaRepository(context),
                    AppContainer.provideInventarioRepository(context)

                )
            )

            LaunchedEffect(solicitante.id) {
                solicitanteViewModel.inicializarSolicitante(solicitante.id)
            }

            FormularioWizardScreen(
                viewModel = solicitanteViewModel,
                onVolverCalendario = {navController.popBackStack()}
            )

        }


        composable(Ruta.Validador.ruta) {
            val usuario by authViewModel.usuarioActual.collectAsState()
            val validador = usuario ?: return@composable          // sesión cerrada: no dibujar nada
            if (validador.rol != RolUsuario.VALIDADOR || validador.departamento == null) {
                Text("Tu usuario no tiene un área asignada. Contacta al administrador.")
                return@composable
            }

            DashboardValidadorScreen(
                viewModel = rememberValidadorViewModel(validador),
                onCerrarSesion = cerrarSesion,
                onIrAInventarioClick = { navController.navigate(Ruta.GestionInventario.ruta) }
            )
        }

        composable(Ruta.GestionInventario.ruta) {
            val usuario by authViewModel.usuarioActual.collectAsState()
            val validador = usuario ?: return@composable
            if (validador.departamento == null) return@composable

            GestionInventarioScreen(
                viewModel = rememberValidadorViewModel(validador),
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.Admin.ruta) {
            val context = LocalContext.current.applicationContext
            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModelFactory(context)
            )

            ResumenEventosScreen(
                navController = navController,
                onCerrarSesion = cerrarSesion,
                viewModel = adminViewModel
            )
        }

        composable("posts") {
            PostScreen(viewModel = viewModel())
        }
    }
}