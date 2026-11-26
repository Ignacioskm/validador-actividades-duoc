package com.validador_actividades_duoc.data.local

import android.content.Context
import androidx.room.withTransaction
import com.validador_actividades_duoc.data.local.dao.InventarioDao
import com.validador_actividades_duoc.data.local.dao.ReservaDao
import com.validador_actividades_duoc.data.local.dao.UsuarioDao
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.RolUsuario
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest

class AppContainerTest : StringSpec({

    beforeTest {
        mockkObject(AppContainer)
        mockkStatic("androidx.room.RoomDatabaseKt")
    }

    afterTest {
        unmockkStatic("androidx.room.RoomDatabaseKt")
        unmockkObject(AppContainer)
    }

    "sembrarDatosIniciales no inserta nada si ya existen usuarios" {
        runTest {
            val context = mockk<Context>(relaxed = true)
            val usuarioDao = mockk<UsuarioDao>(relaxed = true)
            val inventarioDao = mockk<InventarioDao>(relaxed = true)
            val reservaDao = mockk<ReservaDao>(relaxed = true)
            val db = mockk<AppDatabase>(relaxed = true) {
                every { usuarioDao() } returns usuarioDao
                every { inventarioDao() } returns inventarioDao
                every { reservaDao() } returns reservaDao
            }

            coEvery { usuarioDao.contar() } returns 5
            every { AppContainer.provideDatabase(context) } returns db

            AppContainer.sembrarDatosIniciales(context)

            coVerify(exactly = 0) { usuarioDao.insertarUsuario(any()) }
            coVerify(exactly = 0) { inventarioDao.insertarOActualizarRecurso(any()) }
            coVerify(exactly = 0) { reservaDao.insertarReserva(any()) }
        }
    }

    "sembrarDatosIniciales puebla usuarios, inventario y reserva demo cuando la base está vacía" {
        runTest {
            val context = mockk<Context>(relaxed = true)
            val usuarioDao = mockk<UsuarioDao>(relaxed = true)
            val inventarioDao = mockk<InventarioDao>(relaxed = true)
            val reservaDao = mockk<ReservaDao>(relaxed = true)
            val db = mockk<AppDatabase>(relaxed = true) {
                every { usuarioDao() } returns usuarioDao
                every { inventarioDao() } returns inventarioDao
                every { reservaDao() } returns reservaDao
            }

            val txSlot = slot<suspend () -> Any?>()
            coEvery { db.withTransaction(capture(txSlot)) } coAnswers {
                txSlot.captured.invoke()
            }

            coEvery { usuarioDao.contar() } returns 0
            coEvery { usuarioDao.insertarUsuario(any()) } returnsMany (1L..20L).toList()
            coEvery { inventarioDao.insertarOActualizarRecurso(any()) } returnsMany (1L..20L).toList()
            coEvery { inventarioDao.obtenerRecursoPorId(any()) } answers {
                val id = firstArg<Long>()
                mockk(relaxed = true) {
                    every { this@mockk.id } returns id
                    every { stockTotal } returns 50
                    every { nombreItem } returns "Recurso $id"
                }
            }
            coEvery { inventarioDao.obtenerCantidadOcupada(any(), any(), any()) } returns 0
            coEvery { reservaDao.insertarReserva(any()) } returns 100L
            every { AppContainer.provideDatabase(context) } returns db

            AppContainer.sembrarDatosIniciales(context)

            coVerify {
                usuarioDao.insertarUsuario(match { it.correo == "admin@duoc.cl" && it.rol == RolUsuario.ADMIN })
                usuarioDao.insertarUsuario(match { it.correo == "profesor@duoc.cl" && it.rol == RolUsuario.SOLICITANTE })
            }
            coVerify(atLeast = Departamento.entries.size) {
                usuarioDao.insertarUsuario(match { it.rol == RolUsuario.VALIDADOR })
            }
            coVerify(atLeast = 9) {
                inventarioDao.insertarOActualizarRecurso(any())
            }
            coVerify(exactly = 1) {
                reservaDao.insertarReserva(match { it.nombreActividad.contains("DEMO") })
            }
        }
    }
})