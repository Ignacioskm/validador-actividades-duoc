package com.validador_actividades_duoc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.RequerimientoConRecurso
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizarRecurso(recurso: RecursoInventarioEntity): Long

    @Query("SELECT * FROM recursos_inventario WHERE activo = 1")
    fun obtenerRecursosDisponibles(): Flow<List<RecursoInventarioEntity>>

    @Query("SELECT * FROM recursos_inventario WHERE id = :recursoId")
    suspend fun obtenerRecursoPorId(recursoId: Int): RecursoInventarioEntity?

    /**
     * Calcula la suma total de stock comprometido para un recurso en una fecha
     * y rango de bloques específico para reservas activas o pendientes.
     */
    @Query("""
        SELECT COALESCE(SUM(req.cantidadSolicitada), 0) 
        FROM requerimientos req
        INNER JOIN reservas res ON req.reservaId = res.id
        WHERE req.recursoId = :recursoId 
          AND res.fecha = :fecha 
          AND :bloque BETWEEN res.bloqueInicio AND res.bloqueFin
          AND res.estado IN ('PENDIENTE', 'APROBADA')
    """)
    suspend fun obtenerCantidadOcupada(recursoId: Int, fecha: String, bloque: Int): Int

    @Transaction
    @Query("SELECT * FROM requerimientos WHERE id = :requerimientoId")
    suspend fun obtenerRequerimientoConRecurso(requerimientoId: Int): RequerimientoConRecurso?
}