package com.validador_actividades_duoc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE id = :usuarioId")
    suspend fun obtenerUsuarioPorId(usuarioId: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :email LIMIT 1")
    suspend fun obtenerUsuarioPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios")
    suspend fun obtenerTodosLosUsuarios(): List<UsuarioEntity>
}