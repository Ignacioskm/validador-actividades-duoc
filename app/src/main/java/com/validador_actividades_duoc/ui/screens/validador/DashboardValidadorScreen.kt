package com.validador_actividades_duoc.ui.screens.validador

import androidx.compose.foundation.layout.*
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
            TopAppBar(
                title = {
                    Column {
                        Text("Panel de validación", style = MaterialTheme.typography.titleLarge)
                        Text(viewModel.departamento.etiqueta, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                actions = {
                    TextButton(onClick = onIrAInventarioClick) { Text("Inventario") }
                    TextButton(onClick = onCerrarSesion) { Text("Salir") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pendientes por revisar: ${solicitudes.size}",
                style = MaterialTheme.typography.titleLarge
            )

            if (solicitudes.isEmpty()) {
                Text("No tienes solicitudes pendientes.", style = MaterialTheme.typography.bodyLarge)
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

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(reserva.nombreActividad, style = MaterialTheme.typography.titleLarge)
            Text(
                "Solicita: ${solicitud.solicitante.nombre} (${solicitud.solicitante.correo})",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${reserva.fecha} · $desde a $hasta (bloques ${reserva.bloqueInicio}-${reserva.bloqueFin})",
                style = MaterialTheme.typography.bodyMedium
            )
            HorizontalDivider()
            Text("Lo que se pide a tu área:", style = MaterialTheme.typography.labelLarge)
            solicitud.items.forEach { item ->
                Text(
                    "• ${item.recurso.nombreItem} × ${item.requerimiento.cantidadSolicitada}" +
                            "  (stock total: ${item.recurso.stockTotal})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAprobar) { Text("Aprobar") }
                OutlinedButton(onClick = onRechazar) { Text("Rechazar") }
            }
        }
    }
}