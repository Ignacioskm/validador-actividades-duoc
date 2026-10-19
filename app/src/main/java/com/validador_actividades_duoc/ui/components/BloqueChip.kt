package com.validador_actividades_duoc.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.duocuc.reservas.domain.util.BLOQUES
import com.duocuc.reservas.domain.util.BloqueHorario
import com.validador_actividades_duoc.ui.theme.DuocBlackCharcoal
import com.validador_actividades_duoc.ui.theme.DuocTheme
import com.validador_actividades_duoc.ui.theme.DuocYellow

/**
 * Chip interactivo para representar y seleccionar un bloque horario
 *
 * Utiliza el modelo [BloqueHorario] de la capa de dominio
 *
 * @param bloque Objeto BloqueHorario que contiene el número de bloque y el rango de horas
 * @param seleccionado Define si el bloque está dentro del rango seleccionado por el usuario
 * @param alSeleccionar Callback ejecutado al presionar el chip
 * @param modificador Modificador de layout opcional
 * @param habilitado Define si el bloque está disponible para selección
 */
@Composable
fun BloqueChip(
    bloque: BloqueHorario,
    seleccionado: Boolean,
    alSeleccionar: (BloqueHorario) -> Unit,
    modificador: Modifier = Modifier,
    habilitado: Boolean = true
) {
    FilterChip(
        selected = seleccionado,
        onClick = { alSeleccionar(bloque) },
        enabled = habilitado,
        label = {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Bloque ${bloque.indice}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = bloque.etiqueta,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        modifier = modificador,
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DuocYellow,
            selectedLabelColor = DuocBlackCharcoal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = habilitado,
            selected = seleccionado,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = DuocYellow,
            borderWidth = 1.dp,
            selectedBorderWidth = 2.dp
        )
    )
}



/**
 * Vista previa del componente [BloqueChip] en sus dos estados principales
 * - Deseleccionado (ej. Bloque 1)
 * - Seleccionado (ej. Bloque 2)
 * para ambos modos claro y oscuro
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
fun BloqueChipPreview() {
    DuocTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Estados de BloqueChip",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BloqueChip(
                        bloque = BLOQUES[0],
                        seleccionado = false,
                        alSeleccionar = {}
                    )
                    BloqueChip(
                        bloque = BLOQUES[1],
                        seleccionado = true,
                        alSeleccionar = {}
                    )
                }
            }
        }
    }
}