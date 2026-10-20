package com.validador_actividades_duoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.ui.navigation.AppNavigation
import com.validador_actividades_duoc.ui.screens.auth.AuthViewModel
import com.validador_actividades_duoc.ui.theme.DuocTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppContainer.provideDatabase(applicationContext)
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(database.usuarioDao()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Datos de prueba (solo la primera vez)
        lifecycleScope.launch(Dispatchers.IO) {
            AppContainer.sembrarDatosIniciales(applicationContext)
        }

        setContent {
            DuocTheme {
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}
