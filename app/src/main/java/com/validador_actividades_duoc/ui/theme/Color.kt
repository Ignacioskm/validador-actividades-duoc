package com.validador_actividades_duoc.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Colores de Marca DuocUC
 * Descripción: Los colores de este primer apartado definen la identidad visual de la aplicación
 */

/**
 * Amarillo oficial
 * -Uso principal: Botones primarios, acentos, indicadores de selección activos
 */
val DuocYellow = Color(0xFFFFB800)

/**
 * Variante de amarillo
 * -Uso principal: ser la variante alternativa de amarillo para bordes
 */
val DuocYellowAlt = Color(0xFFFFB81C)

/**
 * Tono oscuro de amarillo
 * -Uso principal: para estados presionados o enfocados (Hover/Pressed)
 */
val DuocYellowDark = Color(0xFFE5A10A)

/**
 * Negro Charcoal
 * -Uso principal: Fondos oscuros, barras superiores (TopAppBar) y texto sobre amarillo
 */
val DuocBlackCharcoal = Color(0xFF1A1A1A)

/**
 * Negro puro
 * -Uso principal: íconos de alto contraste o bordes específicos que requieran
 * separación estricta sobre fondos claros
 */
val DuocBlackPuro = Color(0xFF000000)


/*
 * Colores Neutros y de superficie
 * Los colores de este segundo apartado evitan que la pantalla se sienta recargada
 *  y crean jerarquía visual al organizar la información
 */

/**
 * Blanco puro
 * -Uso: Fondo de tarjetas (Cards), campos de texto e íconos sobre fondo oscuro
 */
val DuocWhite = Color(0xFFFFFFFF)

/**
 * Gris muy oscuro
 * -Uso: Texto principal de lectura dentro de la aplicación
 */
val DuocGray80 = Color(0xFF333333)

/**
 * Gris secundario
 * -Uso: Textos secundarios, etiquetas de menor jerarquía y subtítulos
 */
val DuocGrayMedium = Color(0xFF666666)

/**
 * Gris claro
 * -Uso: para bordes y contenedores neutros
 */
val DuocGrayLight = Color(0xFFEEEEEE)

/**
 * Color claro principal de la aplicación
 * -Uso: Fondo general claro de las pantallas
 */
val DuocBackgroundLight = Color(0xFFF5F5F5)


/*
 * Colores Semánticos y de estado
 * Los colores de este tercer apartado comunican al usuario el estado de su solicitud
 * de forma inmediata mediante un código de colores intuitivo (semáforo)
 */

/**
 * Estado PENDIENTE: Ámbar
 * -Uso: Indica que una reserva o requerimiento está a la espera de validación
 */
val StatusPending = Color(0xFFF59E0B)

/**
 * Estado APROBADO / CONFIRMADO: Verde
 * -Uso: Indica que un requerimiento o evento fue autorizado
 */
val StatusApproved = Color(0xFF43B02A)

/**
 * Estado RECHAZADO: Rojo
 * -Uso: Indica que un requerimiento o evento fue rechazado por un validador
 */
val StatusRejected = Color(0xFFD32F2F)


/**
 * Función de vista previa para inspeccionar los colores de la aplicación
 */
@Preview(showBackground = true, name = "Muestra de Colores DuocUC")
@Composable
fun MuestraColoresPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "1. Colores de Marca", fontSize = 16.sp)
        FilaColorPrueba(nombre = "DuocYellow (#FFB800)", color = DuocYellow, textoColor = DuocBlackCharcoal)
        FilaColorPrueba(nombre = "DuocYellowAlt (#FFB81C)", color = DuocYellowAlt, textoColor = DuocBlackCharcoal)
        FilaColorPrueba(nombre = "DuocYellowDark (#E5A10A)", color = DuocYellowDark, textoColor = DuocWhite)
        FilaColorPrueba(nombre = "DuocBlackCharcoal (#1A1A1A)", color = DuocBlackCharcoal, textoColor = DuocWhite)

        Text(text = "2. Neutros y Superficie", fontSize = 16.sp)
        FilaColorPrueba(nombre = "DuocBackgroundLight (#F5F5F5)", color = DuocBackgroundLight, textoColor = DuocBlackCharcoal)
        FilaColorPrueba(nombre = "DuocWhite (#FFFFFF)", color = DuocWhite, textoColor = DuocBlackCharcoal)
        FilaColorPrueba(nombre = "DuocGray80 (#333333)", color = DuocGray80, textoColor = DuocWhite)
        FilaColorPrueba(nombre = "DuocGrayMedium (#666666)", color = DuocGrayMedium, textoColor = DuocWhite)

        Text(text = "3. Semánticos y de Estados", fontSize = 16.sp)
        FilaColorPrueba(nombre = "StatusPending (Ámbar)", color = StatusPending, textoColor = DuocWhite)
        FilaColorPrueba(nombre = "StatusApproved (Verde)", color = StatusApproved, textoColor = DuocWhite)
        FilaColorPrueba(nombre = "StatusRejected (Rojo)", color = StatusRejected, textoColor = DuocWhite)
    }
}

@Composable
private fun FilaColorPrueba(nombre: String, color: Color, textoColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(color)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = nombre, color = textoColor, fontSize = 13.sp)
    }
}
