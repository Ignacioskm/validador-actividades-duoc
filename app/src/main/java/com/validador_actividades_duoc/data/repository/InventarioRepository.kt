package com.validador_actividades_duoc.data.repository

import com.validador_actividades_duoc.data.local.dao.InventarioDao
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.domain.model.Departamento
import kotlinx.coroutines.flow.Flow

class InventarioRepository(private val inventarioDao: InventarioDao) {

    val recursosDisponibles: Flow<List<RecursoInventarioEntity>> =
        inventarioDao.obtenerRecursosDisponibles()

    fun recursosDeArea(departamento: Departamento): Flow<List<RecursoInventarioEntity>> =
        inventarioDao.observarPorDepartamento(departamento)

    suspend fun crearRecurso(departamento: Departamento, nombre: String, stock: Int): Result<Long> =
        runCatching {
            require(nombre.isNotBlank()) { "El nombre no puede estar vacío" }
            require(stock >= 0) { "El stock no puede ser negativo" }
            inventarioDao.insertarOActualizarRecurso(
                RecursoInventarioEntity(
                    departamento = departamento,
                    nombreItem = nombre.trim(),
                    stockTotal = stock
                )
            )
        }

    suspend fun editarStock(recursoId: Long, nuevoStock: Int): Result<Unit> = runCatching {
        require(nuevoStock >= 0) { "El stock no puede ser negativo" }
        inventarioDao.actualizarStock(recursoId, nuevoStock)
    }

    suspend fun retirarRecurso(recursoId: Long): Result<Unit> = runCatching {
        inventarioDao.desactivar(recursoId)
    }

    suspend fun calcularStockDisponible(
        recursoId: Long,
        fecha: String,
        bloqueInicio: Int,
        bloqueFin: Int,
        stockTotal: Int
    ): Int {
        var maxOcupado = 0
        for (bloque in bloqueInicio..bloqueFin) {
            val ocupado = inventarioDao.obtenerCantidadOcupada(recursoId, fecha, bloque)
            if (ocupado > maxOcupado) maxOcupado = ocupado
        }
        return (stockTotal - maxOcupado).coerceAtLeast(0)
    }
}