package com.validador_actividades_duoc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.RESTRICT
import androidx.room.Index

import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento

@Entity(tableName = "recursos_inventario", indices = [Index("departamento")])
data class RecursoInventarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val departamento: Departamento,
    val nombreItem: String,
    val stockTotal: Int,
    val activo: Boolean = true     // para "retirar" un recurso sin borrar su historial
)