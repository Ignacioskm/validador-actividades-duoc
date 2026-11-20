package com.validador_actividades_duoc.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.components.DuocPrimaryButton

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: (RolUsuario) -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    LaunchedEffect(usuarioActual) {
        usuarioActual?.let {
            onLoginExitoso(it.rol)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "VALI-DUOC",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Gestión de actividades institucionales",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(32.dp))

            if (cargando) {
                LoadingIndicator(
                    modifier = Modifier.size(56.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text("Verificando tu sesión...")
            } else {

                DuocPrimaryButton(
                    texto = "Continuar con Google",
                    onClick = {
                        viewModel.iniciarSesionGoogle(activity)
                    },
                    habilitado = true
                )

                Spacer(Modifier.height(16.dp))


                if (errorMsg != null) {
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = errorMsg.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
