package com.validador_actividades_duoc.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.validador_actividades_duoc.R
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.ui.theme.*

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

    var correoDemo by rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(usuarioActual) {
        usuarioActual?.let {
            onLoginExitoso(it.rol)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DuocBackgroundLight
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // Fondo decorativo institucional
            FondoLogin()

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 480.dp)
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 24.dp,
                        vertical = 48.dp
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // LOGO PRINCIPAL
                Image(
                    painter = painterResource(
                        id = R.drawable.logo_vali_duoc
                    ),
                    contentDescription = "Logo VALI-DUOC",
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(120.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(20.dp))

                // SUBTÍTULO
                Text(
                    text = "Gestión de actividades institucionales",
                    fontFamily = LatoFontFamily,
                    fontSize = 16.sp,
                    color = DuocGrayMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                if (cargando) {

                    LoadingIndicator(
                        modifier = Modifier.size(56.dp),
                        color = DuocYellow
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Verificando tu sesión...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DuocGrayMedium
                    )

                } else {

                    // INICIO DE SESIÓN CON GOOGLE
                    Button(
                        onClick = {
                            viewModel.iniciarSesionGoogle(activity)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DuocYellow,
                            contentColor = DuocBlackCharcoal
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 1.dp
                        )
                    ) {
                        Text(
                            text = "Continuar con Google",
                            fontFamily = LatoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    HorizontalDivider(
                        color = DuocGrayMedium.copy(alpha = 0.25f)
                    )

                    Spacer(Modifier.height(24.dp))

                    // TARJETA DE ACCESO DEMO
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = DuocWhite.copy(
                                alpha = 0.85f
                            )
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = DuocGrayMedium.copy(alpha = 0.22f)
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "Acceso de demostración",
                                fontFamily = LatoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = DuocBlackCharcoal,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Ingresa un correo de prueba para acceder con su rol asignado.",
                                fontFamily = LatoFontFamily,
                                fontSize = 13.sp,
                                color = DuocGrayMedium,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )

                            Spacer(Modifier.height(20.dp))

                            // CAMPO DE CORREO
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
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DuocYellow,
                                    unfocusedBorderColor = DuocGrayMedium,
                                    focusedLabelColor = DuocGray80,
                                    unfocusedLabelColor = DuocGrayMedium,
                                    focusedTextColor = DuocBlackCharcoal,
                                    unfocusedTextColor = DuocBlackCharcoal,
                                    cursorColor = DuocYellow,
                                    focusedPlaceholderColor = DuocGrayMedium,
                                    unfocusedPlaceholderColor = DuocGrayMedium
                                )
                            )

                            Spacer(Modifier.height(16.dp))

                            // BOTÓN DE ACCESO DEMO
                            Button(
                                onClick = {
                                    viewModel.iniciarSesionDemo(
                                        activity,
                                        correoDemo.trim()
                                    )
                                },
                                enabled = correoDemo.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DuocBlackCharcoal,
                                    contentColor = DuocWhite,
                                    disabledContainerColor = DuocGrayLight,
                                    disabledContentColor = DuocGrayMedium
                                )
                            ) {
                                Text(
                                    text = "Ingresar en modo demo",
                                    fontFamily = LatoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Solo para pruebas del prototipo",
                                fontFamily = LatoFontFamily,
                                fontSize = 12.sp,
                                color = DuocGrayMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // MENSAJE DE ERROR
                    if (errorMsg != null) {

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = errorMsg.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// FONDO DECORATIVO DE LA PANTALLA
@Composable
private fun FondoLogin() {

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        val w = size.width
        val h = size.height

        // Onda gris superior
        val grisSuperior = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.65f, 0f)

            cubicTo(
                w * 0.30f, h * 0.05f,
                w * 0.18f, h * 0.12f,
                0f, h * 0.19f
            )

            close()
        }

        drawPath(
            path = grisSuperior,
            color = DuocGrayLight
        )

        // Onda amarilla suave superior
        val amarilloSuaveSuperior = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.60f, 0f)

            cubicTo(
                w * 0.38f, h * 0.055f,
                w * 0.15f, h * 0.02f,
                0f, h * 0.145f
            )

            close()
        }

        drawPath(
            path = amarilloSuaveSuperior,
            color = DuocYellow.copy(alpha = 0.18f)
        )

        // Onda amarilla principal superior
        val amarilloSuperior = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.26f, 0f)

            cubicTo(
                w * 0.12f, h * 0.04f,
                w * 0.10f, h * 0.09f,
                0f, h * 0.13f
            )

            close()
        }

        drawPath(
            path = amarilloSuperior,
            brush = Brush.linearGradient(
                colors = listOf(
                    DuocYellow,
                    DuocYellowAlt.copy(alpha = 0.55f)
                )
            )
        )

        // Onda gris inferior
        val grisInferior = Path().apply {
            moveTo(0f, h * 0.91f)

            cubicTo(
                w * 0.28f, h * 0.88f,
                w * 0.50f, h * 1.01f,
                w, h * 0.90f
            )

            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = grisInferior,
            color = DuocGrayLight
        )

        // Onda amarilla suave inferior
        val amarilloSuaveInferior = Path().apply {
            moveTo(0f, h)
            lineTo(w * 0.35f, h)

            cubicTo(
                w * 0.62f, h * 0.96f,
                w * 0.85f, h * 0.90f,
                w, h * 0.87f
            )

            lineTo(w, h)
            close()
        }

        drawPath(
            path = amarilloSuaveInferior,
            color = DuocYellow.copy(alpha = 0.18f)
        )

        // Onda amarilla principal inferior
        val amarilloInferior = Path().apply {
            moveTo(w * 0.45f, h)

            cubicTo(
                w * 0.70f, h * 0.98f,
                w * 0.88f, h * 0.93f,
                w, h * 0.915f
            )

            lineTo(w, h)
            close()
        }

        drawPath(
            path = amarilloInferior,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    DuocYellowAlt.copy(alpha = 0.45f),
                    DuocYellow
                )
            )
        )
    }
}


