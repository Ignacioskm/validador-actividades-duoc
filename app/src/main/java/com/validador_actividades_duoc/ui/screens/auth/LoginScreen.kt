package com.validador_actividades_duoc.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
        usuarioActual?.let { onLoginExitoso(it.rol) }
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

            Spacer(modifier = Modifier.height(32.dp))

            FormTextField(
                valor = correoInput,
                alCambiarValor = { correoInput = it },
                etiqueta = "Correo institucional",
                mensajeError = errorMsg,
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            DuocPrimaryButton(
                texto = "Ingresar",
                onClick = { viewModel.iniciarSesion(correoInput) },
                habilitado = correoInput.isNotBlank()
            )
        }
    }
}