package com.validador_actividades_duoc.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.ui.theme.DuocTheme

/**
 * Campo de texto reutilizable para formularios de la app
 *
 * Encapsula la lógica de diseño de [OutlinedTextField], integrando visualización
 * automática de mensajes de error de validación, soporte para iconos y opciones de teclado
 *
 * @param valor Texto actual ingresado en el campo
 * @param alCambiarValor Callback invocado cuando el usuario edita el texto
 * @param etiqueta Texto descriptivo de la etiqueta del campo
 * @param modificador Modificador de layout para personalizar márgenes o dimensiones
 * @param mensajeError Mensaje explicativo en caso de validación fallida
 * @param iconoInicio Icono opcional que se muestra al principio del campo
 * @param iconoFin Icono opcional que se muestra al final del campo
 * @param opcionesTeclado Configuración del tipo de teclado (ej. numérico, email, texto)
 * @param accionesTeclado Acciones del teclado (ej. siguiente, listo, buscar)
 * @param soloLectura Si es true, deshabilita la edición directa pero permite clics (ej. para selectores de fecha)
 * @param lineaUnica Define si el campo se limita a una sola línea horizontal
 * @param maxLineas Número máximo de líneas visibles cuando lineaUnica es false
 */
@Composable
fun FormTextField(
    valor: String,
    alCambiarValor: (String) -> Unit,
    etiqueta: String,
    modificador: Modifier = Modifier,
    mensajeError: String? = null,
    iconoInicio: @Composable (() -> Unit)? = null,
    iconoFin: @Composable (() -> Unit)? = null,
    opcionesTeclado: KeyboardOptions = KeyboardOptions.Default,
    accionesTeclado: KeyboardActions = KeyboardActions.Default,
    soloLectura: Boolean = false,
    lineaUnica: Boolean = true,
    maxLineas: Int = if (lineaUnica) 1 else 3
) {
    val esError = !mensajeError.isNullOrBlank()

    Column(modifier = modificador.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiarValor,
            label = { Text(text = etiqueta) },
            modifier = Modifier.fillMaxWidth(),
            isError = esError,
            readOnly = soloLectura,
            singleLine = lineaUnica,
            maxLines = maxLineas,
            leadingIcon = iconoInicio,
            trailingIcon = iconoFin,
            keyboardOptions = opcionesTeclado,
            keyboardActions = accionesTeclado,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            )
        )

        if (esError) {
            Text(
                text = mensajeError.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Vista previa para verificar el campo de texto en sus estados
 * normal o con error en ambos temas claro y oscuro
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
fun FormTextFieldPreview() {
    DuocTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {

                FormTextField(
                    valor = "Charla de Inteligencia Artificial",
                    alCambiarValor = {},
                    etiqueta = "Nombre de la Actividad"
                )

                FormTextField(
                    valor = "",
                    alCambiarValor = {},
                    etiqueta = "Nombre de la Actividad",
                    mensajeError = "El nombre de la actividad es obligatorio."
                )
            }
        }
    }
}

