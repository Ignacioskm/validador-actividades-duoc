package com.validador_actividades_duoc.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.validador_actividades_duoc.data.local.dao.UsuarioDao
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.FirebaseAuthRepository
import com.validador_actividades_duoc.domain.model.RolUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val usuarioDao: UsuarioDao,
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {

    private val _usuarioActual =
        MutableStateFlow<UsuarioEntity?>(null)
    val usuarioActual = _usuarioActual.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando = _cargando.asStateFlow()

    init {
        restaurarSesion()
    }

    private suspend fun registrarORecuperarUsuario(
        firebaseUser: FirebaseUser
    ): UsuarioEntity {

        val uid = firebaseUser.uid

        val correo = firebaseUser.email
            ?.trim()
            ?.lowercase()
            ?: error("Google no proporcionó un correo")

        val nombre = firebaseUser.displayName ?: correo

        val existente = usuarioDao.obtenerUsuarioPorUid(uid)

        if (existente != null) {
            // Conservamos el rol y el departamento existentes.
            usuarioDao.actualizarPerfilGoogle(uid, correo, nombre)

            return usuarioDao.obtenerUsuarioPorUid(uid)
                ?: error("No se encontró el usuario actualizado")
        }

        // Evitamos adoptar roles de cuentas de demostración
        // o de otros usuarios por coincidencia de correo.
        check(usuarioDao.obtenerUsuarioPorEmail(correo) == null) {
            "El correo ya existe en la base local. " +
                    "Se requiere vinculación administrativa."
        }

        val nuevoId = usuarioDao.insertarUsuario(
            UsuarioEntity(
                correo = correo,
                nombre = nombre,
                rol = RolUsuario.SOLICITANTE,
                departamento = null,
                firebaseUid = uid
            )
        )

        return usuarioDao.obtenerUsuarioPorId(nuevoId)
            ?: error("No se pudo crear el usuario local")
    }

    private fun restaurarSesion() {
        viewModelScope.launch {
            _cargando.value = true

            try {
                val firebaseUser = authRepository.usuarioFirebase

                _usuarioActual.value = firebaseUser?.let {
                    registrarORecuperarUsuario(it)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _usuarioActual.value = null
                _errorMessage.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    fun iniciarSesionGoogle(activity: Activity) {
        if (_cargando.value) return

        viewModelScope.launch {
            _cargando.value = true
            _errorMessage.value = null

            try {
                val firebaseUser =
                    authRepository.iniciarSesionGoogle(activity)

                _usuarioActual.value =
                    registrarORecuperarUsuario(firebaseUser)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _usuarioActual.value = null
                _errorMessage.value =
                    e.message ?: "No se pudo iniciar sesión"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun cerrarSesion(activity: Activity) {
        viewModelScope.launch {
            _usuarioActual.value = null
            _cargando.value = true
            _errorMessage.value = null

            try {
                authRepository.cerrarSesion(activity)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value =
                    "Error al limpiar credenciales: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}

