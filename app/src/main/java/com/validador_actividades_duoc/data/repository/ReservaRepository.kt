package com.validador_actividades_duoc.data.repository

import com.validador_actividades_duoc.data.local.dao.ReservaDao
import com.validador_actividades_duoc.data.local.entity.RequerimientoEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import kotlinx.coroutines.flow.Flow

class ReservaRepository(private val reservaDao: ReservaDao) {

    fun obtenerTodasLasReservas(): Flow<List<ReservaConRequerimientos>> {
        return reservaDao.obtenerTodasLasReservasConRequerimientos()
    }

    fun obtenerReservasPorSolicitante(solicitanteId: Int): Flow<List<ReservaConRequerimientos>> {
        return reservaDao.obtenerReservasPorSolicitante(solicitanteId)
    }

    fun obtenerReservasPorDepartamento(departamento: Departamento): Flow<List<ReservaConRequerimientos>> {
        return reservaDao.obtenerReservasPorDepartamento(departamento)
    }

    suspend fun crearReservaConRequerimientos(reserva: ReservaEntity, requerimientos: List<RequerimientoEntity>): Long {
        val reservaId = reservaDao.insertarReserva(reserva)
        val requerimientosConId = requerimientos.map { it.copy(reservaId = reservaId) }
        reservaDao.insertarRequerimientos(requerimientosConId)
        return reservaId
    }

    suspend fun actualizarReserva(reserva: ReservaEntity) {
        reservaDao.actualizarReserva(reserva)
    }
}