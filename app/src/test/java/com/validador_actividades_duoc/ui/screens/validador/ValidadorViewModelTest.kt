package com.validador_actividades_duoc.ui.screens.validador

import android.content.Context
import com.validador_actividades_duoc.data.local.AppContainer
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.RequerimientoConRecurso
import com.validador_actividades_duoc.data.local.entity.RequerimientoEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.RolUsuario
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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
class ValidadorViewModelTest : StringSpec({

    val testDispatcher = StandardTestDispatcher()

    val validadorDigitales = UsuarioEntity(
        id = 7L,
        correo = "digitales@duoc.cl",
        nombre = "Validador Digitales",
        rol = RolUsuario.VALIDADOR,
        departamento = Departamento.DIGITALES
    )

    lateinit var reservaRepository: ReservaRepository
    lateinit var inventarioRepository: InventarioRepository

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        reservaRepository = mockk(relaxed = true)
        inventarioRepository = mockk(relaxed = true)

        every { reservaRepository.solicitudesDeArea(Departamento.DIGITALES) } returns flowOf(emptyList())
        every { inventarioRepository.recursosDeArea(Departamento.DIGITALES) } returns flowOf(emptyList())
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "ValidadorViewModel lanza IllegalArgumentException si el usuario no tiene departamento asignado" {
        val validadorSinDepto = validadorDigitales.copy(departamento = null)

        shouldThrow<IllegalArgumentException> {
            ValidadorViewModel(reservaRepository, inventarioRepository, validadorSinDepto)
        }
    }

    "solicitudes filtra y muestra únicamente los requerimientos del departamento del validador" {
        val recursoDigital = RecursoInventarioEntity(1L, Departamento.DIGITALES, "Proyector HDMI", 5)
        val recursoPorteria = RecursoInventarioEntity(2L, Departamento.PORTERIA, "Valla Papal", 10)

        val reqDigital = RequerimientoConRecurso(
            requerimiento = RequerimientoEntity(101L, 50L, 1L, 2),
            recurso = recursoDigital
        )
        val reqPorteria = RequerimientoConRecurso(
            requerimiento = RequerimientoEntity(102L, 50L, 2L, 4),
            recurso = recursoPorteria
        )

        val reservaMixta = ReservaConRequerimientos(
            reserva = ReservaEntity(50L, 3L, "Feria Académica", "2026-12-18", 1, 3),
            requerimientos = listOf(reqDigital, reqPorteria),
            solicitante = UsuarioEntity(3L, "profe@duoc.cl", "Profesor", RolUsuario.SOLICITANTE),
            adjuntos = emptyList()
        )

        every { reservaRepository.solicitudesDeArea(Departamento.DIGITALES) } returns flowOf(listOf(reservaMixta))

        val viewModel = ValidadorViewModel(reservaRepository, inventarioRepository, validadorDigitales)
        val job = launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            viewModel.solicitudes.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.solicitudes.value.size shouldBe 1
        val itemsVisibles = viewModel.solicitudes.value.first().items
        itemsVisibles.size shouldBe 1
        itemsVisibles.first().recurso.nombreItem shouldBe "Proyector HDMI"

        job.cancel()
    }

    "inventario expone los recursos pertenecientes al departamento del validador" {
        val recurso = RecursoInventarioEntity(1L, Departamento.DIGITALES, "Micrófono", 4)
        every { inventarioRepository.recursosDeArea(Departamento.DIGITALES) } returns flowOf(listOf(recurso))

        val viewModel = ValidadorViewModel(reservaRepository, inventarioRepository, validadorDigitales)
        val job = launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            viewModel.inventario.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.inventario.value shouldBe listOf(recurso)
        job.cancel()
    }

    "aprobar y rechazar invocan resolverArea con el departamento e ID del validador y actualizan mensaje" {
        val viewModel = ValidadorViewModel(reservaRepository, inventarioRepository, validadorDigitales)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery {
            reservaRepository.resolverArea(12L, Departamento.DIGITALES, true, null, 7L)
        } returns Result.success(Unit)

        viewModel.aprobar(12L)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Solicitud aprobada"

        viewModel.mensajeMostrado()
        viewModel.mensaje.value shouldBe null

        coEvery {
            reservaRepository.resolverArea(13L, Departamento.DIGITALES, false, "Sin equipos disponibles", 7L)
        } returns Result.success(Unit)

        viewModel.rechazar(13L, "Sin equipos disponibles")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Solicitud rechazada"

        coEvery {
            reservaRepository.resolverArea(14L, Departamento.DIGITALES, false, "", 7L)
        } returns Result.failure(IllegalArgumentException("Indica el motivo del rechazo"))

        viewModel.rechazar(14L, "")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Indica el motivo del rechazo"
    }

    "crearRecurso, editarStock y retirarRecurso delegan en InventarioRepository y notifican resultado" {
        val viewModel = ValidadorViewModel(reservaRepository, inventarioRepository, validadorDigitales)
        testDispatcher.scheduler.advanceUntilIdle()

        // crear
        coEvery { inventarioRepository.crearRecurso(Departamento.DIGITALES, "Cable HDMI", 8) } returns Result.success(1L)
        viewModel.crearRecurso("Cable HDMI", 8)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Recurso creado"

        // editar stock
        coEvery { inventarioRepository.editarStock(1L, 12) } returns Result.success(Unit)
        viewModel.editarStock(1L, 12)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Stock actualizado"

        coEvery { inventarioRepository.retirarRecurso(1L) } returns Result.success(Unit)
        viewModel.retirarRecurso(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "Recurso retirado"

        // error en crear
        coEvery { inventarioRepository.crearRecurso(Departamento.DIGITALES, "", 1) } returns Result.failure(
            IllegalArgumentException("El nombre no puede estar vacío")
        )
        viewModel.crearRecurso("", 1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.mensaje.value shouldBe "El nombre no puede estar vacío"

        coVerify(exactly = 1) { inventarioRepository.retirarRecurso(1L) }
    }

    "ValidadorViewModelFactory crea correctamente una instancia de ValidadorViewModel" {
        mockkObject(AppContainer)
        try {
            val mockContext = mockk<Context>(relaxed = true)
            every { mockContext.applicationContext } returns mockContext
            every { AppContainer.provideReservaRepository(mockContext) } returns reservaRepository
            every { AppContainer.provideInventarioRepository(mockContext) } returns inventarioRepository

            val factory = ValidadorViewModelFactory(mockContext, validadorDigitales)
            val vm = factory.create(ValidadorViewModel::class.java)
            (vm is ValidadorViewModel) shouldBe true
        } finally {
            unmockkObject(AppContainer)
        }
    }
})