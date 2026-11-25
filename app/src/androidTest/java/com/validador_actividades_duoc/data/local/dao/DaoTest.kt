package com.validador_actividades_duoc.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.validador_actividades_duoc.data.local.AppDatabase
import com.validador_actividades_duoc.data.local.entity.AdjuntoEntity
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.RequerimientoEntity
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var db: AppDatabase
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var inventarioDao: InventarioDao
    private lateinit var reservaDao: ReservaDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        usuarioDao = db.usuarioDao()
        inventarioDao = db.inventarioDao()
        reservaDao = db.reservaDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun usuarioDao_cubreContarBuscarPorUidYActualizarPerfilGoogle() = runBlocking {
        assertEquals(0, usuarioDao.contar())

        val id = usuarioDao.insertarUsuario(
            UsuarioEntity(
                correo = "inicial@duoc.cl",
                nombre = "Nombre Inicial",
                rol = RolUsuario.SOLICITANTE,
                firebaseUid = "uid-google-001"
            )
        )

        assertTrue(id > 0L)
        assertEquals(1, usuarioDao.contar())

        val encontrado = usuarioDao.obtenerUsuarioPorUid("uid-google-001")
        assertNotNull(encontrado)
        assertEquals("inicial@duoc.cl", encontrado?.correo)

        usuarioDao.actualizarPerfilGoogle(
            uid = "uid-google-001",
            correo = "actualizado@duoc.cl",
            nombre = "Nombre Actualizado"
        )

        val actualizado = usuarioDao.obtenerUsuarioPorUid("uid-google-001")
        assertEquals("actualizado@duoc.cl", actualizado?.correo)
        assertEquals("Nombre Actualizado", actualizado?.nombre)
    }

    @Test
    fun reservaDao_cubreConsultasPorEstadoSolicitanteFechaYAdjuntos() = runBlocking {
        val solicitanteId = usuarioDao.insertarUsuario(
            UsuarioEntity(
                correo = "profesor.test@duoc.cl",
                nombre = "Profesor Test",
                rol = RolUsuario.SOLICITANTE
            )
        )
        val recursoId = inventarioDao.insertarOActualizarRecurso(
            RecursoInventarioEntity(
                departamento = Departamento.DIGITALES,
                nombreItem = "Proyector Láser",
                stockTotal = 3
            )
        )

        val reservaId = reservaDao.insertarReserva(
            ReservaEntity(
                solicitanteId = solicitanteId,
                nombreActividad = "Seminario de Ingeniería",
                fecha = "2026-12-10",
                bloqueInicio = 2,
                bloqueFin = 3,
                estado = EstadoReserva.PENDIENTE
            )
        )

        reservaDao.insertarRequerimientos(
            listOf(
                RequerimientoEntity(
                    reservaId = reservaId,
                    recursoId = recursoId,
                    cantidadSolicitada = 1,
                    estadoValidacion = EstadoRequerimiento.PENDIENTE
                )
            )
        )

        reservaDao.insertarAdjuntos(
            listOf(
                AdjuntoEntity(
                    reservaId = reservaId,
                    tipo = TipoAdjunto.DOCUMENTO,
                    nombreArchivo = "pauta.pdf",
                    rutaLocal = "/tmp/pauta.pdf",
                    mimeType = "application/pdf",
                    tamanoBytes = 1024L
                )
            )
        )

        val todas = reservaDao.obtenerTodasLasReservasConRequerimientos().first()
        assertEquals(1, todas.size)
        assertEquals(1, todas.first().adjuntos.size)

        val porSolicitante = reservaDao.obtenerReservasPorSolicitante(solicitanteId.toInt()).first()
        assertEquals(1, porSolicitante.size)

        val porEstado = reservaDao.obtenerReservasPorEstado(EstadoReserva.PENDIENTE).first()
        assertEquals(1, porEstado.size)

        val deFecha = reservaDao.observarDeFecha("2026-12-10").first()
        assertEquals(1, deFecha.size)
        assertEquals("Seminario de Ingeniería", deFecha.first().nombreActividad)
    }
}