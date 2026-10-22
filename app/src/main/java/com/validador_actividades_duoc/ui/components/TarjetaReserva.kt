package com.validador_actividades_duoc.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.ui.theme.DuocTheme

/**
 * Tarjeta de resumen para visualizar una solicitud o reserva de actividad
 *
 * Integra [StatusBadge] para reflejar el estado actual (PENDIENTE, CONFIRMADA, RECHAZADA)
 * y muestra la información clave de la reserva (nombre, fecha, horario y cantidad de insumos)
 *
 * @param nombreActividad Título o motivo de la reserva
 * @param fechaHoraTexto Texto formateado con la fecha y rango de bloques
 * @param estado Estado actual de la reserva según [EstadoReserva]
 * @param cantidadInsumos Total de recursos o materiales solicitados
 * @param alHacerClic Callback opcional al presionar la tarjeta para ver el detalle
 * @param modificador Modificador de layout opcional
 */
@Composable
fun TarjetaReserva(
    nombreActividad: String,
    fechaHoraTexto: String,
    estado: EstadoReserva,
    cantidadInsumos: Int,
    alHacerClic: () -> Unit = {},
    modificador: Modifier = Modifier
) {
    Card(
        modifier = modificador
            .fillMaxWidth()
            .clickable { alHacerClic() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nombreActividad,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(estado = estado)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = fechaHoraTexto,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (cantidadInsumos == 1) "1 insumo solicitado" else "$cantidadInsumos insumos solicitados",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Sobrecarga que acepta directamente un objeto [ReservaConRequerimientos]
 * para facilitar su uso directo en listados como el de CalendarioScreen
 *
 * @param reservaConReq Objeto entidad con la reserva y sus requerimientos de la BD
 * @param alHacerClic Callback opcional al presionar la tarjeta
 * @param modificador Modificador de layout opcional
 */
@Composable
fun TarjetaReserva(
    reservaConReq: ReservaConRequerimientos,
    alHacerClic: () -> Unit = {},
    modificador: Modifier = Modifier
) {
    val reserva = reservaConReq.reserva
    TarjetaReserva(
        nombreActividad = reserva.nombreActividad,
        fechaHoraTexto = "Fecha: ${reserva.fecha} | B${reserva.bloqueInicio} al B${reserva.bloqueFin}",
        estado = reserva.estado,
        cantidadInsumos = reservaConReq.requerimientos.size,
        alHacerClic = alHacerClic,
        modificador = modificador
    )
}

/**
 * Vista previa con ejemplos de tarjetas de reservas en distintos estados
 * tanto en modo claro como en modo oscuro
 */
@Preview(showBackground = true, name = "Modo Claro")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Modo Oscuro")
@Composable
fun TarjetaReservaPreview() {
    DuocTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaReserva(
                    nombreActividad = "Charla de Inducción Alumnos",
                    fechaHoraTexto = "Fecha: 2026-10-25 | B1 al B3",
                    estado = EstadoReserva.PENDIENTE,
                    cantidadInsumos = 3
                )

                TarjetaReserva(
                    nombreActividad = "Taller de Programación Kotlin",
                    fechaHoraTexto = "Fecha: 2026-10-26 | B5 al B6",
                    estado = EstadoReserva.CONFIRMADA,
                    cantidadInsumos = 1
                )

                TarjetaReserva(
                    nombreActividad = "Reunión Delegados de Carrera",
                    fechaHoraTexto = "Fecha: 2026-10-28 | B8 al B8",
                    estado = EstadoReserva.RECHAZADA,
                    cantidadInsumos = 0
                )
            }
        }
    }
}
