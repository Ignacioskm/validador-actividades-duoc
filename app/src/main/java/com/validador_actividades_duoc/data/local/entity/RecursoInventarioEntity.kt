package com.validador_actividades_duoc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

import com.validador_actividades_duoc.domain.model.Departamento

@Entity(tableName = "recursos_inventario", indices = [Index("departamento")])
data class RecursoInventarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val departamento: Departamento,
    val nombreItem: String,
    val stockTotal: Int,
    val activo: Boolean = true     // para "retirar" un recurso sin borrar su historial
)