package com.validador_actividades_duoc.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.validador_actividades_duoc.domain.model.TipoAdjunto

@Entity(
    tableName = "adjuntos",
    foreignKeys = [ForeignKey(ReservaEntity::class, ["id"], ["reservaId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("reservaId")]
)
data class AdjuntoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reservaId: Long,
    val tipo: TipoAdjunto,
    val nombreArchivo: String,
    val rutaLocal: String,
    val mimeType: String,
    val tamanoBytes: Long
)