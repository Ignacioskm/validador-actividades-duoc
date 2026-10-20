package com.validador_actividades_duoc.ui.screens.validador

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardValidadorScreen(
    viewModel: ValidadorViewModel,
    onCerrarSesion: () -> Unit,
    onIrAInventarioClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Validador de Área") },
                actions = {
                    TextButton(onClick = onIrAInventarioClick) { Text("Inventario") }
                    TextButton(onClick = onCerrarSesion) { Text("Salir") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Solicitudes pendientes de revisión", style = MaterialTheme.typography.titleLarge)

            // Aquí irá la lista de requerimientos filtrados por departamento
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Ejemplo: Solicitud Auditorio", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Estado: Pendiente", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { /* Aprobar */ }) { Text("Aprobar") }
                        OutlinedButton(onClick = { /* Rechazar con diálogo de motivo */ }) { Text("Rechazar") }
                    }
                }
            }
        }
    }
}