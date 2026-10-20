package com.validador_actividades_duoc.ui.screens.validador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.validador_actividades_duoc.data.local.dao.ReservaDao
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.domain.model.Departamento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ValidadorViewModel(
    private val reservaDao: ReservaDao,
    val departamentoValidador: Departamento
) : ViewModel() {

    private val _reservasArea = MutableStateFlow<List<ReservaConRequerimientos>>(emptyList())
    val reservasArea: StateFlow<List<ReservaConRequerimientos>> = _reservasArea.asStateFlow()

    init {
        cargarSolicitudesPorDepartamento()
    }

    private fun cargarSolicitudesPorDepartamento() {
        viewModelScope.launch {
            reservaDao.obtenerReservasPorDepartamento(departamentoValidador).collect { lista ->
                _reservasArea.value = lista
            }
        }
    }
}