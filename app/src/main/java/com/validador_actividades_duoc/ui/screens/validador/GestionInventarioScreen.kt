package com.validador_actividades_duoc.ui.screens.validador

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.validador_actividades_duoc.ui.theme.*
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.ui.components.FormTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionInventarioScreen(
    viewModel: ValidadorViewModel,
    onVolver: () -> Unit
) {
    val inventario by viewModel.inventario.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarNuevo by remember { mutableStateOf(false) }
    var recursoARetirar by remember { mutableStateOf<RecursoInventarioEntity?>(null) }
    var recursoAEditar by remember { mutableStateOf<RecursoInventarioEntity?>(null) }

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
                            Text("Inventario del área", color = DuocWhite, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(viewModel.departamento.etiqueta, color = DuocGrayLight, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = DuocYellow)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DuocBlackCharcoal)
                )
                HorizontalDivider(thickness = 3.dp, color = DuocYellow)
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { mostrarNuevo = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar recurso", fontWeight = FontWeight.Bold) },
                containerColor = DuocYellow,
                contentColor = DuocBlackCharcoal,
                shape = RoundedCornerShape(16.dp)
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
            Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, DuocGrayMedium.copy(alpha = 0.25f))
    ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.background(DuocYellow.copy(alpha = 0.22f), RoundedCornerShape(10.dp)).padding(12.dp)) {
                        Text(inventario.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Recursos registrados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Inventario de tu departamento", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (inventario.isEmpty()) {
                Text("Aún no tienes recursos. Usa el botón + para agregar.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 84.dp)) {
                items(inventario, key = { it.id }) { recurso ->
                    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, DuocGrayMedium.copy(alpha = 0.25f))
    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(4.dp).height(32.dp).background(DuocYellow, RoundedCornerShape(4.dp)))
                                Text(recurso.nombreItem, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.weight(1f).background(DuocYellow.copy(alpha = 0.16f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                                    Text("Stock total: ${recurso.stockTotal} unidades", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { recursoAEditar = recurso }, modifier = Modifier.background(DuocYellow.copy(alpha = 0.18f), RoundedCornerShape(10.dp))) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar stock", tint = MaterialTheme.colorScheme.onSurface)
                                }
                                IconButton(onClick = { recursoARetirar = recurso }, modifier = Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(10.dp))) {
                                    Icon(Icons.Default.Delete, contentDescription = "Retirar recurso", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    recursoARetirar?.let { recurso ->
        AlertDialog(
            onDismissRequest = { recursoARetirar = null },
            title = { Text("Retirar recurso") },
            text = { Text("¿Quieres retirar ${recurso.nombreItem} del inventario de tu área?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.retirarRecurso(recurso.id)
                    recursoARetirar = null
                }, colors = ButtonDefaults.buttonColors(containerColor = StatusRejected, contentColor = DuocWhite)) { Text("Retirar") }
            },
            dismissButton = { TextButton(onClick = { recursoARetirar = null }) { Text("Cancelar") } }
        )
    }

    if (mostrarNuevo) {
        var nombre by remember { mutableStateOf("") }
        var stock by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { mostrarNuevo = false },
            title = { Text("Nuevo recurso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(valor = nombre, alCambiarValor = { nombre = it }, etiqueta = "Nombre del recurso")
                    FormTextField(
                        valor = stock,
                        alCambiarValor = { stock = it.filter(Char::isDigit) },
                        etiqueta = "Stock total",
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = nombre.isNotBlank() && stock.toIntOrNull() != null,
                    colors = ButtonDefaults.buttonColors(containerColor = DuocYellow, contentColor = DuocBlackCharcoal),
                    onClick = {
                        viewModel.crearRecurso(nombre, stock.toIntOrNull() ?: 0)
                        mostrarNuevo = false
                    }
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { mostrarNuevo = false }) { Text("Cancelar") } }
        )
    }

    recursoAEditar?.let { recurso ->
        var nuevoStock by remember(recurso.id) { mutableStateOf(recurso.stockTotal.toString()) }
        AlertDialog(
            onDismissRequest = { recursoAEditar = null },
            title = { Text("Stock de ${recurso.nombreItem}") },
            text = {
                FormTextField(
                    valor = nuevoStock,
                    alCambiarValor = { nuevoStock = it.filter(Char::isDigit) },
                    etiqueta = "Stock total",
                    opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(
                    enabled = nuevoStock.toIntOrNull() != null,
                    colors = ButtonDefaults.buttonColors(containerColor = DuocYellow, contentColor = DuocBlackCharcoal),
                    onClick = {
                        viewModel.editarStock(recurso.id, nuevoStock.toIntOrNull() ?: recurso.stockTotal)
                        recursoAEditar = null
                    }
                ) { Text("Actualizar") }
            },
            dismissButton = { TextButton(onClick = { recursoAEditar = null }) { Text("Cancelar") } }
        )
    }
}
