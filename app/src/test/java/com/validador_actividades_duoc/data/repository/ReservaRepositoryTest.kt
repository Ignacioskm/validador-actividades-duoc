package com.validador_actividades_duoc.data.repository

import androidx.room.withTransaction
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.local.dao.InventarioDao
import com.validador_actividades_duoc.data.local.dao.ReservaDao
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.RequerimientoConRecurso
import com.validador_actividades_duoc.data.local.entity.RequerimientoEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.domain.model.AdjuntoBorrador
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.MAX_ADJUNTOS
import com.validador_actividades_duoc.domain.model.MAX_BYTES_ADJUNTO
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import java.time.LocalDate

class ReservaRepositoryTest : StringSpec({

    lateinit var db: AppDatabase
    lateinit var reservaDao: ReservaDao
    lateinit var inventarioDao: InventarioDao
    lateinit var repository: ReservaRepository

    val fechaFuturaValida = LocalDate.now().plusDays(20).toString()
    val deptoEjemplo = Departamento.entries.first()

    beforeTest {
        reservaDao = mockk(relaxed = true)
        inventarioDao = mockk(relaxed = true)
        db = mockk {
            every { reservaDao() } returns reservaDao
            every { inventarioDao() } returns inventarioDao
        }

        mockkStatic("androidx.room.RoomDatabaseKt")
        val txSlot = slot<suspend () -> Any?>()
        coEvery { db.withTransaction(capture(txSlot)) } coAnswers {
            txSlot.captured.invoke()
        }

        repository = ReservaRepository(db)
    }

    afterTest {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    "crearReserva falla si no se selecciona ningún recurso" {
        val reserva = ReservaEntity(
            solicitanteId = 1L,
            nombreActividad = "Feria Tecnológica",
            fecha = fechaFuturaValida,
            bloqueInicio = 1,
            bloqueFin = 2
        )

        val resultado = repository.crearReserva(reserva, emptyMap())

        resultado.isFailure shouldBe true
        resultado.exceptionOrNull()?.message shouldContain "al menos un recurso"
    }

    "crearReserva falla si el nombre de actividad está vacío o los bloques son inválidos" {
        val reservaSinNombre = ReservaEntity(
            solicitanteId = 1L,
            nombreActividad = "   ",
            fecha = fechaFuturaValida,
            bloqueInicio = 1,
            bloqueFin = 2
        )
        val reservaBloqueInvalido = reservaSinNombre.copy(
            nombreActividad = "Charla",
            bloqueInicio = 5,
            bloqueFin = 2
        )

        repository.crearReserva(reservaSinNombre, mapOf(1L to 1)).isFailure shouldBe true
        repository.crearReserva(reservaBloqueInvalido, mapOf(1L to 1)).isFailure shouldBe true
    }

    "crearReserva falla si se solicita con menos de 48 horas de anticipación" {
        val reservaHoy = ReservaEntity(
            solicitanteId = 1L,
            nombreActividad = "Evento Urgente",
            fecha = LocalDate.now().toString(),
            bloqueInicio = 1,
            bloqueFin = 2
        )

        val resultado = repository.crearReserva(reservaHoy, mapOf(1L to 1))

        resultado.isFailure shouldBe true
        resultado.exceptionOrNull()?.message shouldContain "48 horas"
    }

    "crearReserva falla si se excede el máximo de adjuntos o el tamaño máximo de 5 MB" {
        val reserva = ReservaEntity(
            solicitanteId = 1L,
            nombreActividad = "Taller",
            fecha = fechaFuturaValida,
            bloqueInicio = 1,
            bloqueFin = 2
        )
        val muchosAdjuntos = List(MAX_ADJUNTOS + 1) { mockk<AdjuntoBorrador>(relaxed = true) }
        val adjuntoPesado = listOf(mockk<AdjuntoBorrador> {
            every { tamanoBytes } returns MAX_BYTES_ADJUNTO + 1
        })

        repository.crearReserva(reserva, mapOf(1L to 1), muchosAdjuntos).isFailure shouldBe true
        repository.crearReserva(reserva, mapOf(1L to 1), adjuntoPesado).isFailure shouldBe true
    }

    "crearReserva falla si no hay stock suficiente para el recurso solicitado" {
        val reserva = ReservaEntity(
            solicitanteId = 1L,
            nombreActividad = "Seminario",
            fecha = fechaFuturaValida,
            bloqueInicio = 1,
            bloqueFin = 2
        )
        coEvery { inventarioDao.obtenerRecursoPorId(10L) } returns RecursoInventarioEntity(
            id = 10L, departamento = deptoEjemplo, nombreItem = "Proyector", stockTotal = 3
        )
        coEvery { inventarioDao.obtenerCantidadOcupada(10L, fechaFuturaValida, any()) } returns 2

        val resultado = repository.crearReserva(reserva, mapOf(10L to 2))

        resultado.isFailure shouldBe true
        resultado.exceptionOrNull()?.message shouldContain "Stock insuficiente"
    }

    "crearReserva inserta reserva, requerimientos y adjuntos cuando todo es válido" {
        val reserva = ReservaEntity(
            solicitanteId = 1L,
            nombreActividad = "Hackathon Duoc",
            fecha = fechaFuturaValida,
            bloqueInicio = 2,
            bloqueFin = 4
        )
        val adjuntoMock = mockk<AdjuntoBorrador> {
            every { tipo } returns TipoAdjunto.entries.first()
            every { nombreArchivo } returns "programa.pdf"
            every { rutaLocal } returns "/tmp/programa.pdf"
            every { mimeType } returns "application/pdf"
            every { tamanoBytes } returns 2048L
        }

        coEvery { inventarioDao.obtenerRecursoPorId(10L) } returns RecursoInventarioEntity(
            id = 10L, departamento = deptoEjemplo, nombreItem = "Notebook", stockTotal = 10
        )
        coEvery { inventarioDao.obtenerCantidadOcupada(10L, fechaFuturaValida, any()) } returns 3
        coEvery { reservaDao.insertarReserva(any()) } returns 99L

        val resultado = repository.crearReserva(reserva, mapOf(10L to 4), listOf(adjuntoMock))

        resultado.isSuccess shouldBe true
        resultado.getOrNull() shouldBe 99L
        coVerify(exactly = 1) { reservaDao.insertarRequerimientos(any()) }
        coVerify(exactly = 1) { reservaDao.insertarAdjuntos(any()) }
    }

    "resolverArea exige motivo si rechaza y cambia estado a PENDIENTE_ADMIN cuando todas las áreas responden" {
        val reservaEntity = ReservaEntity(
            id = 5L, solicitanteId = 1L, nombreActividad = "Expo",
            fecha = fechaFuturaValida, bloqueInicio = 1, bloqueFin = 2, estado = EstadoReserva.PENDIENTE
        )
        val reservaConReqs = mockk<ReservaConRequerimientos> {
            every { reserva } returns reservaEntity
        }
        val reqAprobado = mockk<RequerimientoEntity> {
            every { estadoValidacion } returns EstadoRequerimiento.APROBADO
            every { motivoRechazo } returns null
        }

        coEvery { reservaDao.obtenerPorId(5L) } returns reservaConReqs
        coEvery { reservaDao.requerimientosDeReserva(5L) } returns listOf(reqAprobado)

        repository.resolverArea(5L, deptoEjemplo, aprobar = false, motivo = "", validadorId = 2L).isFailure shouldBe true

        val resOk = repository.resolverArea(5L, deptoEjemplo, aprobar = true, motivo = null, validadorId = 2L)
        resOk.isSuccess shouldBe true
        coVerify { reservaDao.actualizarEstado(5L, EstadoReserva.PENDIENTE_ADMIN, null) }
    }

    "confirmarPorAdmin valida stock y actualiza estado a CONFIRMADA" {
        val recurso = RecursoInventarioEntity(id = 1L, departamento = deptoEjemplo, nombreItem = "Cámara", stockTotal = 5)
        val req = mockk<RequerimientoEntity> {
            every { estadoValidacion } returns EstadoRequerimiento.APROBADO
            every { cantidadSolicitada } returns 2
        }
        val reqConRecurso = mockk<RequerimientoConRecurso> {
            every { requerimiento } returns req
            every { this@mockk.recurso } returns recurso
        }
        val reservaEntity = ReservaEntity(
            id = 8L, solicitanteId = 1L, nombreActividad = "Grabación",
            fecha = fechaFuturaValida, bloqueInicio = 1, bloqueFin = 1, estado = EstadoReserva.PENDIENTE_ADMIN
        )
        val reservaConReqs = mockk<ReservaConRequerimientos> {
            every { reserva } returns reservaEntity
            every { requerimientos } returns listOf(reqConRecurso)
        }

        coEvery { reservaDao.obtenerPorId(8L) } returns reservaConReqs
        coEvery { inventarioDao.obtenerCantidadOcupada(1L, fechaFuturaValida, 1) } returns 1

        val resultado = repository.confirmarPorAdmin(8L, excepcional = false)

        resultado.isSuccess shouldBe true
        coVerify { reservaDao.actualizarEstado(8L, EstadoReserva.CONFIRMADA, null) }
    }

    "rechazarPorAdmin exige motivo y cambia estado a RECHAZADA" {
        val reservaEntity = ReservaEntity(
            id = 9L, solicitanteId = 1L, nombreActividad = "Evento",
            fecha = fechaFuturaValida, bloqueInicio = 1, bloqueFin = 1, estado = EstadoReserva.PENDIENTE_ADMIN
        )
        val reservaConReqs = mockk<ReservaConRequerimientos> {
            every { reserva } returns reservaEntity
        }
        coEvery { reservaDao.obtenerPorId(9L) } returns reservaConReqs

        repository.rechazarPorAdmin(9L, "  ").isFailure shouldBe true

        val res = repository.rechazarPorAdmin(9L, "Falta autorización de dirección")
        res.isSuccess shouldBe true
        coVerify { reservaDao.actualizarEstado(9L, EstadoReserva.RECHAZADA, "Falta autorización de dirección") }
    }
})