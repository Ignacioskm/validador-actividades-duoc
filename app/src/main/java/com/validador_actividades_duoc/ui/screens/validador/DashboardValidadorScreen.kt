package com.validador_actividades_duoc.ui.screens.validador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import com.validador_actividades_duoc.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.domain.util.BLOQUES
import com.validador_actividades_duoc.ui.components.FormTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardValidadorScreen(
    viewModel: ValidadorViewModel,
    onCerrarSesion: () -> Unit,
    onIrAInventarioClick: () -> Unit
) {
    val solicitudes by viewModel.solicitudes.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var solicitudARechazar by remember { mutableStateOf<SolicitudArea?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(Modifier.fillMaxWidth().background(DuocBlackCharcoal)) {
                TopAppBar(
                    title = {
                        Column {
                            Text("VALI-DUOC", color = DuocYellow, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Panel de validación", color = DuocWhite, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(viewModel.departamento.etiqueta, color = DuocGrayLight, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    actions = {
                        IconButton(onClick = onCerrarSesion) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = DuocYellow)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DuocBlackCharcoal)
                )
                Button(
                    onClick = onIrAInventarioClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DuocYellow, contentColor = DuocBlackCharcoal)
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gestionar inventario", fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(thickness = 3.dp, color = DuocYellow)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, DuocGrayMedium.copy(alpha = 0.25f))
    ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.background(DuocYellow.copy(alpha = 0.22f), RoundedCornerShape(10.dp)).padding(12.dp)) {
                        Text(solicitudes.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Pendientes por revisar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Solicitudes de tu departamento", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (solicitudes.isEmpty()) {
                Text("No tienes solicitudes pendientes.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(12.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(solicitudes, key = { it.reserva.id }) { solicitud ->
                        SolicitudCard(
                            solicitud = solicitud,
                            onAprobar = { viewModel.aprobar(solicitud.reserva.id) },
                            onRechazar = { solicitudARechazar = solicitud }
                        )
                    }
                }
            }
        }
    }

    solicitudARechazar?.let { solicitud ->
        var motivo by remember(solicitud.reserva.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { solicitudARechazar = null },
            title = { Text("Rechazar solicitud") },
            text = {
                FormTextField(
                    valor = motivo,
                    alCambiarValor = { motivo = it },
                    etiqueta = "Motivo del rechazo",
                    lineaUnica = false
                )
            },
            confirmButton = {
                Button(
                    enabled = motivo.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejected, contentColor = DuocWhite),
                    onClick = {
                        viewModel.rechazar(solicitud.reserva.id, motivo)
                        solicitudARechazar = null
                    }
                ) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = { solicitudARechazar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SolicitudCard(
    solicitud: SolicitudArea,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    val reserva = solicitud.reserva
    val desde = BLOQUES[reserva.bloqueInicio - 1].inicio
    val hasta = BLOQUES[reserva.bloqueFin - 1].fin

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, DuocGrayMedium.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.width(4.dp).height(40.dp).background(DuocYellow, RoundedCornerShape(4.dp)))
                Text(reserva.nombreActividad, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("Solicita: ${solicitud.solicitante.nombre}", style = MaterialTheme.typography.bodyMedium)
            Text(solicitud.solicitante.correo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.fillMaxWidth().background(DuocYellow.copy(alpha = 0.14f), RoundedCornerShape(10.dp)).padding(12.dp)) {
                Text(reserva.fecha, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("$desde a $hasta · Bloques ${reserva.bloqueInicio}-${reserva.bloqueFin}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text("Recursos solicitados", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            solicitud.items.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(10.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.recurso.nombreItem, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Stock total: ${item.recurso.stockTotal}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(Modifier.background(DuocYellow.copy(alpha = 0.22f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Text("×${item.requerimiento.cantidadSolicitada}", fontWeight = FontWeight.Bold)
                    }
                }
            }
            com.validador_actividades_duoc.ui.components.AdjuntosLista(adjuntos = solicitud.adjuntos)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAprobar, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = DuocYellow, contentColor = DuocBlackCharcoal)) { Text("Aprobar", fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = onRechazar, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, StatusRejected), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Rechazar") }
            }
        }
    }
}
