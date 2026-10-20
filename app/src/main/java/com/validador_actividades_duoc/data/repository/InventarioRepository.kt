package com.validador_actividades_duoc.data.repository

import com.validador_actividades_duoc.data.local.dao.InventarioDao
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import kotlinx.coroutines.flow.Flow

class InventarioRepository(private val inventarioDao: InventarioDao) {

    val recursosDisponibles: Flow<List<RecursoInventarioEntity>> = inventarioDao.obtenerRecursosDisponibles()

    suspend fun insertarOActualizar(recurso: RecursoInventarioEntity): Long {
        return inventarioDao.insertarOActualizarRecurso(recurso)
    }

    suspend fun obtenerPorId(id: Int): RecursoInventarioEntity? {
        return inventarioDao.obtenerRecursoPorId(id)
    }

    suspend fun calcularStockDisponible(recursoId: Int, fecha: String, bloqueInicio: Int, bloqueFin: Int, stockTotal: Int): Int {
        var maxOcupado = 0
        for (bloque in bloqueInicio..bloqueFin) {
            val ocupadoEnBloque = inventarioDao.obtenerCantidadOcupada(recursoId, fecha, bloque)
            if (ocupadoEnBloque > maxOcupado) {
                maxOcupado = ocupadoEnBloque
            }
        }
        return (stockTotal - maxOcupado).coerceAtLeast(0)
    }
}