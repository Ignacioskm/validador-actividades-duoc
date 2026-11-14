package com.validador_actividades_duoc.ui.theme

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/*
    Temas claro/oscuro
 */

/**
 * Esquema de colores para el tema claro
 * - mapea la paleta institucional de DuocUC a los roles semánticos de Material Design 3,
 * priorizando fondos claros con alto contraste en textos e indicadores
 */
private val LightColorScheme = lightColorScheme(
    primary = DuocBlackCharcoal,
    onPrimary = DuocWhite,
    primaryContainer = DuocYellow,
    onPrimaryContainer = DuocBlackCharcoal,
    secondary = DuocYellow,
    onSecondary = DuocBlackCharcoal,
    background = DuocBackgroundLight,
    onBackground = DuocBlackCharcoal,
    surface = DuocWhite,
    onSurface = DuocBlackCharcoal,
    surfaceVariant = DuocGrayLight,
    onSurfaceVariant = DuocGray80,
    error = StatusRejected,
    onError = DuocWhite
)

/**
 * Esquema de colores para el tema oscuro
 * - mapea la paleta institucional de DuocUC a los roles semánticos de Material Design 3,
 * pero pensados para utilizarse en entornos oscuros
 */
private val DarkColorScheme = darkColorScheme(
    primary = DuocYellow,
    onPrimary = DuocBlackCharcoal,
    primaryContainer = DuocYellowDark,
    onPrimaryContainer = DuocWhite,
    secondary = DuocYellowAlt,
    onSecondary = DuocBlackCharcoal,
    background = DuocBlackCharcoal,
    onBackground = DuocWhite,
    surface = DuocGray80,
    onSurface = DuocWhite,
    surfaceVariant = DuocGray80,
    onSurfaceVariant = DuocGrayLight,
    error = StatusRejected,
    onError = DuocWhite
)



/**
 * Aplica el tema visual principal de la aplicación
 *
 * @param darkTheme Determina si se renderiza en modo oscuro, sigue la configuración del teléfono
 * @param dynamicColor Fijado en `false` para preservar la identidad DuocUC
 * @param content Estructura composable que heredará la configuración visual
 */
@Composable
fun DuocTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DuocTypography,
        content = content
    )
}

/**
 * Muestra la vista previa del tema institucional DuocUC en modo claro y modo oscuro,
 * permitiendo validar el contraste de colores y la jerarquía tipográfica
 */

@Preview(
    showBackground = true,
    name = "Modo Claro"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Modo Oscuro"
)
@Composable
fun DuocThemePreview() {
    DuocTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "VALIDADOR DE ACTIVIDADES DUOC UC",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Auditorio Padre Hurtado",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Solicitado para el bloque 09:00 - 11:15",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CONFIRMAR REGISTRO",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}