package com.validador_actividades_duoc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.validador_actividades_duoc.data.local.entity.AdjuntoEntity
import com.validador_actividades_duoc.data.local.entity.RequerimientoEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarReserva(reserva: ReservaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarRequerimientos(requerimientos: List<RequerimientoEntity>)

    @Query("SELECT * FROM requerimientos WHERE reservaId = :reservaId")
    suspend fun requerimientosDeReserva(reservaId: Long): List<RequerimientoEntity>


    @Query("UPDATE reservas SET estado = :estado, motivoRechazo = :motivo WHERE id = :reservaId")
    suspend fun actualizarEstado(reservaId: Long, estado: EstadoReserva, motivo: String?)

    @Transaction
    @Query("SELECT * FROM reservas WHERE id = :reservaId")
    suspend fun obtenerPorId(reservaId: Long): ReservaConRequerimientos?

    @Transaction
    @Query("SELECT * FROM reservas ORDER BY fechaCreacion DESC")
    fun obtenerTodasLasReservasConRequerimientos(): Flow<List<ReservaConRequerimientos>>

    @Transaction
    @Query("SELECT * FROM reservas WHERE solicitanteId = :solicitanteId ORDER BY fechaCreacion DESC")
    fun obtenerReservasPorSolicitante(solicitanteId: Int): Flow<List<ReservaConRequerimientos>>

    @Transaction
    @Query("SELECT * FROM reservas WHERE estado = :estado ORDER BY fechaCreacion DESC")
    fun obtenerReservasPorEstado(estado: EstadoReserva): Flow<List<ReservaConRequerimientos>>

    @Query("""
        UPDATE requerimientos
        SET estadoValidacion = :estado, motivoRechazo = :motivo,
            validadorId = :validadorId, fechaResolucion = :fecha
        WHERE reservaId = :reservaId
          AND estadoValidacion = 'PENDIENTE'
          AND recursoId IN (SELECT id FROM recursos_inventario WHERE departamento = :departamento)
    """)
    suspend fun resolverRequerimientosDeArea(
        reservaId: Long,
        departamento: Departamento,
        estado: EstadoRequerimiento,
        motivo: String?,
        validadorId: Long,
        fecha: Long
    )

    @Transaction
    @Query("SELECT * FROM reservas ORDER BY fechaCreacion DESC")
    fun observarTodas(): Flow<List<ReservaConRequerimientos>>

    @Transaction
    @Query("SELECT * FROM reservas WHERE solicitanteId = :solicitanteId ORDER BY fechaCreacion DESC")
    fun observarPorSolicitante(solicitanteId: Long): Flow<List<ReservaConRequerimientos>>

    @Transaction
    @Query("SELECT * FROM reservas WHERE estado = :estado ORDER BY fechaCreacion ASC")
    fun observarPorEstado(estado: EstadoReserva): Flow<List<ReservaConRequerimientos>>

    @Query("SELECT * FROM reservas WHERE fecha = :fecha AND estado != 'RECHAZADA'")
    fun observarDeFecha(fecha: String): Flow<List<ReservaEntity>>

    /**
     * Solicitudes que le tocan a un ÁREA: reservas aún en validación que tienen
     * al menos un requerimiento PENDIENTE de un recurso de ese departamento.
     * (Antes filtraba por el departamento del solicitante: incorrecto.)
     */
    @Transaction
    @Query("""
        SELECT DISTINCT r.*
        FROM reservas r
        INNER JOIN requerimientos req ON req.reservaId = r.id
        INNER JOIN recursos_inventario rec ON rec.id = req.recursoId
        WHERE rec.departamento = :departamento
          AND req.estadoValidacion = 'PENDIENTE'
          AND r.estado = 'PENDIENTE'
        ORDER BY r.fechaCreacion ASC
    """)
    fun observarPendientesDeArea(departamento: Departamento): Flow<List<ReservaConRequerimientos>>
    @Transaction
    @Query("SELECT * FROM reservas WHERE estado IN (:estado, :pendienteAdmin) ORDER BY fechaCreacion DESC")
    fun observarPorEstado(
        estado: EstadoReserva,
        pendienteAdmin: EstadoReserva
    ): Flow<List<ReservaConRequerimientos>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarAdjuntos(adjuntos: List<AdjuntoEntity>)
}