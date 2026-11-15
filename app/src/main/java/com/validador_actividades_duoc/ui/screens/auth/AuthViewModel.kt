package com.validador_actividades_duoc.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.validador_actividades_duoc.data.local.dao.UsuarioDao
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.domain.util.DOMINIOS_PERMITIDOS
import com.validador_actividades_duoc.domain.util.esCorreoValido
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _usuarioActual = MutableStateFlow<UsuarioEntity?>(null)
    val usuarioActual: StateFlow<UsuarioEntity?> = _usuarioActual.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _cargando = MutableStateFlow(false)

    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()
    fun iniciarSesion(correo: String) {
        val correoLimpio = correo.trim().lowercase()
        if (!esCorreoValido(correoLimpio)) {
            _errorMessage.value = "Usa tu correo institucional (@${DOMINIOS_PERMITIDOS.joinToString(", @")})"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _errorMessage.value = null
                try {
                    delay(2200)
                    var usuario = usuarioDao.obtenerUsuarioPorEmail(correoLimpio)
                    if (usuario == null) {
                        // Un correo institucional nuevo entra como SOLICITANTE
                        val nuevoId = usuarioDao.insertarUsuario(
                            UsuarioEntity(
                                correo = correoLimpio,
                                nombre = correoLimpio.substringBefore('@'),
                                rol = RolUsuario.SOLICITANTE
                            )
                        )
                        usuario = usuarioDao.obtenerUsuarioPorId(nuevoId)
                    }
                    _errorMessage.value = null
                    _usuarioActual.value = usuario
                } catch (e: Exception) {
                    _errorMessage.value = "Error al iniciar sesión: ${e.message}"
                }finally {
                    _cargando.value = false
                }
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }
}
