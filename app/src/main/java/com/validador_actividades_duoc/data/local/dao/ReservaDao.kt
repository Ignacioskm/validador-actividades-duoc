package com.validador_actividades_duoc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.validador_actividades_duoc.data.local.entity.RequerimientoEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarReserva(reserva: ReservaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarRequerimientos(requerimientos: List<RequerimientoEntity>)

    @Update
    suspend fun actualizarReserva(reserva: ReservaEntity)

    @Transaction
    @Query("SELECT * FROM reservas WHERE id = :reservaId")
    suspend fun obtenerReservaPorId(reservaId: Int): ReservaConRequerimientos?

    @Transaction
    @Query("SELECT * FROM reservas ORDER BY fechaCreacion DESC")
    fun obtenerTodasLasReservasConRequerimientos(): Flow<List<ReservaConRequerimientos>>

    @Transaction
    @Query("SELECT * FROM reservas WHERE solicitanteId = :solicitanteId ORDER BY fechaCreacion DESC")
    fun obtenerReservasPorSolicitante(solicitanteId: Int): Flow<List<ReservaConRequerimientos>>

    @Transaction
    @Query("SELECT * FROM reservas WHERE estado = :estado ORDER BY fechaCreacion DESC")
    fun obtenerReservasPorEstado(estado: EstadoReserva): Flow<List<ReservaConRequerimientos>>

    @Transaction
    @Query("""
    SELECT reservas.* 
    FROM reservas 
    INNER JOIN usuarios ON reservas.solicitanteId = usuarios.id 
    WHERE usuarios.departamento = :departamento 
    ORDER BY reservas.fechaCreacion DESC
""")
    fun obtenerReservasPorDepartamento(departamento: Departamento): Flow<List<ReservaConRequerimientos>>
}