package com.validador_actividades_duoc.data.remote

import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.domain.model.Departamento

data class RecursoDto(
    val id: Long,
    val departamento: String,
    val nombreItem: String,
    val stockTotal: Int,
    val activo: Boolean = true,
    val stockDisponible: Int? = null
) {
    fun toEntity() = RecursoInventarioEntity(
        id = id,
        departamento = Departamento.valueOf(departamento),
        nombreItem = nombreItem,
        stockTotal = stockTotal,
        activo = activo
    )
}

data class CrearRecursoRequest(val departamento: String, val nombreItem: String, val stockTotal: Int)
data class ActualizarStockRequest(val stockTotal: Int)
data class ItemCompromisoRequest(val recursoId: Long, val cantidad: Int)
data class CompromisoRequest(
    val reservaRef: String,
    val fecha: String,
    val bloqueInicio: Int,
    val bloqueFin: Int,
    val items: List<ItemCompromisoRequest>
)