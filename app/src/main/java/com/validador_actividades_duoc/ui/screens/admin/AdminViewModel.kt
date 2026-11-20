package com.validador_actividades_duoc.ui.screens.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.Departamento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val reservaRepository: ReservaRepository,
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    // Todas las solis que ya pasaron los filtros de los otros departamentos
    val solicitudesPendientesAdmin: StateFlow<List<ReservaConRequerimientos>> =
        reservaRepository.pendientesDeAdmin()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    //Buscador y lista filtrada de eventos confirmados
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val eventosConfirmados: StateFlow<List<ReservaConRequerimientos>> =
        combine(reservaRepository.confirmadas(), _busqueda) { lista, query ->
            if (query.isBlank()) {
                lista
            } else {
                lista.filter {
                    it.reserva.nombreActividad.contains(query, ignoreCase = true) ||
                            it.solicitante.nombre.contains(query, ignoreCase = true) ||
                            it.solicitante.correo.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // El inventario pulento, aca salen todos los recursos disponibles
    val inventarioGlobal: StateFlow<List<RecursoInventarioEntity>> =
        inventarioRepository.recursosDisponibles
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Estado para notificaciones visuales
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun onBusquedaChanged(nuevaBusqueda: String) {
        _busqueda.value = nuevaBusqueda
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    // Acciones del Admin
    fun confirmarEvento(reservaId: Long) {
        viewModelScope.launch {
            reservaRepository.confirmarPorAdmin(reservaId)
                .onSuccess { _mensaje.value = "Actividad confirmada oficialmente" }
                .onFailure { _mensaje.value = it.message ?: "Error al confirmar" }
        }
    }

    fun rechazarEvento(reservaId: Long, motivo: String) {
        viewModelScope.launch {
            reservaRepository.rechazarPorAdmin(reservaId, motivo)
                .onSuccess { _mensaje.value = "Actividad rechazada por el Administrador" }
                .onFailure { _mensaje.value = it.message ?: "Error al rechazar" }
        }
    }

    fun editarStockGlobal(recursoId: Long, nuevoStock: Int) {
        viewModelScope.launch {
            inventarioRepository.editarStock(recursoId, nuevoStock)
                .onSuccess { _mensaje.value = "Stock actualizado correctamente" }
                .onFailure { _mensaje.value = it.message ?: "Error al actualizar stock" }
        }
    }

    fun crearRecursoGlobal(departamento: Departamento, nombre: String, stock: Int) {
        viewModelScope.launch {
            inventarioRepository.crearRecurso(departamento, nombre, stock)
                .onSuccess { _mensaje.value = "Recurso añadido a ${departamento.etiqueta}" }
                .onFailure { _mensaje.value = it.message ?: "Error al crear recurso" }
        }
    }

    fun confirmarEventoExcepcional(reservaId: Long) {
        viewModelScope.launch {
            reservaRepository.confirmarPorAdmin(
                reservaId = reservaId,
                excepcional = true
            )
                .onSuccess {
                    _mensaje.value =
                        "Actividad confirmada excepcionalmente"
                }
                .onFailure {
                    _mensaje.value =
                        it.message ?: "Error al confirmar"
                }
        }
    }
}

class AdminViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = context.applicationContext
        @Suppress("UNCHECKED_CAST")
        return AdminViewModel(
            AppContainer.provideReservaRepository(app),
            AppContainer.provideInventarioRepository(app)
        ) as T
    }
}