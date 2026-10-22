package com.validador_actividades_duoc.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.util.BLOQUES
import com.validador_actividades_duoc.ui.components.FormTextField
import com.validador_actividades_duoc.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenEventosScreen(
    onCerrarSesion: () -> Unit,
    viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(LocalContext.current))
) {
    var pestañaSeleccionada by remember { mutableIntStateOf(0) }
    val pendientesAdmin by viewModel.solicitudesPendientesAdmin.collectAsState()
    val confirmados by viewModel.eventosConfirmados.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val inventario by viewModel.inventarioGlobal.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var reservaARechazar by remember { mutableStateOf<ReservaConRequerimientos?>(null) }
    var recursoAEditar by remember { mutableStateOf<RecursoInventarioEntity?>(null) }
    var mostrarNuevoRecurso by remember { mutableStateOf(false) }

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
                        Text("Panel Administrador", style = MaterialTheme.typography.titleLarge)
                        Text("Gestión General Duoc UC", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                actions = {
                    TextButton(onClick = onCerrarSesion) {
                        Text("Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = pestañaSeleccionada) {
                Tab(
                    selected = pestañaSeleccionada == 0,
                    onClick = { pestañaSeleccionada = 0 },
                    text = {
                        Text("Por Confirmar (${pendientesAdmin.size})")
                    }
                )
                Tab(
                    selected = pestañaSeleccionada == 1,
                    onClick = { pestañaSeleccionada = 1 },
                    text = { Text("Agenda (${confirmados.size})") }
                )
                Tab(
                    selected = pestañaSeleccionada == 2,
                    onClick = { pestañaSeleccionada = 2 },
                    text = { Text("Inventario Global") }
                )
            }

            when (pestañaSeleccionada) {
                0 -> ListaPendientesAdmin(
                    pendientes = pendientesAdmin,
                    onConfirmar = { viewModel.confirmarEvento(it.reserva.id) },
                    onRechazar = { reservaARechazar = it }
                )

                1 -> ListaEventosConfirmados(
                    busqueda = busqueda,
                    onBusquedaChange = viewModel::onBusquedaChanged,
                    eventos = confirmados
                )

                2 -> GestionInventarioGlobal(
                    inventario = inventario,
                    onEditarStock = { recursoAEditar = it },
                    onNuevoRecursoClick = { mostrarNuevoRecurso = true }
                )
            }
        }
    }

    // Diálogo de Rechazo por Admin
    reservaARechazar?.let { reservaConReq ->
        var motivo by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reservaARechazar = null },
            title = { Text("Rechazar Actividad") },
            text = {
                FormTextField(
                    valor = motivo,
                    alCambiarValor = { motivo = it },
                    etiqueta = "Motivo del rechazo general",
                    lineaUnica = false
                )
            },
            confirmButton = {
                Button(
                    enabled = motivo.isNotBlank(),
                    onClick = {
                        viewModel.rechazarEvento(reservaConReq.reserva.id, motivo)
                        reservaARechazar = null
                    }
                ) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = { reservaARechazar = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de Edición de Stock
    recursoAEditar?.let { recurso ->
        var nuevoStock by remember(recurso.id) { mutableStateOf(recurso.stockTotal.toString()) }
        AlertDialog(
            onDismissRequest = { recursoAEditar = null },
            title = { Text("Editar stock (${recurso.departamento.etiqueta})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(recurso.nombreItem, style = MaterialTheme.typography.titleMedium)
                    FormTextField(
                        valor = nuevoStock,
                        alCambiarValor = { nuevoStock = it.filter(Char::isDigit) },
                        etiqueta = "Nuevo stock total",
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = nuevoStock.isNotBlank(),
                    onClick = {
                        viewModel.editarStockGlobal(recurso.id, nuevoStock.toIntOrNull() ?: recurso.stockTotal)
                        recursoAEditar = null
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { recursoAEditar = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de Crear Recurso para Cualquier Área
    if (mostrarNuevoRecurso) {
        var departamentoSel by remember { mutableStateOf(Departamento.AUXILIARES) }
        var nombre by remember { mutableStateOf("") }
        var stock by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = { mostrarNuevoRecurso = false },
            title = { Text("Agregar Recurso a Departamento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Selecciona Departamento:", style = MaterialTheme.typography.bodyMedium)
                    DropdownMenuDepartamentos(
                        seleccionado = departamentoSel,
                        onSeleccionar = { departamentoSel = it }
                    )
                    FormTextField(
                        valor = nombre,
                        alCambiarValor = { nombre = it },
                        etiqueta = "Nombre del ítem"
                    )
                    FormTextField(
                        valor = stock,
                        alCambiarValor = { stock = it.filter(Char::isDigit) },
                        etiqueta = "Stock inicial",
                        opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = nombre.isNotBlank() && stock.isNotBlank(),
                    onClick = {
                        viewModel.crearRecursoGlobal(departamentoSel, nombre, stock.toIntOrNull() ?: 1)
                        mostrarNuevoRecurso = false
                    }
                ) { Text("Crear Recurso") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarNuevoRecurso = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ListaPendientesAdmin(
    pendientes: List<ReservaConRequerimientos>,
    onConfirmar: (ReservaConRequerimientos) -> Unit,
    onRechazar: (ReservaConRequerimientos) -> Unit
) {
    if (pendientes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay solicitudes pendientes de validación final.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pendientes, key = { it.reserva.id }) { item ->
                TarjetaReservaAdmin(
                    item = item,
                    esPendiente = true,
                    onConfirmar = { onConfirmar(item) },
                    onRechazar = { onRechazar(item) }
                )
            }
        }
    }
}

@Composable
private fun ListaEventosConfirmados(
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    eventos: List<ReservaConRequerimientos>
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        FormTextField(
            valor = busqueda,
            alCambiarValor = onBusquedaChange,
            etiqueta = "Buscar actividad o profesor...",
            iconoInicio = { Icon(Icons.Default.Search, contentDescription = "Buscar") }
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (eventos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay eventos confirmados.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(eventos, key = { it.reserva.id }) { item ->
                    TarjetaReservaAdmin(item = item, esPendiente = false)
                }
            }
        }
    }
}

@Composable
private fun TarjetaReservaAdmin(
    item: ReservaConRequerimientos,
    esPendiente: Boolean,
    onConfirmar: () -> Unit = {},
    onRechazar: () -> Unit = {}
) {
    val reserva = item.reserva
    val desde = BLOQUES.getOrNull(reserva.bloqueInicio - 1)?.inicio?.toString() ?: ""
    val hasta = BLOQUES.getOrNull(reserva.bloqueFin - 1)?.fin?.toString() ?: ""

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(reserva.nombreActividad, style = MaterialTheme.typography.titleMedium)
                StatusBadge(estado = reserva.estado)
            }
            Text("Solicita: ${item.solicitante.nombre} (${item.solicitante.correo})")
            Text("Fecha: ${reserva.fecha} ($desde a $hasta)")

            HorizontalDivider()
            Text("Desglose por Departamento:", style = MaterialTheme.typography.labelLarge)

            // Agrupamos requerimientos por departamento del recurso
            val porArea = item.requerimientos.groupBy { it.recurso.departamento }
            porArea.forEach { (depto, reqs) ->
                Text("• ${depto.etiqueta}:", style = MaterialTheme.typography.bodyMedium)
                reqs.forEach { req ->
                    Text(
                        "   - ${req.recurso.nombreItem} x${req.requerimiento.cantidadSolicitada}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (esPendiente) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onConfirmar) { Text("Confirmar Actividad") }
                    OutlinedButton(onClick = onRechazar) { Text("Rechazar") }
                }
            }
        }
    }
}

@Composable
private fun GestionInventarioGlobal(
    inventario: List<RecursoInventarioEntity>,
    onEditarStock: (RecursoInventarioEntity) -> Unit,
    onNuevoRecursoClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevoRecursoClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar recurso")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val porDepartamento = inventario.groupBy { it.departamento }
            porDepartamento.forEach { (depto, recursos) ->
                item {
                    Text(
                        text = depto.etiqueta,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(recursos, key = { it.id }) { recurso ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(recurso.nombreItem, style = MaterialTheme.typography.bodyLarge)
                                Text("Stock total: ${recurso.stockTotal} unid.")
                            }
                            IconButton(onClick = { onEditarStock(recurso) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Stock")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenuDepartamentos(
    seleccionado: Departamento,
    onSeleccionar: (Departamento) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido }
    ) {
        OutlinedTextField(
            value = seleccionado.etiqueta,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            Departamento.entries.forEach { depto ->
                DropdownMenuItem(
                    text = { Text(depto.etiqueta) },
                    onClick = {
                        onSeleccionar(depto)
                        expandido = false
                    }
                )
            }
        }
    }
}