package com.validador_actividades_duoc.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.domain.util.ResultadoAnticipacion
import com.validador_actividades_duoc.ui.theme.DuocTheme

/**
 * Componente que muestra una tarjeta informativa sobre las reglas de anticipación (48h / 14 días).
 */
@Composable
fun BannerAnticipacion(
    resultado: ResultadoAnticipacion,
    modifier: Modifier = Modifier
) {
    val (colorFondo, colorTexto, icono, mensaje) = when (resultado) {
        ResultadoAnticipacion.BLOQUEADA -> DatosBanner(
            colorFondo = MaterialTheme.colorScheme.errorContainer,
            colorTexto = MaterialTheme.colorScheme.onErrorContainer,
            icono = Icons.Default.Error,
            mensaje = "Reserva bloqueada. Debe solicitar las reservas con un mínimo de 48 horas de anticipación."
        )
        ResultadoAnticipacion.ADVERTENCIA -> DatosBanner(
            colorFondo = Color(0xFFFFF3CD),
            colorTexto = Color(0xFF856404),
            icono = Icons.Default.Warning,
            mensaje = "Su pedido de reserva tiene menos de 14 días de antelación. Esta solicitud requerirá revisión especial."
        )
        ResultadoAnticipacion.OK -> DatosBanner(
            colorFondo = Color(0xFFD4EDDA),
            colorTexto = Color(0xFF155724),
            icono = Icons.Default.CheckCircle,
            mensaje = "Su reserva ha sido realizada dentro del plazo."
        )
        //no sé que ponderle acá, quizá después cambie los mensajes. Necesito verlos en contexto para ver si son claros
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorTexto
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = colorTexto,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class DatosBanner(
    val colorFondo: Color,
    val colorTexto: Color,
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val mensaje: String
)

/**
 * Vista previa que permite inspeccionar los 3 estados del BannerAnticipacion
 * (OK, ADVERTENCIA y BLOQUEADA) tanto en modo claro como en modo oscuro
 */
@Preview(showBackground = true, name = "Banner Anticipación - Modo Claro")
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Banner Anticipación - Modo Oscuro"
)
@Composable
fun BannerAnticipacionPreview() {
    DuocTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Estado 1: Cumple plazo ideal (+14 días)
                BannerAnticipacion(resultado = ResultadoAnticipacion.OK)

                // Estado 2: Advertencia de revisión especial (48h a 14 días)
                BannerAnticipacion(resultado = ResultadoAnticipacion.ADVERTENCIA)

                // Estado 3: Solicitud bloqueada (-48 horas)
                BannerAnticipacion(resultado = ResultadoAnticipacion.BLOQUEADA)
            }
        }
    }
}