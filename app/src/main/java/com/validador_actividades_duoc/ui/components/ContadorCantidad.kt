package com.validador_actividades_duoc.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.ui.theme.DuocBlackCharcoal
import com.validador_actividades_duoc.ui.theme.DuocTheme
import com.validador_actividades_duoc.ui.theme.DuocYellow

/**
 * Componente atómico e interactivo para ajustar la cantidad solicitada de un recurso
 *
 * Muestra el nombre del recurso, el stock máximo disponible y controles
 * que respetan automáticamente los límites permitidos (mínimo 0, máximo stockDisponible).
 *
 * @param nombreRecurso Nombre descriptivo del ítem (ej. "Micrófono Inalámbrico")
 * @param cantidadActual Cantidad de unidades actualmente seleccionadas
 * @param stockDisponible Unidades totales disponibles para la fecha y bloque elegidos
 * @param alCambiarCantidad Callback que devuelve la nueva cantidad deseada
 * @param modificador Modificador de layout opcional
 */
@Composable
fun ContadorCantidad(
    nombreRecurso: String,
    cantidadActual: Int,
    stockDisponible: Int,
    alCambiarCantidad: (Int) -> Unit,
    modificador: Modifier = Modifier
) {
    Card(
        modifier = modificador.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = nombreRecurso,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Disponible: $stockDisponible unid.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                IconButton(
                    onClick = { alCambiarCantidad(cantidadActual - 1) },
                    enabled = cantidadActual > 0,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Disminuir cantidad de $nombreRecurso"
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (cantidadActual > 0) DuocYellow else MaterialTheme.colorScheme.surface,
                    contentColor = if (cantidadActual > 0) DuocBlackCharcoal else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(width = 40.dp, height = 36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = cantidadActual.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = { alCambiarCantidad(cantidadActual + 1) },
                    enabled = cantidadActual < stockDisponible,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar cantidad de $nombreRecurso"
                    )
                }
            }
        }
    }
}

/**
 * Vista previa para verificar el contador en sus estados
 * sin selección, con unidades seleccionadas y al límite del stock
 * en ambos temas claro y oscuro
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
fun ContadorCantidadPreview() {
    DuocTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ej nada seleccionado
                ContadorCantidad(
                    nombreRecurso = "Proyector HDMI",
                    cantidadActual = 0,
                    stockDisponible = 4,
                    alCambiarCantidad = {}
                )

                // ej 3 seleccionados de 10
                ContadorCantidad(
                    nombreRecurso = "Micrófono Inalámbrico",
                    cantidadActual = 3,
                    stockDisponible = 10,
                    alCambiarCantidad = {}
                )

                // ej de limite alcanzado
                ContadorCantidad(
                    nombreRecurso = "Puntero Laser Presentador",
                    cantidadActual = 5,
                    stockDisponible = 5,
                    alCambiarCantidad = {}
                )
            }
        }
    }
}
