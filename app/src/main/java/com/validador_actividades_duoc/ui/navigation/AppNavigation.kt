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

sealed class Ruta(val ruta:String) {

    //De momento pondré rutas basicas aca, creo que están todas.
    object Login: Ruta("Login")
    object Solicitante: Ruta("solicitante_calendario")
    object Validador: Ruta("validador_dashboard")
    object Admin: Ruta("admin_resumen")

    object FormularioReserva: Ruta("solicitante_formulario")
    object GestionInventario: Ruta("validador_inventario")
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel){

    val navController = rememberNavController()
    val usuarioActual by authViewModel.usuarioActual.collectAsState()

    val rutaInicial  = when (usuarioActual?.rol){
        null -> Ruta.Login.ruta
        RolUsuario.SOLICITANTE -> Ruta.Solicitante.ruta
        RolUsuario.VALIDADOR -> Ruta.Validador.ruta
        RolUsuario.ADMIN-> Ruta.Admin.ruta
    }

    //NavHost es el contenedor de todas las pantallas
    NavHost(
        navController = navController,
        startDestination = rutaInicial
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
                        popUpTo(Ruta.Login.ruta){ inclusive = true}
                    }
                }
            )
        }

        composable(Ruta.Solicitante.ruta){
            CalendarioScreen(
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta){
                        popUpTo(0)
                    }
                },
                onNuevaReservaClick = {
                    //Esta se descomenta cuando la tengamos
                    //navController.navigate(Ruta.FormularioReserva.ruta)
                }
            )
        }

        composable(Ruta.Validador.ruta) {
            DashboardValidadorScreen(
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0)
                    }
                },
                onIrAInventarioClick = {
                    // navController.navigate(Ruta.GestionInventario.ruta) // Se habilitará luego
                }
            )
        }

        composable(Ruta.Admin.ruta) {
            ResumenEventosScreen(
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0)
                    }
                }
            )
        }

    }
}
