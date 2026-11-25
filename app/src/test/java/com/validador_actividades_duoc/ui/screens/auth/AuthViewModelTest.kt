package com.validador_actividades_duoc.ui.screens.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseUser
import com.validador_actividades_duoc.data.local.dao.UsuarioDao
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.FirebaseAuthRepository
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.RolUsuario
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest : StringSpec({

    val testDispatcher = StandardTestDispatcher()

    lateinit var usuarioDao: UsuarioDao
    lateinit var authRepository: FirebaseAuthRepository
    lateinit var activity: Activity

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        usuarioDao = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        activity = mockk(relaxed = true)
        every { authRepository.usuarioFirebase } returns null
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "restaurarSesion sin usuario en Firebase deja usuarioActual en null y cargando en false" {
        val viewModel = AuthViewModel(usuarioDao, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.usuarioActual.value shouldBe null
        viewModel.cargando.value shouldBe false
        viewModel.errorMessage.value shouldBe null
    }

    "restaurarSesion con usuario nuevo en Firebase lo registra localmente como SOLICITANTE" {
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "uid-123"
            every { email } returns "  Alumno.Nuevo@Duocuc.cl "
            every { displayName } returns "Alumno Nuevo"
        }
        val usuarioCreado = UsuarioEntity(
            id = 10L,
            correo = "alumno.nuevo@duocuc.cl",
            nombre = "Alumno Nuevo",
            rol = RolUsuario.SOLICITANTE,
            departamento = null,
            firebaseUid = "uid-123"
        )

        every { authRepository.usuarioFirebase } returns firebaseUser
        coEvery { usuarioDao.obtenerUsuarioPorUid("uid-123") } returns null
        coEvery { usuarioDao.obtenerUsuarioPorEmail("alumno.nuevo@duocuc.cl") } returns null
        coEvery { usuarioDao.insertarUsuario(any()) } returns 10L
        coEvery { usuarioDao.obtenerUsuarioPorId(10L) } returns usuarioCreado

        val viewModel = AuthViewModel(usuarioDao, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.usuarioActual.value shouldBe usuarioCreado
        viewModel.cargando.value shouldBe false
        coVerify(exactly = 1) {
            usuarioDao.insertarUsuario(
                match { it.rol == RolUsuario.SOLICITANTE && it.firebaseUid == "uid-123" }
            )
        }
    }

    "restaurarSesion con usuario existente actualiza su perfil y conserva su rol" {
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "uid-admin"
            every { email } returns "jefe@duoc.cl"
            every { displayName } returns "Jefe Actualizado"
        }
        val usuarioExistente = UsuarioEntity(
            id = 2L,
            correo = "jefe@duoc.cl",
            nombre = "Jefe Antiguo",
            rol = RolUsuario.ADMIN,
            departamento = null,
            firebaseUid = "uid-admin"
        )
        val usuarioActualizado = usuarioExistente.copy(nombre = "Jefe Actualizado")

        every { authRepository.usuarioFirebase } returns firebaseUser
        coEvery { usuarioDao.obtenerUsuarioPorUid("uid-admin") } returnsMany listOf(
            usuarioExistente,
            usuarioActualizado
        )

        val viewModel = AuthViewModel(usuarioDao, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.usuarioActual.value?.rol shouldBe RolUsuario.ADMIN
        viewModel.usuarioActual.value?.nombre shouldBe "Jefe Actualizado"
        coVerify(exactly = 1) {
            usuarioDao.actualizarPerfilGoogle("uid-admin", "jefe@duoc.cl", "Jefe Actualizado")
        }
    }

    "restaurarSesion muestra error si el correo ya existe localmente sin vincular" {
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "uid-999"
            every { email } returns "admin@duoc.cl"
            every { displayName } returns "Admin"
        }
        val usuarioLocalSinUid = UsuarioEntity(
            id = 1L,
            correo = "admin@duoc.cl",
            nombre = "Administrador",
            rol = RolUsuario.ADMIN
        )

        every { authRepository.usuarioFirebase } returns firebaseUser
        coEvery { usuarioDao.obtenerUsuarioPorUid("uid-999") } returns null
        coEvery { usuarioDao.obtenerUsuarioPorEmail("admin@duoc.cl") } returns usuarioLocalSinUid

        val viewModel = AuthViewModel(usuarioDao, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.usuarioActual.value shouldBe null
        viewModel.errorMessage.value shouldContain "vinculación administrativa"
    }

    "iniciarSesionGoogle autentica correctamente y actualiza usuarioActual" {
        val viewModel = AuthViewModel(usuarioDao, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "uid-google"
            every { email } returns "docente@profesor.duoc.cl"
            every { displayName } returns "Docente Duoc"
        }
        val usuarioGuardado = UsuarioEntity(
            id = 5L,
            correo = "docente@profesor.duoc.cl",
            nombre = "Docente Duoc",
            rol = RolUsuario.SOLICITANTE,
            firebaseUid = "uid-google"
        )

        coEvery { authRepository.iniciarSesionGoogle(activity) } returns firebaseUser
        coEvery { usuarioDao.obtenerUsuarioPorUid("uid-google") } returns null
        coEvery { usuarioDao.obtenerUsuarioPorEmail("docente@profesor.duoc.cl") } returns null
        coEvery { usuarioDao.insertarUsuario(any()) } returns 5L
        coEvery { usuarioDao.obtenerUsuarioPorId(5L) } returns usuarioGuardado

        viewModel.iniciarSesionGoogle(activity)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.usuarioActual.value shouldBe usuarioGuardado
        viewModel.errorMessage.value shouldBe null
        viewModel.cargando.value shouldBe false
    }

    "iniciarSesionGoogle captura excepciones y expone el mensaje de error" {
        val viewModel = AuthViewModel(usuarioDao, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { authRepository.iniciarSesionGoogle(activity) } throws RuntimeException("Credenciales canceladas")

        viewModel.iniciarSesionGoogle(activity)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.usuarioActual.value shouldBe null
        viewModel.errorMessage.value shouldBe "Credenciales canceladas"
        viewModel.cargando.value shouldBe false
    }

    "cerrarSesion limpia usuarioActual y llama al repositorio" {
        val viewModel = AuthViewModel(usuarioDao, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.cerrarSesion(activity)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.usuarioActual.value shouldBe null
        viewModel.cargando.value shouldBe false
        coVerify(exactly = 1) { authRepository.cerrarSesion(activity) }
    }

    "iniciarSesionDemo permite ingresar con correos autorizados y asigna su rol y departamento" {
        val viewModel = AuthViewModel(usuarioDao, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val casosDemo = listOf(
            Triple("admin@duoc.cl", RolUsuario.ADMIN, null),
            Triple("profesor@duoc.cl", RolUsuario.SOLICITANTE, null),
            Triple("digitales@duoc.cl", RolUsuario.VALIDADOR, Departamento.DIGITALES),
            Triple("porteria@duoc.cl", RolUsuario.VALIDADOR, Departamento.PORTERIA),
            Triple("auxiliares@duoc.cl", RolUsuario.VALIDADOR, Departamento.AUXILIARES),
            Triple("comunicacion@duoc.cl", RolUsuario.VALIDADOR, Departamento.COMUNICACION)
        )

        casosDemo.forEachIndexed { index, (correo, rolEsperado, deptoEsperado) ->
            val idSimulado = (index + 1).toLong()
            val entidadEsperada = UsuarioEntity(
                id = idSimulado,
                correo = correo,
                nombre = "Demo",
                rol = rolEsperado,
                departamento = deptoEsperado
            )
            coEvery { usuarioDao.obtenerUsuarioPorEmail(correo) } returns null
            coEvery { usuarioDao.insertarUsuario(any()) } returns idSimulado
            coEvery { usuarioDao.obtenerUsuarioPorId(idSimulado) } returns entidadEsperada

            viewModel.iniciarSesionDemo(activity, correo)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.usuarioActual.value?.rol shouldBe rolEsperado
            viewModel.usuarioActual.value?.departamento shouldBe deptoEsperado
        }
    }

    "iniciarSesionDemo rechaza correos no autorizados o vinculados a Google" {
        val viewModel = AuthViewModel(usuarioDao, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.iniciarSesionDemo(activity, "intruder@duoc.cl")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.errorMessage.value shouldContain "no está habilitado"

        coEvery { usuarioDao.obtenerUsuarioPorEmail("admin@duoc.cl") } returns UsuarioEntity(
            id = 1L,
            correo = "admin@duoc.cl",
            nombre = "Admin Real",
            rol = RolUsuario.ADMIN,
            firebaseUid = "uid-real"
        )
        viewModel.iniciarSesionDemo(activity, "admin@duoc.cl")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.errorMessage.value shouldContain "vinculado a una cuenta Google"
    }
})