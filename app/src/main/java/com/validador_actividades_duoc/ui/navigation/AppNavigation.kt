package com.validador_actividades_duoc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.screens.auth.AuthViewModel
import com.validador_actividades_duoc.ui.screens.auth.LoginScreen
import com.validador_actividades_duoc.ui.screens.solicitante.CalendarioScreen
import com.validador_actividades_duoc.ui.screens.validador.DashboardValidadorScreen
import com.validador_actividades_duoc.ui.screens.admin.ResumenEventosScreen
import com.validador_actividades_duoc.ui.screens.validador.GestionInventarioScreen
import com.validador_actividades_duoc.ui.screens.validador.ValidadorViewModel

sealed class Ruta(val ruta:String) {

    object Login: Ruta("Login")
    object Solicitante: Ruta("solicitante_calendario")
    object Validador: Ruta("validador_dashboard")
    object Admin: Ruta("admin_resumen")
    object GestionInventario: Ruta("validador_inventario")
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel){

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta
    ) {
        composable(Ruta.Login.ruta){
            LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = { rol ->
                    val destino = when (rol) {
                        RolUsuario.SOLICITANTE -> Ruta.Solicitante.ruta
                        RolUsuario.VALIDADOR -> Ruta.Validador.ruta
                        RolUsuario.ADMIN -> Ruta.Admin.ruta
                    }
                    navController.navigate(destino) {
                        // Limpiamos el historial para que al dar "Atrás" no vuelva al login
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Ruta.Solicitante.ruta){
            CalendarioScreen(
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta){
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNuevaReservaClick = {
                    // navController.navigate(Ruta.FormularioReserva.ruta)
                }
            )
        }

        composable(Ruta.Validador.ruta) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val database = com.validador_actividades_duoc.data.local.AppContainer.provideDatabase(context)

            // Obtenemos el usuario actual del ViewModel para filtrar su departamento
            val usuarioActual by authViewModel.usuarioActual.collectAsState()
            val deptoUsuario = usuarioActual?.departamento ?: com.validador_actividades_duoc.domain.model.Departamento.AUXILIARES

            val validadorViewModel = ValidadorViewModel(
                reservaDao = database.reservaDao(),
                departamentoValidador = deptoUsuario
            )

            DashboardValidadorScreen(
                viewModel = validadorViewModel,
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                },
                onIrAInventarioClick = {
                    navController.navigate(Ruta.GestionInventario.ruta)
                }
            )
        }

        composable(Ruta.Admin.ruta) {
            ResumenEventosScreen(
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Ruta.GestionInventario.ruta) {
            GestionInventarioScreen(
                onVolver = { navController.popBackStack() }
            )
        }
    }
}