package com.validador_actividades_duoc.data.repository

import androidx.room.withTransaction
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.local.entity.RequerimientoEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento
import com.validador_actividades_duoc.domain.util.BLOQUES
import com.validador_actividades_duoc.domain.util.ResultadoAnticipacion
import com.validador_actividades_duoc.domain.util.evaluarAnticipacion
import com.validador_actividades_duoc.data.local.entity.AdjuntoEntity
import com.validador_actividades_duoc.domain.model.AdjuntoBorrador
import com.validador_actividades_duoc.domain.model.MAX_ADJUNTOS
import com.validador_actividades_duoc.domain.model.MAX_BYTES_ADJUNTO
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ReservaRepository(private val db: AppDatabase) {

    private val reservaDao = db.reservaDao()
    private val inventarioDao = db.inventarioDao()

    // Estas son las lecturas! leer desps la guía
    fun todas(): Flow<List<ReservaConRequerimientos>> = reservaDao.observarTodas()
    fun delSolicitante(id: Long) = reservaDao.observarPorSolicitante(id)
    fun deFecha(fecha: String) = reservaDao.observarDeFecha(fecha)
    fun solicitudesDeArea(depto: Departamento) = reservaDao.observarPendientesDeArea(depto)
    // fun pendientesDeAdmin() = reservaDao.observarPorEstado(EstadoReserva.PENDIENTE_ADMIN)

    // ReservaRepository.kt

    // Obtener todas las solicitudes pendientes de resolución final por el Admin (incluyendo las recién creadas)
    fun pendientesDeAdmin(): Flow<List<ReservaConRequerimientos>> =
        reservaDao.observarPorEstado(EstadoReserva.PENDIENTE, EstadoReserva.PENDIENTE_ADMIN)
    // O si prefieres que vea absolutamente todas las que no están confirmadas ni rechazadas:
    // reservaDao.observarTodas()
    fun confirmadas() = reservaDao.observarPorEstado(EstadoReserva.CONFIRMADA)

    // 1. El solicitante hace la reserva de la actividad.
    /**
     * @param items recursoId -> cantidad pedida. Las áreas involucradas salen de aquí.
     * Revalida anticipación y stock dentro de una transacción (no se confía en la UI).
     */

    suspend fun crearReserva(
        reserva: ReservaEntity,
        items: Map<Long, Int>,
        adjuntos: List<AdjuntoBorrador> = emptyList()
    ): Result<Long> = runCatching {
        require(items.isNotEmpty()) { "Selecciona al menos un recurso" }
        require(reserva.nombreActividad.isNotBlank()) { "Falta el nombre de la actividad" }
        require(reserva.bloqueInicio in 1..BLOQUES.size && reserva.bloqueFin in reserva.bloqueInicio..BLOQUES.size) {
            "Rango de bloques inválido"
        }
        require(adjuntos.size <= MAX_ADJUNTOS) { "Máximo $MAX_ADJUNTOS archivos adjuntos" }
        require(adjuntos.all { it.tamanoBytes <= MAX_BYTES_ADJUNTO }) { "Cada archivo debe pesar menos de 5 MB" }

        val anticipacion = evaluarAnticipacion(
            LocalDate.parse(reserva.fecha), BLOQUES[reserva.bloqueInicio - 1]
        )
        check(anticipacion != ResultadoAnticipacion.BLOQUEADA) {
            "Debes solicitar con al menos 48 horas de anticipación"
        }

        db.withTransaction {
            val requerimientos = items.map { (recursoId, cantidad) ->
                val recurso = inventarioDao.obtenerRecursoPorId(recursoId)
                    ?: error("El recurso $recursoId no existe")

                var maxOcupado = 0
                for (bloque in reserva.bloqueInicio..reserva.bloqueFin) {
                    maxOcupado = maxOf(
                        maxOcupado,
                        inventarioDao.obtenerCantidadOcupada(recursoId, reserva.fecha, bloque)
                    )
                }
                val disponible = (recurso.stockTotal - maxOcupado).coerceAtLeast(0)
                check(cantidad in 1..disponible) {
                    "Stock insuficiente de '${recurso.nombreItem}' (disponible: $disponible)"
                }
                RequerimientoEntity(reservaId = 0, recursoId = recursoId, cantidadSolicitada = cantidad)
            }

            val reservaId = reservaDao.insertarReserva(reserva.copy(estado = EstadoReserva.PENDIENTE))
            reservaDao.insertarRequerimientos(requerimientos.map { it.copy(reservaId = reservaId) })
            if (adjuntos.isNotEmpty()) {
                reservaDao.insertarAdjuntos(adjuntos.map {
                    AdjuntoEntity(
                        reservaId = reservaId,
                        tipo = it.tipo,
                        nombreArchivo = it.nombreArchivo,
                        rutaLocal = it.rutaLocal,
                        mimeType = it.mimeType,
                        tamanoBytes = it.tamanoBytes
                    )
                })
            }
            reservaId
        }
    }

    // 2. Un departamento aprueba o rechaza una actividad
    suspend fun resolverArea(
        reservaId: Long,
        departamento: Departamento,
        aprobar: Boolean,
        motivo: String?,
        validadorId: Long
    ): Result<Unit> = runCatching {
        if (!aprobar) require(!motivo.isNullOrBlank()) { "Indica el motivo del rechazo" }

        db.withTransaction {
            val reserva = reservaDao.obtenerPorId(reservaId)?.reserva ?: error("La solicitud no existe")
            check(reserva.estado == EstadoReserva.PENDIENTE) { "Esta solicitud ya no está en validación" }

            reservaDao.resolverRequerimientosDeArea(
                reservaId = reservaId,
                departamento = departamento,
                estado = if (aprobar) EstadoRequerimiento.APROBADO else EstadoRequerimiento.RECHAZADO,
                motivo = motivo?.trim(),
                validadorId = validadorId,
                fecha = System.currentTimeMillis()
            )
            recalcularEstado(reservaId)
        }
    }

    /** Cualquier rechazo => RECHAZADA. Todos aprobados => PENDIENTE_ADMIN. Si no, sigue PENDIENTE. */
    // ReservaRepository.kt

    /**
     * Cuando todas las áreas involucradas han emitido su decisión (Aprobado o Rechazado),
     * la solicitud pasa a PENDIENTE_ADMIN para que el Administrador revise el estado
     * de cada departamento y tome la decisión final.
     */
    private suspend fun recalcularEstado(reservaId: Long) {
        val reqs = reservaDao.requerimientosDeReserva(reservaId)

        // Verificamos si todos los requerimientos ya fueron procesados por sus áreas
        val todasLasAreasRespondieron = reqs.none { it.estadoValidacion == EstadoRequerimiento.PENDIENTE }

        val nuevoEstado = when {
            // Si todas las áreas respondieron (así hayan aprobado o rechazado algunas),
            // pasa al Admin para la resolución final y envío de mensaje
            todasLasAreasRespondieron -> EstadoReserva.PENDIENTE_ADMIN

            // Si aún quedan departamentos por validar, sigue PENDIENTE
            else -> EstadoReserva.PENDIENTE
        }

        val motivo = reqs.firstOrNull { it.estadoValidacion == EstadoRequerimiento.RECHAZADO }?.motivoRechazo
        reservaDao.actualizarEstado(reservaId, nuevoEstado, motivo)
    }

    // 3. el admin valida definitivamente la actividad
    suspend fun confirmarPorAdmin(reservaId: Long,excepcional: Boolean = false): Result<Unit> = runCatching {
        db.withTransaction {
            val reservaConReqs = reservaDao.obtenerPorId(reservaId)
                ?: error("La solicitud no existe")

            val reserva = reservaConReqs.reserva

            val puedeConfirmar = if (excepcional) {
                reserva.estado == EstadoReserva.PENDIENTE ||
                        reserva.estado == EstadoReserva.PENDIENTE_ADMIN
            } else {
                reserva.estado == EstadoReserva.PENDIENTE_ADMIN
            }

            check(puedeConfirmar) {
                "Esta solicitud no está lista para confirmación final"
            }

            // Revalidar stock contra reservas ya CONFIRMADAS
            for (reqConRecurso in reservaConReqs.requerimientos) {
                val req = reqConRecurso.requerimiento
                val recurso = reqConRecurso.recurso

                if (req.estadoValidacion == EstadoRequerimiento.APROBADO) {
                    var maxOcupado = 0
                    for (bloque in reserva.bloqueInicio..reserva.bloqueFin) {
                        val ocupado = inventarioDao.obtenerCantidadOcupada(recurso.id, reserva.fecha, bloque)
                        if (ocupado > maxOcupado) maxOcupado = ocupado
                    }

                    val disponible = recurso.stockTotal - maxOcupado
                    check(req.cantidadSolicitada <= disponible) {
                        "No se puede confirmar. Stock insuficiente de '${recurso.nombreItem}'. " +
                                "Quedan $disponible unid. disponibles y se requieren ${req.cantidadSolicitada}."
                    }
                }
            }

            reservaDao.actualizarEstado(reservaId, EstadoReserva.CONFIRMADA, null)
        }
    }

    suspend fun rechazarPorAdmin(reservaId: Long, motivo: String): Result<Unit> = runCatching {
        require(motivo.isNotBlank()) { "Indica el motivo del rechazo" }
        db.withTransaction {
            val reserva = reservaDao.obtenerPorId(reservaId)?.reserva
                ?: error("La solicitud no existe")

            check(reserva.estado == EstadoReserva.PENDIENTE_ADMIN || (reserva.estado == EstadoReserva.PENDIENTE)) {
                "Esta solicitud no está en revisión final"
            }

            // Cambia el estado definitivamente a RECHAZADA y guarda el motivo del Admin
            reservaDao.actualizarEstado(reservaId, EstadoReserva.RECHAZADA, motivo.trim())
        }
    }
}