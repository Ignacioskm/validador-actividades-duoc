package com.validador_actividades_duoc.data.repository

import com.validador_actividades_duoc.data.local.dao.InventarioDao
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.domain.model.Departamento
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class InventarioRepositoryTest : StringSpec({

    val deptoEjemplo = Departamento.entries.first()

    fun crearDaoMock(): InventarioDao = mockk<InventarioDao>(relaxed = true).apply {
        every { obtenerRecursosDisponibles() } returns flowOf(emptyList())
        every { observarPorDepartamento(any()) } returns flowOf(emptyList())
    }

    "recursosDisponibles emite la lista activa entregada por InventarioDao" {
        val dao = crearDaoMock()
        val listaMock = listOf(
            RecursoInventarioEntity(id = 1L, departamento = deptoEjemplo, nombreItem = "Proyector", stockTotal = 5)
        )
        every { dao.obtenerRecursosDisponibles() } returns flowOf(listaMock)

        val repo = InventarioRepository(dao)
        repo.recursosDisponibles.first() shouldBe listaMock
    }

    "recursosDeArea filtra por el departamento indicado" {
        val dao = crearDaoMock()
        val listaDepto = listOf(
            RecursoInventarioEntity(id = 2L, departamento = deptoEjemplo, nombreItem = "Micrófono", stockTotal = 3)
        )
        every { dao.observarPorDepartamento(deptoEjemplo) } returns flowOf(listaDepto)

        val repo = InventarioRepository(dao)
        repo.recursosDeArea(deptoEjemplo).first() shouldBe listaDepto
    }

    "crearRecurso recorta espacios del nombre e inserta en el DAO cuando los datos son válidos" {
        val dao = crearDaoMock()
        coEvery { dao.insertarOActualizarRecurso(any()) } returns 15L
        val repo = InventarioRepository(dao)

        val resultado = repo.crearRecurso(deptoEjemplo, "  Parlante JBL  ", 4)

        resultado.isSuccess shouldBe true
        resultado.getOrNull() shouldBe 15L
        coVerify(exactly = 1) {
            dao.insertarOActualizarRecurso(
                match { it.nombreItem == "Parlante JBL" && it.stockTotal == 4 && it.departamento == deptoEjemplo }
            )
        }
    }

    "crearRecurso falla si el nombre está vacío o el stock es negativo" {
        val dao = crearDaoMock()
        val repo = InventarioRepository(dao)

        val resNombreVacio = repo.crearRecurso(deptoEjemplo, "   ", 5)
        val resStockNegativo = repo.crearRecurso(deptoEjemplo, "Atril", -1)

        resNombreVacio.isFailure shouldBe true
        resStockNegativo.isFailure shouldBe true
        coVerify(exactly = 0) { dao.insertarOActualizarRecurso(any()) }
    }

    "editarStock actualiza en DAO si stock >= 0 y falla si es negativo" {
        val dao = crearDaoMock()
        val repo = InventarioRepository(dao)

        repo.editarStock(1L, 10).isSuccess shouldBe true
        coVerify(exactly = 1) { dao.actualizarStock(1L, 10) }

        repo.editarStock(1L, -2).isFailure shouldBe true
        coVerify(exactly = 0) { dao.actualizarStock(1L, -2) }
    }

    "retirarRecurso llama a desactivar en el DAO" {
        val dao = crearDaoMock()
        val repo = InventarioRepository(dao)

        val resultado = repo.retirarRecurso(7L)

        resultado.isSuccess shouldBe true
        coVerify(exactly = 1) { dao.desactivar(7L) }
    }

    "calcularStockDisponible resta el máximo ocupado dentro del rango de bloques" {
        val dao = crearDaoMock()
        coEvery { dao.obtenerCantidadOcupada(1L, "2026-12-15", 2) } returns 1
        coEvery { dao.obtenerCantidadOcupada(1L, "2026-12-15", 3) } returns 4
        coEvery { dao.obtenerCantidadOcupada(1L, "2026-12-15", 4) } returns 2

        val repo = InventarioRepository(dao)
        val disponible = repo.calcularStockDisponible(
            recursoId = 1L,
            fecha = "2026-12-15",
            bloqueInicio = 2,
            bloqueFin = 4,
            stockTotal = 6
        )

        disponible shouldBe 2
    }

    "calcularStockDisponible nunca devuelve valores negativos" {
        val dao = crearDaoMock()
        coEvery { dao.obtenerCantidadOcupada(1L, "2026-12-15", 1) } returns 10

        val repo = InventarioRepository(dao)
        val disponible = repo.calcularStockDisponible(1L, "2026-12-15", 1, 1, stockTotal = 5)

        disponible shouldBe 0
    }
})