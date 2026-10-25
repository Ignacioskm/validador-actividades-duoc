package com.validador_actividades_duoc.data.local

import androidx.room.TypeConverter
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.domain.model.TipoAdjunto

class Converters {

    @TypeConverter
    fun fromRolUsuario(value: RolUsuario?): String? = value?.name

    @TypeConverter
    fun toRolUsuario(value: String?): RolUsuario? = value?.let { enumValueOf<RolUsuario>(it) }

    @TypeConverter
    fun fromDepartamento(value: Departamento?): String? = value?.name

    @TypeConverter
    fun toDepartamento(value: String?): Departamento? = value?.let { enumValueOf<Departamento>(it) }

    @TypeConverter
    fun fromEstadoReserva(value: EstadoReserva?): String? = value?.name

    @TypeConverter
    fun toEstadoReserva(value: String?): EstadoReserva? = value?.let { enumValueOf<EstadoReserva>(it) }

    @TypeConverter
    fun fromEstadoRequerimiento(value: EstadoRequerimiento?): String? = value?.name

    @TypeConverter
    fun toEstadoRequerimiento(value: String?): EstadoRequerimiento? = value?.let { enumValueOf<EstadoRequerimiento>(it) }

    @TypeConverter
    fun fromTipoAdjunto(value: TipoAdjunto?): String? = value?.name

    @TypeConverter
    fun toTipoAdjunto(value: String?): TipoAdjunto? = value?.let { enumValueOf<TipoAdjunto>(it) }
}