package com.validador_actividades_duoc.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.ui.theme.DuocBlackCharcoal
import com.validador_actividades_duoc.ui.theme.DuocTheme
import com.validador_actividades_duoc.ui.theme.StatusApproved
import com.validador_actividades_duoc.ui.theme.StatusPending
import com.validador_actividades_duoc.ui.theme.StatusRejected

/**
 * Muestra visualmente la condición de un estado de un requerimiento
 * (ej. un insumo solicitado) mediante un badge o cápsula de color.
 *
 * @param estado Estado del requerimiento (Pendiente, Aprobado, Rechazado)
 * @param modificador Modificador de layout para personalizar posicionamiento o margen
 */
@Composable
fun StatusBadge(
    estado: EstadoRequerimiento,
    modificador: Modifier = Modifier
) {
    val (colorFondo, colorTexto, nomEstado) = when (estado) {
        EstadoRequerimiento.PENDIENTE -> Triple(StatusPending, DuocBlackCharcoal, "Pendiente")
        EstadoRequerimiento.APROBADO -> Triple(StatusApproved, DuocBlackCharcoal, "Aprobado")
        EstadoRequerimiento.RECHAZADO -> Triple(StatusRejected, DuocBlackCharcoal, "Rechazado")
    }

    StatusBadgeContent(
        colorFondo = colorFondo,
        colorTexto = colorTexto,
        nomEstado = nomEstado,
        modificador = modificador
    )
}

/**
 * Sobrecarga de [StatusBadge] para mostrar visualmente la condición de un estado de una reserva
 *
 * @param estado Estado de la reserva (Pendiente, Confirmada, Rechazada)
 * @param modificador Modificador de layout para personalizar posicionamiento o margen
 */
@Composable
fun StatusBadge(
    estado: EstadoReserva,
    modificador: Modifier = Modifier
) {
    val (colorFondo, colorTexto, nomEstado) = when (estado) {
        EstadoReserva.PENDIENTE -> Triple(StatusPending, DuocBlackCharcoal, "Pendiente")
        EstadoReserva.CONFIRMADA -> Triple(StatusApproved, DuocBlackCharcoal, "Confirmada")
        EstadoReserva.RECHAZADA -> Triple(StatusRejected, DuocBlackCharcoal, "Rechazada")
    }

    StatusBadgeContent(
        colorFondo = colorFondo,
        colorTexto = colorTexto,
        nomEstado = nomEstado,
        modificador = modificador
    )
}

/**
 * Contenedor que dibuja la cápsula visual del Badge
 * Reutilizado internamente por ambas sobrecargas
 *
 * @param colorFondo Color de relleno de la cápsula
 * @param colorTexto Color con el que se imprimirá la etiqueta
 * @param nomEstado Palabra descriptiva que se mostrará estado actual
 * @param modificador Modificador de layout aplicado al badge
 */
@Composable
private fun StatusBadgeContent(
    colorFondo: Color,
    colorTexto: Color,
    nomEstado: String,
    modificador: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modificador
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = nomEstado,
            style = MaterialTheme.typography.labelMedium,
            color = colorTexto
        )
    }
}

/**
 * Vista previa que verifica el renderizado de todos los estados
 * tanto para Requerimientos como para Reservas
 * evaluando el comportamiento en ambos temas claro y oscuro
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
fun StatusBadgePreview() {
    DuocTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Previews para Estados de Requerimiento
                Text(
                    text = "Estados de Requerimiento",
                    style = MaterialTheme.typography.titleSmall
                )
                StatusBadge(estado = EstadoRequerimiento.PENDIENTE)
                StatusBadge(estado = EstadoRequerimiento.APROBADO)
                StatusBadge(estado = EstadoRequerimiento.RECHAZADO)


                Text(
                    text = "Estados de Reserva",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
                StatusBadge(estado = EstadoReserva.PENDIENTE)
                StatusBadge(estado = EstadoReserva.CONFIRMADA)
                StatusBadge(estado = EstadoReserva.RECHAZADA)
            }
        }
    }
}