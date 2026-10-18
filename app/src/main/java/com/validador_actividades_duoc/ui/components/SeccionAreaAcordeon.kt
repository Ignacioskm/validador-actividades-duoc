package com.validador_actividades_duoc.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.ui.theme.DuocTheme

/**
 * Componente acordeón desplegable para agrupar insumos por Departamento/Área
 *
 * @param departamento Área responsable de los insumos
 * @param totalSeleccionados Cantidad de unidades de insumos seleccionadas en esta área
 * @param inicialmenteExpandido Define si la sección arranca abierta o cerrada
 * @param modificador Modificador de layout opcional
 * @param contenido Slot composable donde se colocan los controles [ContadorCantidad]
 */
@Composable
fun SeccionAreaAcordeon(
    departamento: Departamento,
    totalSeleccionados: Int = 0,
    inicialmenteExpandido: Boolean = false,
    modificador: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    var expandido by remember { mutableStateOf(inicialmenteExpandido) }

    val rotacionFlecha by animateFloatAsState(
        targetValue = if (expandido) 180f else 0f,
        label = "RotacionFlechaAcordeon"
    )

    Card(
        modifier = modificador.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = departamento.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (totalSeleccionados > 0) {
                            "$totalSeleccionados recurso(s) seleccionado(s)"
                        } else {
                            "Sin recursos seleccionados"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (totalSeleccionados > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        fontWeight = if (totalSeleccionados > 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
                IconButton(onClick = { expandido = !expandido }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Colapsar sección" else "Expandir sección",
                        modifier = Modifier.rotate(rotacionFlecha),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // contenido desplegable con animación de entrada/salida
            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contenido()
                }
            }
        }
    }
}

/**
 * Vista previa con ejemplos de secciones sin recursos
 * y con recursos que se ven expandidos
 * en ambos temas claro y oscuro
 */
@Preview(showBackground = true, name = "Modo Claro")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Modo Oscuro")
@Composable
fun SeccionAreaAcordeonPreview() {
    DuocTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SeccionAreaAcordeon(
                    departamento = Departamento.AUXILIARES,
                    totalSeleccionados = 0,
                    inicialmenteExpandido = false
                ) {
                    ContadorCantidad(
                        nombreRecurso = "Mesa Plegable",
                        cantidadActual = 0,
                        stockDisponible = 10,
                        alCambiarCantidad = {}
                    )
                }

                SeccionAreaAcordeon(
                    departamento = Departamento.DIGITALES,
                    totalSeleccionados = 3,
                    inicialmenteExpandido = true
                ) {
                    ContadorCantidad(
                        nombreRecurso = "Proyector HDMI",
                        cantidadActual = 1,
                        stockDisponible = 4,
                        alCambiarCantidad = {}
                    )
                    ContadorCantidad(
                        nombreRecurso = "Micrófono Inalámbrico",
                        cantidadActual = 2,
                        stockDisponible = 6,
                        alCambiarCantidad = {}
                    )
                }
            }
        }
    }
}