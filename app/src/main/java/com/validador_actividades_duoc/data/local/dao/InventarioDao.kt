package com.validador_actividades_duoc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.domain.model.Departamento
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizarRecurso(recurso: RecursoInventarioEntity): Long

    @Query("SELECT * FROM recursos_inventario WHERE activo = 1 ORDER BY departamento, nombreItem")
    fun obtenerRecursosDisponibles(): Flow<List<RecursoInventarioEntity>>

    @Query("SELECT * FROM recursos_inventario WHERE activo = 1 AND departamento = :departamento ORDER BY nombreItem")
    fun observarPorDepartamento(departamento: Departamento): Flow<List<RecursoInventarioEntity>>

    @Query("SELECT * FROM recursos_inventario WHERE id = :recursoId")
    suspend fun obtenerRecursoPorId(recursoId: Long): RecursoInventarioEntity?

    @Query("UPDATE recursos_inventario SET stockTotal = :stock WHERE id = :recursoId")
    suspend fun actualizarStock(recursoId: Long, stock: Int)

    // "Retirar" un recurso sin borrar el historial de reservas que lo usaron
    @Query("UPDATE recursos_inventario SET activo = 0 WHERE id = :recursoId")
    suspend fun desactivar(recursoId: Long)

    /**
     * Stock comprometido de un recurso en UN bloque de una fecha.
     * Cuentan las reservas que "tienen" el recurso: en validación, en revisión del admin o confirmadas.
     * (Antes decía 'APROBADA', un estado que no existe.)
     */
    @Query("""
        SELECT COALESCE(SUM(req.cantidadSolicitada), 0)
        FROM requerimientos req
        INNER JOIN reservas res ON req.reservaId = res.id
        WHERE req.recursoId = :recursoId
          AND res.fecha = :fecha
          AND :bloque BETWEEN res.bloqueInicio AND res.bloqueFin
          AND res.estado IN ('CONFIRMADA')
    """)
    suspend fun obtenerCantidadOcupada(recursoId: Long, fecha: String, bloque: Int): Int
}