package com.validador_actividades_duoc.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.validador_actividades_duoc.data.local.dao.UsuarioDao
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.domain.util.esCorreoValido
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class AuthViewModel(
    private val usuarioDao : UsuarioDao
): ViewModel() {

    private val _usuarioActual = MutableStateFlow<UsuarioEntity?>(null)
    val usuarioActual: StateFlow<UsuarioEntity?> = _usuarioActual.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun iniciarSesion(correo: String){
        val correoLimpio = correo.trim().lowercase()
        if(!esCorreoValido(correoLimpio)){
            _errorMessage.value = "Correo institucional no valido(use @duocuc.cl o @duoc.cl)"
            return
        }

        viewModelScope.launch {
            try {
                var usuario = usuarioDao.obtenerUsuarioPorEmail(correoLimpio)
                if (usuario == null){
                    val nuevoId = usuarioDao.insertarUsuario(
                        UsuarioEntity(correo = correoLimpio, nombre = correoLimpio.substringBefore('@'),rol = RolUsuario.SOLICITANTE)
                    )
                    usuario = usuarioDao.obtenerUsuarioPorId(nuevoId.toInt())
                }
                _usuarioActual.value = usuario
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al iniciar sesión: ${e.message}"
            }
        }
    }

    fun cerrarSesion(){
        _usuarioActual.value = null
    }

    //Este método se borrará desps de hacer las pruebas
    // Método auxiliar para pruebas rápidas por rol desde la UI
    fun simularLoginRol(rol: RolUsuario) {
        _usuarioActual.value = UsuarioEntity(
            id = 99,
            correo = "test.${rol.name.lowercase()}@duocuc.cl",
            nombre = "Usuario ${rol.name}",
            rol = rol
        )
    }
}
