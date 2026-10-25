package com.validador_actividades_duoc.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ReservaConRequerimientos(
    @Embedded val reserva: ReservaEntity,
    @Relation(entity = RequerimientoEntity::class, parentColumn = "id", entityColumn = "reservaId")
    val requerimientos: List<RequerimientoConRecurso>,
    @Relation(parentColumn = "solicitanteId", entityColumn = "id")
    val solicitante: UsuarioEntity,
    @Relation(parentColumn = "id", entityColumn = "reservaId")
    val adjuntos: List<AdjuntoEntity>
)