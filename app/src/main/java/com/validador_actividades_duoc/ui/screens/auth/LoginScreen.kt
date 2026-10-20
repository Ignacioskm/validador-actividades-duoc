package com.validador_actividades_duoc.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.components.DuocPrimaryButton
import com.validador_actividades_duoc.ui.components.FormTextField

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: (RolUsuario) -> Unit
) {
    var correoInput by remember { mutableStateOf("") }
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()

    LaunchedEffect(usuarioActual) {
        usuarioActual?.let{
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
        ){
            Text(
                text = "VALI-DUOC",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(32.dp))

            FormTextField(
                valor = correoInput,
                alCambiarValor = { correoInput = it},
                etiqueta = "Correo institucional",
                mensajeError = errorMsg
            )

            Spacer(modifier = Modifier.height(16.dp))

            DuocPrimaryButton(
                texto = "Ingresar",
                onClick = {viewModel.iniciarSesion(correoInput)}
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Acceso Rápido de Pruebas (Dev):", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.simularLoginRol(RolUsuario.SOLICITANTE) }) {
                    Text("Solicitante")
                }
                Button(onClick = { viewModel.simularLoginRol(RolUsuario.VALIDADOR) }) {
                    Text("Validador")
                }
                Button(onClick = { viewModel.simularLoginRol(RolUsuario.ADMIN) }) {
                    Text("Admin")
                }
            }
        }
    }

}