package com.validador_actividades_duoc.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.RolUsuario

@Entity(
    tableName = "usuarios",
    indices = [
        Index("correo", unique = true),
        Index("firebaseUid", unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val correo: String,
    val nombre: String,
    val rol: RolUsuario,
    val departamento: Departamento? = null,
    val firebaseUid: String? = null
)
