package com.validador_actividades_duoc.data.local.entity

// comentario 3 de ejemplo ASDSADAASDds

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
    tableName = "requerimientos",
    foreignKeys = [
        ForeignKey(ReservaEntity::class, ["id"], ["reservaId"], onDelete = CASCADE),
        ForeignKey(RecursoInventarioEntity::class, ["id"], ["recursoId"], onDelete = RESTRICT)
    ],
    indices = [Index("reservaId"), Index("recursoId")]
)
data class RequerimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reservaId: Long,
    val recursoId: Long,
    val cantidadSolicitada: Int,
    val estadoValidacion: EstadoRequerimiento = EstadoRequerimiento.PENDIENTE,
    val motivoRechazo: String? = null,     // NUEVO: el prototipo no lo tiene y hace falta
    val validadorId: Long? = null,
    val fechaResolucion: Long? = null
)