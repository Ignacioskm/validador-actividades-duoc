package com.validador_actividades_duoc.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation


data class RequerimientoConRecurso(
    @Embedded val requerimiento: RequerimientoEntity,
    @Relation(parentColumn = "recursoId", entityColumn = "id") val recurso: RecursoInventarioEntity
)