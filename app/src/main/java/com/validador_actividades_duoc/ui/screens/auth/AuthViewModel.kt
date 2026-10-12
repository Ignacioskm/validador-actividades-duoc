package com.validador_actividades_duoc.ui.screens.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.validador_actividades_duoc.domain.model.RolUsuario


//TEMPLATE PARA EVITAR ERRORES -ESTO DESPS SE CAMBIA
data class UsuarioSimulado(val rol: RolUsuario)

class AuthViewModel : ViewModel() {
    private val _usuarioActual = MutableStateFlow<UsuarioSimulado?>(null)
    val usuarioActual: StateFlow<UsuarioSimulado?> = _usuarioActual

    fun cerrarSesion() {
        _usuarioActual.value = null
    }
}
