package com.validador_actividades_duoc

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.ui.navigation.AppNavigation
import com.validador_actividades_duoc.ui.screens.auth.AuthViewModel
import com.validador_actividades_duoc.notifications.NotificacionesHelper
import com.validador_actividades_duoc.notifications.NotificadorEventos
import com.validador_actividades_duoc.ui.theme.DuocTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.validador_actividades_duoc.data.repository.FirebaseAuthRepository

import com.validador_actividades_duoc.data.remote.RetrofitInstance

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppContainer.provideDatabase(applicationContext)
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(
                    usuarioDao = database.usuarioDao(),
                    authRepository = FirebaseAuthRepository()
                ) as T
            }
        }
    }

    private val notificador by lazy {
        NotificadorEventos(
            applicationContext,
            AppContainer.provideReservaRepository(applicationContext)
        )
    }

    private val permisoNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            // Si lo conceden, se reinicia el notificador para avisar lo que quedó pendiente
            if (concedido) {
                authViewModel.usuarioActual.value?.let { notificador.iniciar(lifecycleScope, it) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Datos de prueba (solo la primera vez)
        lifecycleScope.launch(Dispatchers.IO) {
            AppContainer.sembrarDatosIniciales(applicationContext)
        }

        // --- PRUEBA TEMPORAL DE CONEXIÓN AL BACKEND ---
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val recursos = RetrofitInstance.inventarioApi.obtenerRecursos()
                android.util.Log.d("TEST_API", "Conectado! Recursos recibidos: ${recursos.size} -> $recursos")
            } catch (e: java.net.SocketTimeoutException) {
                android.util.Log.e(
                    "TEST_API",
                    "Timeout conectando al backend en ${RetrofitInstance.getBaseUrl()}. " +
                    "Asegúrate de que el servidor local (Node.js en puerto 3000) esté iniciado y que la dirección IP sea accesible desde el dispositivo/emulador.",
                    e
                )
            } catch (e: java.net.ConnectException) {
                android.util.Log.e(
                    "TEST_API",
                    "No se pudo conectar al backend en ${RetrofitInstance.getBaseUrl()}. Servidor no disponible o conexión rechazada.",
                    e
                )
            } catch (e: Exception) {
                android.util.Log.e("TEST_API", "Error conectando al backend en ${RetrofitInstance.getBaseUrl()}", e)
            }
        }
        // ----------------------------------------------

        // Notificaciones según el usuario con sesión iniciada
        lifecycleScope.launch {
            authViewModel.usuarioActual.collect { usuario ->
                if (usuario == null) {
                    notificador.detener()
                } else {
                    if (!NotificacionesHelper.tienePermiso(this@MainActivity)) {
                        permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    notificador.iniciar(lifecycleScope, usuario)
                }
            }
        }

        setContent {
            DuocTheme {
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}
