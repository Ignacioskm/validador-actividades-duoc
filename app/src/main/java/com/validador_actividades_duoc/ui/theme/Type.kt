package com.validador_actividades_duoc.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.validador_actividades_duoc.R


/*
    Declaración de Familias Tipográficas (FontFamily)
 */

/**
 * Bebas Neue: Fuente principal para títulos grandes y encabezados
 */
val BebasNeueFontFamily = FontFamily(
    Font(R.font.bebasneue_regular, FontWeight.Normal)
)

/**
 * Lato: Fuente secundaria para cuerpo de texto, subtítulos y botones
 */
val LatoFontFamily = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_bold, FontWeight.Bold),
    Font(R.font.lato_italic, FontWeight.Normal, FontStyle.Italic)
)


/*
    Configuración del Sistema Tipográfico (Typography)
 */

/**
 * Configuración centralizada de tipografías para Material Design 3
 *
 * - **Bebas Neue**: Asignada a `displayLarge` y `headlineLarge` (títulos de alto impacto)
 * - **Lato**: Asignada a `titleLarge`, `bodyLarge`, `bodyMedium` y `labelLarge` (lectura y botones)
 */
val DuocTypography = Typography(
    // títulos y encabezados principales
    displayLarge = TextStyle(
        fontFamily = BebasNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = BebasNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),

    // títulos secundarios y subtítulos
    titleLarge = TextStyle(
        fontFamily = LatoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),

    // cuerpo de texto
    bodyLarge = TextStyle(
        fontFamily = LatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = LatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),

    // etiquetas, botones e inputs
    labelLarge = TextStyle(
        fontFamily = LatoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
)


/**
 *  Función de vista previa para inspeccionar la tipografía de la aplicación
 */

@Preview(showBackground = true, name = "Prueba de Tipografías Duoc UC")
@Composable
fun TipografiasPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "RESERVAS DE EVENTOS DUOC UC",
            style = DuocTypography.displayLarge,
            color = DuocBlackCharcoal
        )
        Text(
            text = "Formulario de Solicitud de Recursos",
            style = DuocTypography.titleLarge,
            color = DuocGray80
        )
        Text(
            text = "Seleccione el rango de bloques horarios y los recursos requeridos para el evento.",
            style = DuocTypography.bodyMedium,
            color = DuocGrayMedium
        )
        Text(
            text = "CONTINUAR AL PASO 2",
            style = DuocTypography.labelLarge,
            color = DuocBlackCharcoal
        )
    }
}