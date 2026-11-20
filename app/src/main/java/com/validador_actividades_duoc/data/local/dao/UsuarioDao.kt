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
    suspend fun obtenerUsuarioPorId(usuarioId: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE correo = :email LIMIT 1")
    suspend fun obtenerUsuarioPorEmail(email: String): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    //Consulta prnicipal para identificar usuarios autenticados.
    @Query("SELECT * FROM usuarios WHERE firebaseUid = :uid LIMIT 1")
    suspend fun obtenerUsuarioPorUid(uid: String): UsuarioEntity?

    @Query("""
    UPDATE usuarios
    SET correo = :correo, nombre = :nombre
    WHERE firebaseUid = :uid
    """)
    suspend fun actualizarPerfilGoogle(
        uid: String,
        correo: String,
        nombre: String
    )

}