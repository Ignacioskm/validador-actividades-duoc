package com.validador_actividades_duoc.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.ui.theme.DuocBlackCharcoal
import com.validador_actividades_duoc.ui.theme.DuocTheme
import com.validador_actividades_duoc.ui.theme.DuocYellow

/**
 * Botón principal reutilizable
 *
 * @param texto Texto visible que describe la acción principal del botón
 * @param onClick Función de callback que se ejecuta al presionar el botón
 * @param modificador Modificador de layout para personalizar el tamaño o margen
 * @param habilitado Define si el botón está interactivo (true) o deshabilitado (false)
 * @param cargando Indica si hay una operación asíncrona en proceso, muestra un spinner si es true
 */
@Composable
fun DuocPrimaryButton(
    texto: String,
    onClick: () -> Unit,
    modificador: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        modifier = modificador
            .fillMaxWidth()
            .height(50.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = DuocYellow,
            contentColor = DuocBlackCharcoal,
            disabledContainerColor = DuocYellow.copy(alpha = 0.5f),
            disabledContentColor = DuocBlackCharcoal.copy(alpha = 0.5f)
        )
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = DuocBlackCharcoal,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Vista previa para verificar el botón en sus distintos estados:
 * normal, cargando y deshabilitado
 * en tema claro y oscuro
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
fun DuocPrimaryButtonPreview() {
    DuocTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {

                DuocPrimaryButton(
                    texto = "Confirmar Reserva",
                    onClick = {}
                )

                DuocPrimaryButton(
                    texto = "Enviando Solicitud...",
                    onClick = {},
                    cargando = true
                )

                DuocPrimaryButton(
                    texto = "Formulario Incompleto",
                    onClick = {},
                    habilitado = false
                )
            }
        }
    }
}
