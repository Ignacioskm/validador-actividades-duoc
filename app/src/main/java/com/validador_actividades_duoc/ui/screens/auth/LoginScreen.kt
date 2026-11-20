
package com.validador_actividades_duoc.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.components.DuocPrimaryButton

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: (RolUsuario) -> Unit
) {
    val activity = LocalContext.current as Activity

    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    var correoDemo by remember { mutableStateOf("") }

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
                .verticalScroll(rememberScrollState())
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

                // INICIO CON GOOGLE
                DuocPrimaryButton(
                    texto = "Continuar con Google",
                    onClick = {
                        viewModel.iniciarSesionGoogle(activity)
                    },
                    habilitado = true
                )

                // ACCESO LOCAL SOLO PARA DEBUG
                    Spacer(Modifier.height(24.dp))

                    HorizontalDivider()

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Acceso de demostración",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Ingresa un correo de prueba para acceder " +
                                "con su rol asignado.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = correoDemo,
                        onValueChange = {
                            correoDemo = it
                        },
                        label = {
                            Text("Correo de demostración")
                        },
                        placeholder = {
                            Text("admin@duoc.cl")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.iniciarSesionDemo(
                                activity,
                                correoDemo
                            )
                        },
                        enabled = correoDemo.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ingresar en modo demo")
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Solo para pruebas del prototipo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )


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

