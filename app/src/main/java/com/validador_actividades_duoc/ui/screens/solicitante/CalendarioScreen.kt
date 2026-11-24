package com.validador_actividades_duoc.ui.screens.solicitante

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.domain.util.BLOQUES
import com.validador_actividades_duoc.domain.util.BloqueHorario
import com.validador_actividades_duoc.ui.components.StatusBadge
import com.validador_actividades_duoc.ui.components.TarjetaReserva
import com.validador_actividades_duoc.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Pantalla Principal del Solicitante
 *
 * Muestra la disponibilidad de bloques para la fecha seleccionada y el historial
 * de solicitudes creadas por el usuario autenticado
 *
 * @param solicitanteId ID del usuario con sesión activa
 * @param nombreUsuario Nombre para mostrar en el encabezado
 * @param onAbrirWizard Callback para navegar al Formulario Wizard
 * @param onCerrarSesion Callback para cerrar sesión y volver a la pantalla de Login
 * @param viewModel ViewModel que administra los estados de la UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    solicitanteId: Long,
    nombreUsuario: String = "Solicitante",
    onAbrirWizard: (fecha: LocalDate, bloque: Int?) -> Unit = { _, _ -> },
    onCerrarSesion: () -> Unit = {},
    viewModel: SolicitanteViewModel = viewModel(
        factory = SolicitanteViewModelFactory(
            AppContainer.provideReservaRepository(LocalContext.current),
            AppContainer.provideInventarioRepository(LocalContext.current)
        )
    )
) {
    LaunchedEffect(solicitanteId) {
        viewModel.inicializarSolicitante(solicitanteId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
            TopAppBar(
                title = {
                    Column {
                        Text("VALI-DUOC", color = DuocYellow,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold)
                        Text(
                            text = "Hola, $nombreUsuario",
                            color = DuocWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestión de Reservas Duoc",
                            style = MaterialTheme.typography.bodySmall,
                            color = DuocGrayLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = DuocYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DuocBlackCharcoal,
                    titleContentColor = DuocWhite,
                    actionIconContentColor = DuocYellow
                )
            )
            HorizontalDivider(thickness = 3.dp, color = DuocYellow)
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAbrirWizard(uiState.fechaSeleccionada, null) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva Solicitud", fontWeight = FontWeight.Bold) },
                containerColor = DuocYellow,
                contentColor = DuocBlackCharcoal,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SeccionSelectorSemana(
                fechaSeleccionada = uiState.fechaSeleccionada,
                onFechaSeleccionada = { nuevaFecha ->
                    viewModel.seleccionarFechaCalendario(nuevaFecha)
                }
            )



            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(4.dp).height(36.dp)
                            .clip(RoundedCornerShape(4.dp)).background(DuocYellow))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Disponibilidad", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text(
                                uiState.fechaSeleccionada.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "CL")))
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }


                items(BLOQUES.size) { index ->
                    val numeroBloque = index + 1
                    val bloqueHora = BLOQUES[index]

                    val reservaEnBloque = uiState.misSolicitudes.firstOrNull { req ->
                        req.reserva.fecha == uiState.fechaSeleccionada.toString() &&
                                numeroBloque in req.reserva.bloqueInicio..req.reserva.bloqueFin
                    }

                    TarjetaBloqueHorario(
                        numeroBloque = numeroBloque,
                        bloqueHora = bloqueHora,
                        reserva = reservaEnBloque,
                        onBloqueLibreClick = { onAbrirWizard(uiState.fechaSeleccionada, numeroBloque) }
                    )
                }


                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Mis Solicitudes (${uiState.misSolicitudes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (uiState.cargando) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.misSolicitudes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No tienes solicitudes registradas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toca '+ Nueva Solicitud' para crear una",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.misSolicitudes) { itemReserva ->
                        TarjetaReserva(reservaConReq = itemReserva)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

/**
 * Componente para navegar por los días de la semana
 */
@Composable
fun SeccionSelectorSemana(
    fechaSeleccionada: LocalDate,
    onFechaSeleccionada: (LocalDate) -> Unit
) {
    var inicioSemana by remember(fechaSeleccionada) {
        mutableStateOf(fechaSeleccionada.minusDays(fechaSeleccionada.dayOfWeek.value.toLong() - 1))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { inicioSemana = inicioSemana.minusWeeks(1) },
                modifier = Modifier.clip(CircleShape).background(DuocYellow.copy(alpha = 0.16f))) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Semana anterior")
            }

            Text(
                text = inicioSemana.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES")))
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { inicioSemana = inicioSemana.plusWeeks(1) },
                modifier = Modifier.clip(CircleShape).background(DuocYellow.copy(alpha = 0.16f))) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Semana siguiente")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(7) { offset ->
                val dia = inicioSemana.plusDays(offset.toLong())
                val esSeleccionado = dia == fechaSeleccionada

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (esSeleccionado) DuocYellow
                            else Color.Transparent
                        )
                        .clickable { onFechaSeleccionada(dia) }
                        .padding(horizontal = 2.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES")).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (esSeleccionado) DuocBlackCharcoal else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dia.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (esSeleccionado) DuocBlackCharcoal else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.size(4.dp).clip(CircleShape).background(
                        if (esSeleccionado) DuocBlackCharcoal else Color.Transparent))
                }
            }
        }
    }
}

/**
 * Fila individual que representa cada uno de los 13 bloques horarios del día
 */
@Composable
fun TarjetaBloqueHorario(
    numeroBloque: Int,
    bloqueHora: BloqueHorario,
    reserva: ReservaConRequerimientos?,
    onBloqueLibreClick: () -> Unit
) {
    val estaOcupado = reserva != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !estaOcupado) { onBloqueLibreClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (estaOcupado) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        //aguregué este borde porque sino los bloques horarios se perdian con el fondo
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (estaOcupado) MaterialTheme.colorScheme.surfaceVariant
                        else DuocYellow.copy(alpha = 0.22f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B$numeroBloque",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${bloqueHora.inicio} - ${bloqueHora.fin}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (estaOcupado) reserva!!.reserva.nombreActividad else "Disponible · Toca para solicitar",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (estaOcupado) FontWeight.Bold else FontWeight.Normal,
                    color = if (estaOcupado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (estaOcupado) {
                StatusBadge(estado = reserva!!.reserva.estado)
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(showBackground = true, name = "Calendario - Modo Claro")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Calendario - Modo Oscuro")
@Composable
fun CalendarioScreenPreview() {
    DuocTheme {
        Surface {
            CalendarioScreen(
                solicitanteId = 1L,
                nombreUsuario = "Carlos Gómez",
                onAbrirWizard = { _, _ -> },
                onCerrarSesion = {}
            )
        }
    }
}
