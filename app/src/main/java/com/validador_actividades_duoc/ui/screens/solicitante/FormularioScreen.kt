package com.validador_actividades_duoc.ui.screens.solicitante

import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.util.BLOQUES
import com.validador_actividades_duoc.ui.components.BannerAnticipacion
import com.validador_actividades_duoc.ui.components.BloqueChip
import com.validador_actividades_duoc.ui.components.ContadorCantidad
import com.validador_actividades_duoc.ui.components.FormTextField
import com.validador_actividades_duoc.ui.components.SeccionAreaAcordeon
import com.validador_actividades_duoc.ui.theme.DuocTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.validador_actividades_duoc.ui.components.AdjuntosSection

/**
 * Pantalla Wizard de creación de reservas paso a paso para el Solicitante
 *
 * @param onVolverCalendario Callback ejecutado tras completar la reserva o cancelar
 * @param viewModel ViewModel encargado de la gestión de estados del formulario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioWizardScreen(
    onVolverCalendario: () -> Unit = {},
    viewModel: SolicitanteViewModel = viewModel(
        factory = SolicitanteViewModelFactory(
            AppContainer.provideReservaRepository(LocalContext.current),
            AppContainer.provideInventarioRepository(LocalContext.current)
        )
    )
) {
    val wizardState by viewModel.wizardState.collectAsState()

    if (wizardState.guardadoExitoso) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Solicitud Registrada", fontWeight = FontWeight.Bold) },
            text = {
                Text("Tu reserva ha sido creada. Ahora se encuentra en estado PENDIENTE hasta la confirmación de los validadores del área.")
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.reiniciarWizard()
                    onVolverCalendario()
                }) {
                    Text("Volver al Inicio")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nueva Solicitud",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Paso ${wizardState.pasoActual} de 3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (wizardState.pasoActual > 1) {
                            viewModel.pasoAnteriorWizard()
                        } else {
                            onVolverCalendario()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // indicador visual de progreso del wizard
            LinearProgressIndicator(
                progress = { wizardState.pasoActual / 3f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            wizardState.mensajeError?.let { errorMsg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.limpiarMensajeError() }) {
                            Text("OK")
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (wizardState.pasoActual) {
                    1 -> Paso1FechaYBloques(wizardState = wizardState, viewModel = viewModel)
                    2 -> Paso2RecursosEInsumos(wizardState = wizardState, viewModel = viewModel)
                    3 -> Paso3ResumenYConfirmacion(wizardState = wizardState)
                }
            }

            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (wizardState.pasoActual > 1) {
                        OutlinedButton(
                            onClick = { viewModel.pasoAnteriorWizard() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Atrás")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    Button(
                        onClick = {
                            if (wizardState.pasoActual < 3) {
                                viewModel.siguientePasoWizard()
                            } else {
                                viewModel.confirmarReserva()
                            }
                        },
                        enabled = when (wizardState.pasoActual) {
                            1 -> wizardState.esPaso1Valido
                            2 -> wizardState.esPaso2Valido
                            3 -> !wizardState.cargando
                            else -> false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        if (wizardState.cargando && wizardState.pasoActual == 3) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.height(20.dp)
                            )
                        } else {
                            Text(if (wizardState.pasoActual == 3) "Enviar Solicitud" else "Siguiente")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Paso1FechaYBloques(
    wizardState: WizardState,
    viewModel: SolicitanteViewModel
) {
    val context = LocalContext.current
    val fechaFormatted = wizardState.fecha.format(
        DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy", Locale("es", "ES"))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Datos de la Actividad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            FormTextField(
                valor = wizardState.nombreActividad,
                alCambiarValor = { viewModel.actualizarNombreActividad(it) },
                etiqueta = "Nombre de la Actividad",
                mensajeError = if (wizardState.nombreActividad.isBlank()) "Campo obligatorio" else null
            )
        }

        item {
            Column {
                Text(
                    text = "Fecha del Evento",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    onClick = {
                        val hoy = wizardState.fecha
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                viewModel.actualizarFechaReserva(LocalDate.of(year, month + 1, dayOfMonth))
                            },
                            hoy.year,
                            hoy.monthValue - 1,
                            hoy.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = fechaFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            BannerAnticipacion(resultado = wizardState.resultadoAnticipacion)
        }

        item {
            Column {
                Text(
                    text = "Bloques Horarios (Toca para seleccionar inicio y fin)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BLOQUES.size) { index ->
                        val numeroBloque = index + 1
                        val esSeleccionado = numeroBloque in wizardState.bloqueInicio..wizardState.bloqueFin

                        BloqueChip(
                            bloque = BLOQUES[index],
                            seleccionado = esSeleccionado,
                            alSeleccionar = {
                                if (wizardState.bloqueInicio == wizardState.bloqueFin && numeroBloque > wizardState.bloqueInicio) {
                                    viewModel.seleccionarRangoBloques(wizardState.bloqueInicio, numeroBloque)
                                } else {
                                    viewModel.seleccionarRangoBloques(numeroBloque, numeroBloque)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rango seleccionado: Bloque B${wizardState.bloqueInicio} al Bloque B${wizardState.bloqueFin}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            AdjuntosSection(
                adjuntos = wizardState.adjuntos,
                alAgregar = viewModel::agregarAdjunto,
                alQuitar = viewModel::quitarAdjunto
            )
        }

    }
}

@Composable
private fun Paso2RecursosEInsumos(
    wizardState: WizardState,
    viewModel: SolicitanteViewModel
) {
    val recursosPorDepto = wizardState.recursosDisponibles.groupBy { it.departamento }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Requerimientos e Insumos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecciona la cantidad de insumos requeridos por departamento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (recursosPorDepto.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            recursosPorDepto.forEach { (departamento, listaRecursos) ->
                val itemsSeleccionadosEnDepto = listaRecursos.sumOf { rec ->
                    wizardState.cantidadesInsumos[rec.id] ?: 0
                }

                item {
                    SeccionAreaAcordeon(
                        departamento = departamento,
                        totalSeleccionados = itemsSeleccionadosEnDepto
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listaRecursos.forEach { recurso ->
                                val cantidadPedida = wizardState.cantidadesInsumos[recurso.id] ?: 0

                                // Obtenemos el stock real calculado (o el total por defecto si aún no carga)
                                val stockRealDisponible = wizardState.stockDisponibles[recurso.id] ?: recurso.stockTotal

                                ContadorCantidad(
                                    nombreRecurso = recurso.nombreItem,
                                    cantidadActual = cantidadPedida,
                                    stockDisponible = stockRealDisponible, // <--- Usamos el stock disponible real
                                    alCambiarCantidad = { nuevaCant ->
                                        viewModel.actualizarCantidadInsumo(recurso.id, nuevaCant)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Paso3ResumenYConfirmacion(
    wizardState: WizardState
) {
    val fechaFormatted = wizardState.fecha.format(
        DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy", Locale("es", "ES"))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Resumen de la Solicitud",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = wizardState.nombreActividad,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📅 Fecha: $fechaFormatted",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⏰ Horario: Bloque B${wizardState.bloqueInicio} al B${wizardState.bloqueFin}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            BannerAnticipacion(resultado = wizardState.resultadoAnticipacion)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recursos Solicitados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val seleccionados = wizardState.cantidadesInsumos.filterValues { it > 0 }

                    if (seleccionados.isEmpty()) {
                        Text(
                            text = "No has seleccionado ningún recurso.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        seleccionados.forEach { (recursoId, cantidad) ->
                            val recurso = wizardState.recursosDisponibles.firstOrNull { it.id == recursoId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = recurso?.nombreItem ?: "Recurso #$recursoId",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$cantidad unid.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (wizardState.adjuntos.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Documentos adjuntos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        wizardState.adjuntos.forEach {
                            Text("• ${it.nombreArchivo}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

    }
}









@Preview(showBackground = true, name = "Wizard Completo - Modo Claro")
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Wizard Completo - Modo Oscuro"
)
@Composable
fun FormularioWizardScreenPreview() {
    DuocTheme {
        Surface {
            FormularioWizardScreen()
        }
    }
}


@Preview(showBackground = true, name = "Paso 1: Fecha y Bloques")
@Composable
fun Paso1Preview() {
    DuocTheme {
        Surface {
            Paso1FechaYBloques(
                wizardState = WizardState(
                    nombreActividad = "Charla de Ciberseguridad",
                    bloqueInicio = 2,
                    bloqueFin = 4
                ),
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            )
        }
    }
}



@Preview(showBackground = true, name = "Paso 2: Recursos e Insumos")
@Composable
fun Paso2Preview() {
    val recursosFalsos = listOf(
        RecursoInventarioEntity(id = 1, departamento = Departamento.DIGITALES, nombreItem = "Proyector HDMI", stockTotal = 4),
        RecursoInventarioEntity(id = 2, departamento = Departamento.DIGITALES, nombreItem = "Micrófono Inalámbrico", stockTotal = 6),
        RecursoInventarioEntity(id = 3, departamento = Departamento.AUXILIARES, nombreItem = "Mesa Plegable", stockTotal = 15)
    )

    DuocTheme {
        Surface {
            Paso2RecursosEInsumos(
                wizardState = WizardState(
                    pasoActual = 2,
                    recursosDisponibles = recursosFalsos,
                    cantidadesInsumos = mapOf(1L to 1, 2L to 2)
                ),
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            )
        }
    }
}


@Preview(showBackground = true, name = "Paso 3: Resumen y Confirmación")
@Composable
fun Paso3Preview() {
    val recursosFalsos = listOf(
        RecursoInventarioEntity(
            id = 1,
            departamento = Departamento.DIGITALES,
            nombreItem = "Proyector HDMI",
            stockTotal = 4
        ),
        RecursoInventarioEntity(id = 2, departamento = Departamento.DIGITALES, nombreItem = "Micrófono Inalámbrico", stockTotal = 6)
    )

    DuocTheme {
        Surface {
            Paso3ResumenYConfirmacion(
                wizardState = WizardState(
                    pasoActual = 3,
                    nombreActividad = "Charla de Ciberseguridad",
                    bloqueInicio = 2,
                    bloqueFin = 4,
                    recursosDisponibles = recursosFalsos,
                    cantidadesInsumos = mapOf(1L to 1, 2L to 2)
                )
            )
        }
    }
}