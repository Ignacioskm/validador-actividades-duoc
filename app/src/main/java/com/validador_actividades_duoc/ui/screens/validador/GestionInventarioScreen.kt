package com.validador_actividades_duoc.ui.screens.validador

import androidx.compose.foundation.layout.*
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
            TopAppBar(
                title = {
                    Column {
                        Text("Inventario del área", style = MaterialTheme.typography.titleLarge)
                        Text(viewModel.departamento.etiqueta, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                navigationIcon = { TextButton(onClick = onVolver) { Text("Volver") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarNuevo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar recurso")
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
            Text("Recursos (${inventario.size})", style = MaterialTheme.typography.titleLarge)

            if (inventario.isEmpty()) {
                Text("Aún no tienes recursos. Usa el botón + para agregar.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(inventario, key = { it.id }) { recurso ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(recurso.nombreItem, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Stock total: ${recurso.stockTotal} unidades",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(onClick = { recursoAEditar = recurso }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar stock")
                            }
                            IconButton(onClick = { viewModel.retirarRecurso(recurso.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Retirar recurso")
                            }
                        }
                    }
                }
            }
        }
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
                    enabled = nombre.isNotBlank() && stock.isNotBlank(),
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
                    enabled = nuevoStock.isNotBlank(),
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