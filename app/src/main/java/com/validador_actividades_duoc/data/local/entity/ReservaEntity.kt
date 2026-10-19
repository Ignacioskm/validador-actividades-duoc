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

@Entity(
    tableName = "reservas",
    foreignKeys = [ForeignKey(UsuarioEntity::class, ["id"], ["solicitanteId"], onDelete = CASCADE)],
    indices = [Index("solicitanteId"), Index("fecha")]
)
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val solicitanteId: Long,
    val nombreActividad: String,
    val fecha: String,          // "YYYY-MM-DD"
    val bloqueInicio: Int,      // índice 1..13
    val bloqueFin: Int,         // índice 1..13 (rango continuo)
    val estado: EstadoReserva = EstadoReserva.PENDIENTE,
    val fechaCreacion: Long = System.currentTimeMillis()
)