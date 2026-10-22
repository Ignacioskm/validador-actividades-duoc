package com.validador_actividades_duoc.ui.screens.solicitante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.util.BLOQUES
import com.validador_actividades_duoc.domain.util.ResultadoAnticipacion
import com.validador_actividades_duoc.domain.util.evaluarAnticipacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Estado UI para la pantalla principal del Solicitante (Calendario / Mis Solicitudes)
 */
data class SolicitanteUiState(
    val fechaSeleccionada: LocalDate = LocalDate.now(),
    val misSolicitudes: List<ReservaConRequerimientos> = emptyList(),
    val cargando: Boolean = false,
    val mensajeError: String? = null
)

/**
 * Estado dinámico para el Formulario Wizard de Creación de Reserva
 */
data class WizardState(
    val pasoActual: Int = 1,
    val nombreActividad: String = "",
    val fecha: LocalDate = LocalDate.now().plusDays(14),
    val bloqueInicio: Int = 1,
    val bloqueFin: Int = 1,
    val recursosDisponibles: List<RecursoInventarioEntity> = emptyList(),
    val cantidadesInsumos: Map<Long, Int> = emptyMap(),
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val guardadoExitoso: Boolean = false
) {
    /**
     * Propiedad calculada que evalúa en tiempo real las reglas de antelación de la reserva
     *
     * Utiliza la función [evaluarAnticipacion] de la capa de dominio para determinar si la
     * solicitud cumple con la antelación mínima o ideal según la
     * fecha y el bloque horario seleccionados
     *
     */
    val resultadoAnticipacion: ResultadoAnticipacion
        get() {
            val bloqueValido = BLOQUES.getOrNull(bloqueInicio - 1) ?: BLOQUES.first()
            return evaluarAnticipacion(fecha, bloqueValido)
        }

    /**
     * Valida si el Paso 1 está listo para avanzar
     * Corresponde a datos de la actividad y horario
     */
    val esPaso1Valido: Boolean
        get() = nombreActividad.isNotBlank() &&
                bloqueInicio in 1..BLOQUES.size &&
                bloqueFin in bloqueInicio..BLOQUES.size &&
                resultadoAnticipacion != ResultadoAnticipacion.BLOQUEADA

    /**
     * Valida si el Paso 2 tiene al menos un recurso seleccionado
     */
    val esPaso2Valido: Boolean
        get() = cantidadesInsumos.any { it.value > 0 }
}

/**
 * ViewModel encargado del flujo del Solicitante
 * Administra el estado del calendario y coordina la lógica del formulario Wizard
 */
class SolicitanteViewModel(
    private val reservaRepository: ReservaRepository,
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    // Estado 1: Pantalla Principal Calendari
    private val _uiState = MutableStateFlow(SolicitanteUiState())
    val uiState: StateFlow<SolicitanteUiState> = _uiState.asStateFlow()

    // Estado 2: Formulario Wizard
    private val _wizardState = MutableStateFlow(WizardState())
    val wizardState: StateFlow<WizardState> = _wizardState.asStateFlow()

    private var solicitanteIdActual: Long? = null

    init {
        cargarInventarioDisponible()
    }

    /**
     * Carga y observa las solicitudes asociadas al solicitante con sesión activa
     */
    fun inicializarSolicitante(solicitanteId: Long) {
        if (solicitanteIdActual == solicitanteId) return
        solicitanteIdActual = solicitanteId

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            reservaRepository.delSolicitante(solicitanteId)
                .catch { error ->
                    _uiState.update { it.copy(cargando = false, mensajeError = error.message) }
                }
                .collect { lista ->
                    _uiState.update {
                        it.copy(
                            misSolicitudes = lista,
                            cargando = false,
                            mensajeError = null
                        )
                    }
                }
        }
    }

    /**
     * Carga el catálogo de recursos disponibles para el Paso 2 del Wizard
     */
    private fun cargarInventarioDisponible() {
        viewModelScope.launch {
            inventarioRepository.recursosDisponibles
                .catch { error ->
                    _wizardState.update { it.copy(mensajeError = error.message) }
                }
                .collect { recursos ->
                    _wizardState.update { it.copy(recursosDisponibles = recursos) }
                }
        }
    }

    // ACCIONES DEL CALENDARIO

    fun seleccionarFechaCalendario(fecha: LocalDate) {
        _uiState.update { it.copy(fechaSeleccionada = fecha) }
    }

    // ACCIONES DEL WIZARD (PASO 1)

    fun actualizarNombreActividad(nombre: String) {
        _wizardState.update { it.copy(nombreActividad = nombre) }
    }

    fun actualizarFechaReserva(fecha: LocalDate) {
        _wizardState.update { it.copy(fecha = fecha) }
    }

    fun seleccionarRangoBloques(inicio: Int, fin: Int) {
        _wizardState.update {
            it.copy(
                bloqueInicio = inicio,
                bloqueFin = maxOf(inicio, fin)
            )
        }
    }

    // ACCIONES DEL WIZARD (PASO 2)

    fun actualizarCantidadInsumo(recursoId: Long, cantidad: Int) {
        _wizardState.update { estado ->
            val mapaActualizado = estado.cantidadesInsumos.toMutableMap()
            if (cantidad > 0) {
                mapaActualizado[recursoId] = cantidad
            } else {
                mapaActualizado.remove(recursoId)
            }
            estado.copy(cantidadesInsumos = mapaActualizado)
        }
    }

    // NAVEGACIÓN Y ENVÍO DEL WIZARD
    fun siguientePasoWizard() {
        _wizardState.update { estado ->
            if (estado.pasoActual < 3) {
                estado.copy(pasoActual = estado.pasoActual + 1)
            } else estado
        }
    }

    fun pasoAnteriorWizard() {
        _wizardState.update { estado ->
            if (estado.pasoActual > 1) {
                estado.copy(pasoActual = estado.pasoActual - 1)
            } else estado
        }
    }

    /**
     * Envía la solicitud al [ReservaRepository] validando que exista un usuario activo
     */
    fun confirmarReserva() {
        val userId = solicitanteIdActual
        if (userId == null) {
            _wizardState.update { it.copy(mensajeError = "No hay una sesión de usuario activa") }
            return
        }

        val estado = _wizardState.value
        val itemsFiltrados = estado.cantidadesInsumos.filterValues { it > 0 }

        if (itemsFiltrados.isEmpty()) {
            _wizardState.update { it.copy(mensajeError = "Debes seleccionar al menos un recurso") }
            return
        }

        viewModelScope.launch {
            _wizardState.update { it.copy(cargando = true, mensajeError = null) }

            val nuevaReserva = ReservaEntity(
                solicitanteId = userId,
                nombreActividad = estado.nombreActividad.trim(),
                fecha = estado.fecha.toString(),
                bloqueInicio = estado.bloqueInicio,
                bloqueFin = estado.bloqueFin
            )

            val resultado = reservaRepository.crearReserva(nuevaReserva, itemsFiltrados)

            resultado.onSuccess {
                _wizardState.update {
                    it.copy(
                        cargando = false,
                        guardadoExitoso = true,
                        mensajeError = null
                    )
                }
            }.onFailure { error ->
                _wizardState.update {
                    it.copy(
                        cargando = false,
                        mensajeError = error.message ?: "Error al guardar la solicitud"
                    )
                }
            }
        }
    }

    /**
     * Reinicia el estado del formulario para crear una nueva reserva limpia
     */
    fun reiniciarWizard() {
        _wizardState.value = WizardState(
            recursosDisponibles = _wizardState.value.recursosDisponibles
        )
    }

    fun limpiarMensajeError() {
        _uiState.update { it.copy(mensajeError = null) }
        _wizardState.update { it.copy(mensajeError = null) }
    }
}

/**
 * Factory para instanciar SolicitanteViewModel inyectando sus dependencias mediante AppContainer
 */
class SolicitanteViewModelFactory(
    private val reservaRepository: ReservaRepository,
    private val inventarioRepository: InventarioRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolicitanteViewModel::class.java)) {
            return SolicitanteViewModel(reservaRepository, inventarioRepository) as T
        }
        throw IllegalArgumentException("ViewModel Class Desconocida: ${modelClass.name}")
    }
}