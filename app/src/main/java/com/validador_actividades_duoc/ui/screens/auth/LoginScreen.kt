package com.validador_actividades_duoc.ui.screens.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.validador_actividades_duoc.domain.model.RolUsuario

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: (RolUsuario) -> Unit
) {
    Text(text = "Pantalla de Login (En construcción)")
}