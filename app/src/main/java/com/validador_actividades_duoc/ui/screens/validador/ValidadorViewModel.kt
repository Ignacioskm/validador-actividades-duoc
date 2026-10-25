package com.validador_actividades_duoc.ui.screens.validador

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.RequerimientoConRecurso
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.Departamento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.validador_actividades_duoc.data.local.entity.AdjuntoEntity

/** Lo que ve un área de una reserva: solo SUS ítems. */
data class SolicitudArea(
    val reserva: ReservaEntity,
    val solicitante: UsuarioEntity,
    val items: List<RequerimientoConRecurso>,
    val adjuntos: List<AdjuntoEntity>
)

class ValidadorViewModel(
    private val reservaRepository: ReservaRepository,
    private val inventarioRepository: InventarioRepository,
    val validador: UsuarioEntity
) : ViewModel() {

    val departamento: Departamento = requireNotNull(validador.departamento) {
        "El validador debe tener un departamento asignado"
    }

    val solicitudes: StateFlow<List<SolicitudArea>> =
        reservaRepository.solicitudesDeArea(departamento)
            .map { reservas ->
                reservas.map { r ->
                    SolicitudArea(
                        reserva = r.reserva,
                        solicitante = r.solicitante,
                        items = r.requerimientos.filter { it.recurso.departamento == departamento },
                        adjuntos = r.adjuntos
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val inventario: StateFlow<List<RecursoInventarioEntity>> =
        inventarioRepository.recursosDeArea(departamento)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun mensajeMostrado() { _mensaje.value = null }

    fun aprobar(reservaId: Long) = resolver(reservaId, aprobar = true, motivo = null)

    fun rechazar(reservaId: Long, motivo: String) = resolver(reservaId, aprobar = false, motivo = motivo)

    private fun resolver(reservaId: Long, aprobar: Boolean, motivo: String?) {
        viewModelScope.launch {
            reservaRepository.resolverArea(reservaId, departamento, aprobar, motivo, validador.id)
                .onSuccess { _mensaje.value = if (aprobar) "Solicitud aprobada" else "Solicitud rechazada" }
                .onFailure { _mensaje.value = it.message ?: "No se pudo completar la acción" }
        }
    }

    fun crearRecurso(nombre: String, stock: Int) {
        viewModelScope.launch {
            inventarioRepository.crearRecurso(departamento, nombre, stock)
                .onSuccess { _mensaje.value = "Recurso creado" }
                .onFailure { _mensaje.value = it.message }
        }
    }

    fun editarStock(recursoId: Long, nuevoStock: Int) {
        viewModelScope.launch {
            inventarioRepository.editarStock(recursoId, nuevoStock)
                .onSuccess { _mensaje.value = "Stock actualizado" }
                .onFailure { _mensaje.value = it.message }
        }
    }

    fun retirarRecurso(recursoId: Long) {
        viewModelScope.launch {
            inventarioRepository.retirarRecurso(recursoId)
                .onSuccess { _mensaje.value = "Recurso retirado" }
                .onFailure { _mensaje.value = it.message }
        }
    }
}

class ValidadorViewModelFactory(
    private val context: Context,
    private val validador: UsuarioEntity
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = context.applicationContext
        @Suppress("UNCHECKED_CAST")
        return ValidadorViewModel(
            AppContainer.provideReservaRepository(app),
            AppContainer.provideInventarioRepository(app),
            validador
        ) as T
    }
}