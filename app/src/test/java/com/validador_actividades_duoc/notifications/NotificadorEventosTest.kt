package com.validador_actividades_duoc.notifications

import android.content.Context
import android.content.SharedPreferences
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.RolUsuario
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class NotificadorEventosTest : StringSpec({

    lateinit var appContext: Context
    lateinit var prefs: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var reservaRepository: ReservaRepository
    lateinit var notificador: NotificadorEventos

    val deptoEjemplo = Departamento.entries.first()

    beforeTest {
        editor = mockk(relaxed = true) {
            every { putBoolean(any(), any()) } returns this
            every { apply() } just runs
        }
        prefs = mockk(relaxed = true) {
            every { getBoolean(any(), any()) } returns false
            every { edit() } returns editor
        }
        appContext = mockk(relaxed = true) {
            every { applicationContext } returns this
            every { getSharedPreferences("notificaciones_enviadas", Context.MODE_PRIVATE) } returns prefs
        }
        reservaRepository = mockk(relaxed = true)

        mockkObject(NotificacionesHelper)
        every { NotificacionesHelper.mostrar(any(), any(), any(), any()) } returns true

        notificador = NotificadorEventos(appContext, reservaRepository)
    }

    afterTest {
        notificador.detener()
        unmockkObject(NotificacionesHelper)
    }

    "validador con departamento notifica una sola vez por solicitud pendiente" {
        val dispatcher = UnconfinedTestDispatcher()
        runTest(dispatcher) {
            val flujoArea = MutableSharedFlow<List<ReservaConRequerimientos>>(replay = 1)
            every { reservaRepository.solicitudesDeArea(deptoEjemplo) } returns flujoArea

            val validador = UsuarioEntity(
                id = 10L,
                correo = "validador@duoc.cl",
                nombre = "Validador Test",
                rol = RolUsuario.VALIDADOR,
                departamento = deptoEjemplo
            )
            val reserva = ReservaEntity(
                id = 42L,
                solicitanteId = 1L,
                nombreActividad = "Charla IA",
                fecha = "2026-12-10",
                bloqueInicio = 1,
                bloqueFin = 2,
                estado = EstadoReserva.PENDIENTE
            )
            val item = mockk<ReservaConRequerimientos>(relaxed = true) {
                every { this@mockk.reserva } returns reserva
                every { solicitante.nombre } returns "Ana Pérez"
            }

            notificador.iniciar(this, validador)
            flujoArea.emit(listOf(item))

            val clave = "u10:area:42"
            verify(exactly = 1) {
                NotificacionesHelper.mostrar(
                    appContext,
                    clave.hashCode(),
                    "Nueva solicitud para ${deptoEjemplo.etiqueta}",
                    "Charla IA · Ana Pérez. Revisa los recursos que te piden."
                )
            }
            verify(exactly = 1) { editor.putBoolean(clave, true) }

            every { prefs.getBoolean(clave, false) } returns true
            flujoArea.emit(listOf(item))

            verify(exactly = 1) { NotificacionesHelper.mostrar(any(), any(), any(), any()) }
            notificador.detener()
        }
    }
})