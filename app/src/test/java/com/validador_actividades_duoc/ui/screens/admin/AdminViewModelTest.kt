package com.validador_actividades_duoc.ui.screens.admin

import android.content.Context
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.remote.ActualizarStockRequest
import com.validador_actividades_duoc.data.remote.CrearRecursoRequest
import com.validador_actividades_duoc.data.remote.InventarioApiService
import com.validador_actividades_duoc.data.remote.RecursoDto
import com.validador_actividades_duoc.data.remote.RetrofitInstance
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.RolUsuario
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest : StringSpec({

    val testDispatcher = StandardTestDispatcher()

    lateinit var reservaRepository: ReservaRepository
    lateinit var inventarioRepository: InventarioRepository
    lateinit var inventarioApi: InventarioApiService

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        reservaRepository = mockk(relaxed = true)
        inventarioRepository = mockk(relaxed = true)
        inventarioApi = mockk(relaxed = true)

        every { reservaRepository.pendientesDeAdmin() } returns flowOf(emptyList())
        every { reservaRepository.confirmadas() } returns flowOf(emptyList())

        mockkObject(RetrofitInstance)
        every { RetrofitInstance.inventarioApi } returns inventarioApi
        coEvery { inventarioApi.obtenerRecursos(any()) } returns emptyList()
    }

    afterTest {
        unmockkObject(RetrofitInstance)
        Dispatchers.resetMain()
    }

    "cargarInventarioRemoto filtra solo recursos activos y actualiza inventarioGlobal" {
        coEvery { inventarioApi.obtenerRecursos(any()) } returns listOf(
            RecursoDto(1L, Departamento.DIGITALES.name, "Proyector 4K", 5, activo = true),
            RecursoDto(2L, Departamento.PORTERIA.name, "Valla Antigua", 10, activo = false)
        )

        val viewModel = AdminViewModel(reservaRepository, inventarioRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.cargandoInventario.value shouldBe false
        viewModel.errorInventario.value shouldBe null
        viewModel.inventarioGlobal.value.size shouldBe 1
        viewModel.inventarioGlobal.value.first().nombreItem shouldBe "Proyector 4K"
    }

    "cargarInventarioRemoto captura errores de red y expone errorInventario si no es silencioso" {
        coEvery { inventarioApi.obtenerRecursos(any()) } throws RuntimeException("Timeout Oracle OCI")

        val viewModel = AdminViewModel(reservaRepository, inventarioRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.cargandoInventario.value shouldBe false
        viewModel.errorInventario.value shouldContain "Timeout Oracle OCI"
    }

    "eventosConfirmados filtra por nombre de actividad, nombre de solicitante o correo" {
        val solicitante1 = UsuarioEntity(1L, "ana@duocuc.cl", "Ana Pérez", RolUsuario.SOLICITANTE)
        val solicitante2 = UsuarioEntity(2L, "luis@duoc.cl", "Luis Soto", RolUsuario.SOLICITANTE)

        val evento1 = ReservaConRequerimientos(
            reserva = ReservaEntity(10L, 1L, "Feria de Empleabilidad", "2026-12-10", 1, 3, EstadoReserva.CONFIRMADA),
            requerimientos = emptyList(),
            solicitante = solicitante1,
            adjuntos = emptyList()
        )
        val evento2 = ReservaConRequerimientos(
            reserva = ReservaEntity(11L, 2L, "Torneo E-Sports", "2026-12-12", 4, 6, EstadoReserva.CONFIRMADA),
            requerimientos = emptyList(),
            solicitante = solicitante2,
            adjuntos = emptyList()
        )

        every { reservaRepository.confirmadas() } returns flowOf(listOf(evento1, evento2))

        val viewModel = AdminViewModel(reservaRepository, inventarioRepository)
        val collectorJob = launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            viewModel.eventosConfirmados.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // sin filtro muestra ambos
        viewModel.eventosConfirmados.value.size shouldBe 2

        // filtra por actividad
        viewModel.onBusquedaChanged("e-sports")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.eventosConfirmados.value.map { it.reserva.id } shouldBe listOf(11L)

        // filtra por correo
        viewModel.onBusquedaChanged("ana@duocuc.cl")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.eventosConfirmados.value.map { it.reserva.id } shouldBe listOf(10L)

        collectorJob.cancel()
    }

    "confirmarEvento, confirmarEventoExcepcional y rechazarEvento actualizan el mensaje según el resultado" {
        val viewModel = AdminViewModel(reservaRepository, inventarioRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // confirmacion normal
        coEvery { reservaRepository.confirmarPorAdmin(5L, false) } returns Result.success(Unit)
        viewModel.confirmarEvento(5L)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Actividad confirmada oficialmente"

        viewModel.mensajeMostrado()
        viewModel.mensaje.value shouldBe null

        // confirmacion excepcional
        coEvery { reservaRepository.confirmarPorAdmin(6L, true) } returns Result.success(Unit)
        viewModel.confirmarEventoExcepcional(6L)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Actividad confirmada excepcionalmente"

        // rechazo exitoso
        coEvery { reservaRepository.rechazarPorAdmin(7L, "Choque de horario") } returns Result.success(Unit)
        viewModel.rechazarEvento(7L, "Choque de horario")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Actividad rechazada por el Administrador"

        // fallo al confirmar
        coEvery { reservaRepository.confirmarPorAdmin(8L, false) } returns Result.failure(
            IllegalStateException("Stock insuficiente")
        )
        viewModel.confirmarEvento(8L)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Stock insuficiente"
    }

    "editarStockGlobal actualiza el recurso en inventarioGlobal y notifica éxito o error" {
        coEvery { inventarioApi.obtenerRecursos(any()) } returns listOf(
            RecursoDto(1L, Departamento.AUXILIARES.name, "Silla Plegable", 20, activo = true)
        )
        val viewModel = AdminViewModel(reservaRepository, inventarioRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            inventarioApi.actualizarStock(1L, ActualizarStockRequest(35))
        } returns RecursoDto(1L, Departamento.AUXILIARES.name, "Silla Plegable", 35, activo = true)

        viewModel.editarStockGlobal(1L, 35)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.inventarioGlobal.value.first().stockTotal shouldBe 35
        viewModel.mensaje.value shouldBe "Stock actualizado en Oracle Cloud"

        coEvery { inventarioApi.actualizarStock(1L, any()) } throws RuntimeException("Error 500")
        viewModel.editarStockGlobal(1L, 50)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldContain "Error al actualizar stock"
    }

    "crearRecursoGlobal agrega el nuevo recurso ordenado y muestra la etiqueta del departamento" {
        coEvery { inventarioApi.obtenerRecursos(any()) } returns listOf(
            RecursoDto(1L, Departamento.PORTERIA.name, "Cono Vial", 10, activo = true)
        )
        val viewModel = AdminViewModel(reservaRepository, inventarioRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            inventarioApi.crearRecurso(
                CrearRecursoRequest(Departamento.DIGITALES.name, "Cámara Sony", 3)
            )
        } returns RecursoDto(2L, Departamento.DIGITALES.name, "Cámara Sony", 3, activo = true)

        viewModel.crearRecursoGlobal(Departamento.DIGITALES, "  Cámara Sony  ", 3)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.inventarioGlobal.value.size shouldBe 2
        viewModel.inventarioGlobal.value.first().departamento shouldBe Departamento.DIGITALES
        viewModel.mensaje.value shouldBe "Recurso añadido en Oracle Cloud (${Departamento.DIGITALES.etiqueta})"
        coVerify(exactly = 1) {
            inventarioApi.crearRecurso(CrearRecursoRequest(Departamento.DIGITALES.name, "Cámara Sony", 3))
        }
    }

    "AdminViewModelFactory instancia AdminViewModel correctamente" {
        mockkObject(AppContainer)
        try {
            val mockContext = mockk<Context>(relaxed = true)
            every { mockContext.applicationContext } returns mockContext
            every { AppContainer.provideReservaRepository(mockContext) } returns reservaRepository
            every { AppContainer.provideInventarioRepository(mockContext) } returns inventarioRepository

            val factory = AdminViewModelFactory(mockContext)
            val vm = factory.create(AdminViewModel::class.java)
            (vm is AdminViewModel) shouldBe true
        } finally {
            unmockkObject(AppContainer)
        }
    }
})