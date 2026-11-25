package com.validador_actividades_duoc.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.validador_actividades_duoc.ui.theme.*
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
import androidx.navigation.NavController
import kotlinx.coroutines.delay


// Importaciones locales del proyecto
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento
import com.validador_actividades_duoc.domain.util.BLOQUES
import com.validador_actividades_duoc.ui.components.FormTextField
import com.validador_actividades_duoc.ui.components.StatusBadge

// Importaciones de las pantallas y ViewModel del Solicitante
import com.validador_actividades_duoc.ui.screens.solicitante.CalendarioScreen
import com.validador_actividades_duoc.ui.screens.solicitante.FormularioWizardScreen
import com.validador_actividades_duoc.ui.screens.solicitante.SolicitanteViewModel
import com.validador_actividades_duoc.ui.screens.solicitante.SolicitanteViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenEventosScreen(
    navController: NavController,
    adminId: Long = 1L,
    onCerrarSesion: () -> Unit,
    viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(LocalContext.current))
) {
    var pestañaSeleccionada by remember { mutableIntStateOf(0) }
    val pendientesAdmin by viewModel.solicitudesPendientesAdmin.collectAsState()
    val confirmados by viewModel.eventosConfirmados.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val inventario by viewModel.inventarioGlobal.collectAsState()
    val cargandoInventario by viewModel.cargandoInventario.collectAsState()
    val errorInventario by viewModel.errorInventario.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var reservaARechazar by remember { mutableStateOf<ReservaConRequerimientos?>(null) }
    var recursoAEditar by remember { mutableStateOf<RecursoInventarioEntity?>(null) }
    var mostrarNuevoRecurso by remember { mutableStateOf(false) }

    // --- ESTADOS Y VIEWMODEL PARA LA RESERVA DE ADMIN ---
    var mostrarCalendarioAdmin by remember { mutableStateOf(false) }
    var mostrarWizardAdmin by remember { mutableStateOf(false) }

    val solicitanteViewModel: SolicitanteViewModel = viewModel(
        factory = SolicitanteViewModelFactory(
            AppContainer.provideReservaRepository(LocalContext.current),
            AppContainer.provideInventarioRepository(LocalContext.current)
        )
    )

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    LaunchedEffect(pestañaSeleccionada) {
        if (pestañaSeleccionada == 2) {
            viewModel.cargarInventarioRemoto(silencioso = inventario.isNotEmpty())
            while (true) {
                delay(5_000)
                viewModel.cargarInventarioRemoto(silencioso = true)
            }
        }
    }


    // --- FLUIDO DE NAVEGACIÓN PARA RESERVAS DEL ADMIN ---
    if (mostrarWizardAdmin) {
        // Formulario de Reserva (Wizard de 3 pasos)
        FormularioWizardScreen(
            onVolverCalendario = { mostrarWizardAdmin = false },
            viewModel = solicitanteViewModel
        )
    } else if (mostrarCalendarioAdmin) {
        // Pantalla de Calendario del Admin
        CalendarioScreen(
            solicitanteId = adminId,
            nombreUsuario = "Administrador",
            onAbrirWizard = { fecha, bloque ->
                solicitanteViewModel.prepararWizard(fecha, bloque)
                mostrarWizardAdmin = true},
            onCerrarSesion = { mostrarCalendarioAdmin = false }, // Vuelve al Panel Admin al cerrar la vista
            viewModel = solicitanteViewModel
        )
    } else {
        // --- VISTA NORMAL DEL PANEL ADMINISTRADOR ---
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DuocBlackCharcoal)
                ) {
                    TopAppBar(
                        title = {
                            Column {
                                Text("VALI-DUOC", color = DuocYellow,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold)
                                Text("Panel Administrador", color = DuocWhite,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold)
                                Text("Gestión General Duoc UC", color = DuocGrayLight,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        actions = {
                            IconButton(onClick = onCerrarSesion) {
                                Icon(Icons.Default.ExitToApp,
                                    contentDescription = "Cerrar sesión",
                                    tint = DuocYellow)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DuocBlackCharcoal,
                            titleContentColor = DuocWhite,
                            actionIconContentColor = DuocYellow
                        )
                    )
                    Button(
                        onClick = { mostrarCalendarioAdmin = true },
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .heightIn(min = 44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = coloresAccionAdmin()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reservar", fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(thickness = 3.dp, color = DuocYellow)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(
                    selectedTabIndex = pestañaSeleccionada,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = { posiciones ->
                        TabRowDefaults.Indicator(
                            modifier = with(TabRowDefaults) {
                                Modifier.tabIndicatorOffset(posiciones[pestañaSeleccionada])
                            },
                            color = DuocYellow,
                            height = 3.dp
                        )
                    }
                ) {
                    val etiquetas = listOf(
                        "Por Confirmar (${pendientesAdmin.size})",
                        "Agenda (${confirmados.size})",
                        "Inventario Global"
                    )
                    etiquetas.forEachIndexed { indice, etiqueta ->
                        val seleccionada = pestañaSeleccionada == indice
                        Tab(
                            selected = seleccionada,
                            onClick = { pestañaSeleccionada = indice },
                            selectedContentColor = MaterialTheme.colorScheme.onSurface,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text = {
                                Text(
                                    etiqueta,
                                    modifier = Modifier.background(
                                        if (seleccionada) DuocYellow.copy(alpha = 0.18f)
                                        else androidx.compose.ui.graphics.Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    ).padding(horizontal = 6.dp, vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }

                when (pestañaSeleccionada) {
                    0 -> ListaPendientesAdmin(
                        pendientes = pendientesAdmin,
                        onConfirmar = { viewModel.confirmarEvento(it.reserva.id) },
                        onRechazar = { reservaARechazar = it },
                        onConfirmarExcepcional = { viewModel.confirmarEventoExcepcional(it.reserva.id) }
                    )

                    1 -> ListaEventosConfirmados(
                        busqueda = busqueda,
                        onBusquedaChange = viewModel::onBusquedaChanged,
                        eventos = confirmados
                    )

                    2 -> when {
                        cargandoInventario -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = DuocYellow)
                            }
                        }
                        errorInventario != null -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(errorInventario!!, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.cargarInventarioRemoto() }, colors = coloresAccionAdmin()) {
                                    Text("Reintentar")
                                }
                            }
                        }
                        else -> {
                            GestionInventarioGlobal(
                                inventario = inventario,
                                onEditarStock = { recursoAEditar = it },
                                onNuevoRecursoClick = { mostrarNuevoRecurso = true }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogos de Rechazar, Editar y Crear Recurso
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
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
                    colors = coloresAccionAdmin(),
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
                    colors = coloresAccionAdmin(),
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
    onRechazar: (ReservaConRequerimientos) -> Unit,
    onConfirmarExcepcional: (ReservaConRequerimientos) -> Unit
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
                    onRechazar = { onRechazar(item) },
                    onConfirmarExcepcional = { onConfirmarExcepcional(item) }
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
        OutlinedTextField(
            value = busqueda,
            onValueChange = onBusquedaChange,
            placeholder = { Text("Buscar actividad o profesor...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = DuocYellow,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
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
    onRechazar: () -> Unit = {},
    onConfirmarExcepcional: () -> Unit = {}
) {
    var mostrarDialogoExcepcional by remember {mutableStateOf(false) }
    val reserva = item.reserva
    val desde = BLOQUES.getOrNull(reserva.bloqueInicio - 1)?.inicio?.toString() ?: ""
    val hasta = BLOQUES.getOrNull(reserva.bloqueFin - 1)?.fin?.toString() ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila superior: Nombre de la actividad y Badge general de la Reserva
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(4.dp).height(36.dp)
                    .background(DuocYellow, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = reserva.nombreActividad,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(estado = reserva.estado) // Badge general de la Reserva
            }

            Text("Solicita: ${item.solicitante.nombre} (${item.solicitante.correo})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = DuocYellow.copy(alpha = 0.15f)
            ) {
                Text("Fecha: ${reserva.fecha} ($desde a $hasta)",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            }

            HorizontalDivider()

            Text(
                text = "Estado por Departamento:",
                style = MaterialTheme.typography.labelLarge
            )

            // Agrupamos los requerimientos solicitados por cada Departamento
            val porArea = item.requerimientos.groupBy { it.recurso.departamento }

            porArea.forEach { (depto, reqs) ->
                // Determinamos el estado del departamento según sus requerimientos
                val estadoArea = when {
                    reqs.any { it.requerimiento.estadoValidacion == EstadoRequerimiento.RECHAZADO } -> EstadoRequerimiento.RECHAZADO
                    reqs.all { it.requerimiento.estadoValidacion == EstadoRequerimiento.APROBADO } -> EstadoRequerimiento.APROBADO
                    else -> EstadoRequerimiento.PENDIENTE
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    // Encabezado del área con su StatusBadge correspondiente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${depto.etiqueta}:",
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        // BADGE INDIVIDUAL DEL DEPARTAMENTO
                        StatusBadge(estado = estadoArea)
                    }

                    // Detalle de insumos pedidos a este departamento
                    reqs.forEach { req ->
                        Text(
                            text = "   - ${req.recurso.nombreItem} x${req.requerimiento.cantidadSolicitada}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Acciones del Administrador
            if (esPendiente) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onConfirmar,
                            modifier = Modifier.weight(1f),
                            colors = coloresAccionAdmin()
                        ) { Text("Confirmar Actividad", textAlign = TextAlign.Center) }
                        OutlinedButton(
                            onClick = onRechazar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Rechazar") }
                    }
                    OutlinedButton(
                        onClick = { mostrarDialogoExcepcional = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, DuocYellow),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) { Text("Validar excepcionalmente") }
                }
            }
        }
    }
    if (mostrarDialogoExcepcional) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoExcepcional = false
            },
            title = {
                Text("Confirmación excepcional")
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas validar esta " +
                            "actividad aunque todavía no se hayan " +
                            "aprobado todos los recursos? " +
                            "Las decisiones de los departamentos " +
                            "no serán modificadas."
                )
            },
            confirmButton = {
                Button(
                    colors = coloresAccionAdmin(),
                    onClick = {
                        mostrarDialogoExcepcional = false
                        onConfirmarExcepcional()
                    }
                ) {
                    Text("Sí, validar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoExcepcional = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

}

@Composable
private fun GestionInventarioGlobal(
    inventario: List<RecursoInventarioEntity>,
    onEditarStock: (RecursoInventarioEntity) -> Unit,
    onNuevoRecursoClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevoRecursoClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar recurso") },
                containerColor = DuocYellow,
                contentColor = DuocBlackCharcoal
            )
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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(recursos, key = { it.id }) { recurso ->
                    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(recurso.nombreItem, style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold)
                                Text("Stock total: ${recurso.stockTotal} unid.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onEditarStock(recurso) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Stock", tint = MaterialTheme.colorScheme.onSurface)
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

@Composable
private fun coloresAccionAdmin() = ButtonDefaults.buttonColors(
    containerColor = DuocYellow,
    contentColor = DuocBlackCharcoal
)

