package com.validador_actividades_duoc.ui.screens.solicitante

import androidx.lifecycle.ViewModel
import com.validador_actividades_duoc.data.AdjuntoStorage
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.AdjuntoBorrador
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.MAX_ADJUNTOS
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import com.validador_actividades_duoc.domain.util.ResultadoAnticipacion
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SolicitanteViewModelTest : StringSpec({

    val testDispatcher = StandardTestDispatcher()
    val deptoEjemplo = Departamento.entries.first()

    lateinit var reservaRepository: ReservaRepository
    lateinit var inventarioRepository: InventarioRepository

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        reservaRepository = mockk(relaxed = true)
        inventarioRepository = mockk(relaxed = true)
        every { inventarioRepository.recursosDisponibles } returns flowOf(emptyList())
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "inicializarSolicitante carga las solicitudes del usuario y evita suscribirse dos veces al mismo ID" {
        every { reservaRepository.delSolicitante(10L) } returns flowOf(listOf(mockk(relaxed = true)))

        val viewModel = SolicitanteViewModel(reservaRepository, inventarioRepository)
        viewModel.inicializarSolicitante(10L)
        viewModel.inicializarSolicitante(10L) // Llamada duplicada ignorada
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.misSolicitudes.size shouldBe 1
        viewModel.uiState.value.cargando shouldBe false
        viewModel.uiState.value.mensajeError shouldBe null
        verify(exactly = 1) { reservaRepository.delSolicitante(10L) }
    }

    "inicializarSolicitante captura excepciones del flujo y actualiza mensajeError" {
        every { reservaRepository.delSolicitante(10L) } returns flow {
            throw RuntimeException("Error al consultar reservas")
        }

        val viewModel = SolicitanteViewModel(reservaRepository, inventarioRepository)
        viewModel.inicializarSolicitante(10L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.cargando shouldBe false
        viewModel.uiState.value.mensajeError shouldBe "Error al consultar reservas"
    }

    "cargarInventarioDisponible actualiza recursosDisponibles y calcula stockDisponibles" {
        val recurso = RecursoInventarioEntity(
            id = 1L,
            departamento = deptoEjemplo,
            nombreItem = "Proyector HDMI",
            stockTotal = 5
        )
        every { inventarioRepository.recursosDisponibles } returns flowOf(listOf(recurso))
        coEvery { inventarioRepository.calcularStockDisponible(1L, any(), 1, 1, 5) } returns 3

        val viewModel = SolicitanteViewModel(reservaRepository, inventarioRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.wizardState.value.recursosDisponibles shouldBe listOf(recurso)
        viewModel.wizardState.value.stockDisponibles[1L] shouldBe 3
    }

    "WizardState evalúa correctamente anticipación y validez de Paso 1 y Paso 2" {
        val estadoBloqueado = WizardState(
            nombreActividad = "Charla Hoy",
            fecha = LocalDate.now(),
            bloqueInicio = 1,
            bloqueFin = 1
        )
        estadoBloqueado.resultadoAnticipacion shouldBe ResultadoAnticipacion.BLOQUEADA
        estadoBloqueado.esPaso1Valido shouldBe false

        val estadoAdvertencia = WizardState(
            nombreActividad = "Taller Próximo",
            fecha = LocalDate.now().plusDays(5),
            bloqueInicio = 1,
            bloqueFin = 2
        )
        estadoAdvertencia.resultadoAnticipacion shouldBe ResultadoAnticipacion.ADVERTENCIA
        estadoAdvertencia.esPaso1Valido shouldBe true

        val estadoIdeal = WizardState(
            nombreActividad = "Feria Duoc",
            fecha = LocalDate.now().plusDays(20),
            bloqueInicio = 1,
            bloqueFin = 2,
            cantidadesInsumos = mapOf(1L to 2)
        )
        estadoIdeal.resultadoAnticipacion shouldBe ResultadoAnticipacion.OK
        estadoIdeal.esPaso1Valido shouldBe true
        estadoIdeal.esPaso2Valido shouldBe true
        estadoIdeal.copy(cantidadesInsumos = emptyMap()).esPaso2Valido shouldBe false
    }

    "acciones del calendario y wizard actualizan fecha, bloques, insumos y navegación entre pasos" {
        val viewModel = SolicitanteViewModel(reservaRepository, inventarioRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val nuevaFecha = LocalDate.now().plusDays(15)
        viewModel.seleccionarFechaCalendario(nuevaFecha)
        viewModel.uiState.value.fechaSeleccionada shouldBe nuevaFecha

        viewModel.prepararWizard(nuevaFecha, bloque = 3)
        viewModel.wizardState.value.bloqueInicio shouldBe 3
        viewModel.wizardState.value.bloqueFin shouldBe 3

        viewModel.actualizarNombreActividad("Hackathon")
        viewModel.actualizarFechaReserva(nuevaFecha.plusDays(2))
        viewModel.seleccionarRangoBloques(4, 2) // fin debe ajustarse a maxOf(4, 2) = 4
        viewModel.wizardState.value.bloqueInicio shouldBe 4
        viewModel.wizardState.value.bloqueFin shouldBe 4

        viewModel.actualizarCantidadInsumo(1L, 3)
        viewModel.wizardState.value.cantidadesInsumos[1L] shouldBe 3
        viewModel.actualizarCantidadInsumo(1L, 0)
        viewModel.wizardState.value.cantidadesInsumos.containsKey(1L) shouldBe false

        viewModel.siguientePasoWizard()
        viewModel.wizardState.value.pasoActual shouldBe 2
        viewModel.siguientePasoWizard()
        viewModel.wizardState.value.pasoActual shouldBe 3
        viewModel.siguientePasoWizard() // No pasa de 3
        viewModel.wizardState.value.pasoActual shouldBe 3

        viewModel.pasoAnteriorWizard()
        viewModel.wizardState.value.pasoActual shouldBe 2
        viewModel.pasoAnteriorWizard()
        viewModel.wizardState.value.pasoActual shouldBe 1
        viewModel.pasoAnteriorWizard() // No baja de 1
        viewModel.wizardState.value.pasoActual shouldBe 1

        viewModel.reiniciarWizard()
        viewModel.wizardState.value.nombreActividad shouldBe ""
    }

    "agregarAdjunto respeta MAX_ADJUNTOS y quitarAdjunto elimina el archivo físico y del estado" {
        mockkObject(AdjuntoStorage)
        justRun { AdjuntoStorage.eliminarArchivo(any()) }

        try {
            val viewModel = SolicitanteViewModel(reservaRepository, inventarioRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            repeat(MAX_ADJUNTOS + 2) { i ->
                viewModel.agregarAdjunto(
                    AdjuntoBorrador(
                        tipo = TipoAdjunto.DOCUMENTO,
                        nombreArchivo = "doc_$i.pdf",
                        rutaLocal = "/tmp/doc_$i.pdf",
                        mimeType = "application/pdf",
                        tamanoBytes = 1024L
                    )
                )
            }
            viewModel.wizardState.value.adjuntos.size shouldBe MAX_ADJUNTOS

            val primero = viewModel.wizardState.value.adjuntos.first()
            viewModel.quitarAdjunto(primero)

            viewModel.wizardState.value.adjuntos.size shouldBe MAX_ADJUNTOS - 1
            verify(exactly = 1) { AdjuntoStorage.eliminarArchivo(primero.rutaLocal) }
        } finally {
            unmockkObject(AdjuntoStorage)
        }
    }

    "confirmarReserva valida sesión activa y recursos seleccionados antes de guardar" {
        val viewModel = SolicitanteViewModel(reservaRepository, inventarioRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.confirmarReserva()
        viewModel.wizardState.value.mensajeError shouldBe "No hay una sesión de usuario activa"

        viewModel.limpiarMensajeError()
        viewModel.wizardState.value.mensajeError shouldBe null

        every { reservaRepository.delSolicitante(5L) } returns flowOf(emptyList())
        viewModel.inicializarSolicitante(5L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.confirmarReserva()
        viewModel.wizardState.value.mensajeError shouldBe "Debes seleccionar al menos un recurso"
    }

    "confirmarReserva guarda la solicitud exitosamente o reporta error del repositorio" {
        every { reservaRepository.delSolicitante(5L) } returns flowOf(emptyList())
        val viewModel = SolicitanteViewModel(reservaRepository, inventarioRepository)
        viewModel.inicializarSolicitante(5L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.actualizarNombreActividad("  Seminario Innovación  ")
        viewModel.actualizarCantidadInsumo(2L, 4)

        // Caso éxito
        coEvery { reservaRepository.crearReserva(any(), mapOf(2L to 4), emptyList()) } returns Result.success(99L)
        viewModel.confirmarReserva()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.wizardState.value.guardadoExitoso shouldBe true
        viewModel.wizardState.value.cargando shouldBe false
        coVerify(exactly = 1) {
            reservaRepository.crearReserva(
                match { it.solicitanteId == 5L && it.nombreActividad == "Seminario Innovación" },
                mapOf(2L to 4),
                emptyList()
            )
        }

        // Caso error
        coEvery { reservaRepository.crearReserva(any(), any(), any()) } returns Result.failure(
            IllegalStateException("Stock insuficiente")
        )
        viewModel.confirmarReserva()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.wizardState.value.mensajeError shouldBe "Stock insuficiente"
    }

    "SolicitanteViewModelFactory crea SolicitanteViewModel y rechaza clases desconocidas" {
        val factory = SolicitanteViewModelFactory(reservaRepository, inventarioRepository)
        val vm = factory.create(SolicitanteViewModel::class.java)
        (vm is SolicitanteViewModel) shouldBe true

        class OtroViewModel : ViewModel()
        shouldThrow<IllegalArgumentException> {
            factory.create(OtroViewModel::class.java)
        }
    }
})