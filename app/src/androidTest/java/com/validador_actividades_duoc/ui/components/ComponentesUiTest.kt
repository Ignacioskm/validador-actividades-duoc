package com.validador_actividades_duoc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.data.local.entity.AdjuntoEntity
import com.validador_actividades_duoc.data.local.entity.ReservaConRequerimientos
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.domain.model.AdjuntoBorrador
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import com.validador_actividades_duoc.domain.util.ResultadoAnticipacion
import com.validador_actividades_duoc.ui.theme.DuocTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ComponentesUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun duocPrimaryButton_ejecutaClickSoloCuandoEstaHabilitado() {
        var clics = 0
        var habilitado by mutableStateOf(true)

        composeTestRule.setContent {
            DuocTheme {
                DuocPrimaryButton(
                    texto = "Confirmar Reserva",
                    onClick = { clics++ },
                    habilitado = habilitado
                )
            }
        }

        composeTestRule.onNodeWithText("Confirmar Reserva")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        assertEquals(1, clics)

        habilitado = false
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Confirmar Reserva")
            .assertIsNotEnabled()
            .performClick()

        assertEquals(1, clics)
    }

    @Test
    fun formTextField_actualizaTextoYMuestraMensajeError() {
        var texto by mutableStateOf("")

        composeTestRule.setContent {
            DuocTheme {
                FormTextField(
                    valor = texto,
                    alCambiarValor = { texto = it },
                    etiqueta = "Nombre de la Actividad",
                    mensajeError = if (texto.isBlank()) "Campo obligatorio" else null
                )
            }
        }

        composeTestRule.onNodeWithText("Campo obligatorio").assertIsDisplayed()

        composeTestRule.onNodeWithText("Nombre de la Actividad")
            .performTextInput("Charla Ciberseguridad")

        assertEquals("Charla Ciberseguridad", texto)
        composeTestRule.onNodeWithText("Campo obligatorio").assertDoesNotExist()
    }

    @Test
    fun contadorCantidad_respetaLimitesDeStockYActualizaCantidad() {
        var cantidad by mutableIntStateOf(0)

        composeTestRule.setContent {
            DuocTheme {
                ContadorCantidad(
                    nombreRecurso = "Proyector HDMI",
                    cantidadActual = cantidad,
                    stockDisponible = 2,
                    alCambiarCantidad = { cantidad = it }
                )
            }
        }

        val btnDisminuir =
            composeTestRule.onNodeWithContentDescription("Disminuir cantidad de Proyector HDMI")
        val btnAumentar =
            composeTestRule.onNodeWithContentDescription("Aumentar cantidad de Proyector HDMI")

        composeTestRule.onNodeWithText("Proyector HDMI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disponible: 2 unid.").assertIsDisplayed()
        btnDisminuir.assertIsNotEnabled()
        btnAumentar.assertIsEnabled()

        btnAumentar.performClick()
        assertEquals(1, cantidad)
        btnDisminuir.assertIsEnabled()

        btnAumentar.performClick()
        assertEquals(2, cantidad)
        btnAumentar.assertIsNotEnabled()
    }

    @Test
    fun bannerAnticipacion_muestraMensajesDePlazoCorrectos() {
        var estado by mutableStateOf(ResultadoAnticipacion.OK)

        composeTestRule.setContent {
            DuocTheme {
                BannerAnticipacion(resultado = estado)
            }
        }

        composeTestRule.onNodeWithText("Su reserva ha sido realizada dentro del plazo.")
            .assertIsDisplayed()

        estado = ResultadoAnticipacion.ADVERTENCIA
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(
            "Su pedido de reserva tiene menos de 14 días de antelación. Esta solicitud requerirá revisión especial."
        ).assertIsDisplayed()

        estado = ResultadoAnticipacion.BLOQUEADA
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(
            "Reserva bloqueada. Debe solicitar las reservas con un mínimo de 48 horas de anticipación."
        ).assertIsDisplayed()
    }

    @Test
    fun seccionAreaAcordeon_expandeContenidoAlHacerClic() {
        composeTestRule.setContent {
            DuocTheme {
                SeccionAreaAcordeon(
                    departamento = Departamento.DIGITALES,
                    totalSeleccionados = 0
                ) {
                    ContadorCantidad(
                        nombreRecurso = "Micrófono Inalámbrico",
                        cantidadActual = 0,
                        stockDisponible = 5,
                        alCambiarCantidad = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("DIGITALES").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sin recursos seleccionados").assertIsDisplayed()
        composeTestRule.onNodeWithText("Micrófono Inalámbrico").assertDoesNotExist()

        composeTestRule.onNodeWithText("DIGITALES").performClick()
        composeTestRule.onNodeWithText("Micrófono Inalámbrico").assertIsDisplayed()
    }

    val solicitantePrueba = UsuarioEntity(
        id = 1L,
        correo = "solicitante@duoc.cl",
        nombre = "Solicitante Prueba",
        rol = RolUsuario.SOLICITANTE
    )

    @Test
    fun tarjetaReserva_muestraSingularYPluralDeInsumosYEjecutaClic() {
        var clics = 0

        composeTestRule.setContent {
            DuocTheme {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TarjetaReserva(
                        nombreActividad = "Charla IA",
                        fechaHoraTexto = "Fecha: 2026-12-01 | B1 al B2",
                        estado = EstadoReserva.PENDIENTE,
                        cantidadInsumos = 1,
                        alHacerClic = { clics++ }
                    )
                    TarjetaReserva(
                        reservaConReq = ReservaConRequerimientos(
                            reserva = ReservaEntity(
                                id = 2L,
                                solicitanteId = 1L,
                                nombreActividad = "Hackathon Duoc",
                                fecha = "2026-12-05",
                                bloqueInicio = 3,
                                bloqueFin = 5,
                                estado = EstadoReserva.CONFIRMADA
                            ),
                            requerimientos = emptyList(),
                            solicitante = solicitantePrueba,
                            adjuntos = emptyList()
                        ),
                        alHacerClic = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Charla IA").assertIsDisplayed().performClick()
        assertEquals(1, clics)

        composeTestRule.onNodeWithText("1 insumo solicitado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hackathon Duoc").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fecha: 2026-12-05 | B3 al B5").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 insumos solicitados").assertIsDisplayed()
    }

    @Test
    fun adjuntosSection_muestraControlesListaAdjuntosYPermiteQuitar() {
        var quitado: AdjuntoBorrador? = null
        val borradorDoc = AdjuntoBorrador(
            nombreArchivo = "programa.pdf",
            rutaLocal = "/tmp/programa.pdf",
            tipo = TipoAdjunto.DOCUMENTO,
            mimeType = "application/pdf",
            tamanoBytes = 2048L
        )
        val borradorImg = AdjuntoBorrador(
            nombreArchivo = "afiche.jpg",
            rutaLocal = "/tmp/afiche.jpg",
            tipo = TipoAdjunto.IMAGEN,
            mimeType = "image/jpeg",
            tamanoBytes = 4096L
        )

        composeTestRule.setContent {
            DuocTheme {
                AdjuntosSection(
                    adjuntos = listOf(borradorDoc, borradorImg),
                    alAgregar = {},
                    alQuitar = { quitado = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Documentos de la actividad (opcional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tomar foto").assertIsDisplayed()
        composeTestRule.onNodeWithText("Galería").assertIsDisplayed()
        composeTestRule.onNodeWithText("Archivo PDF / Word").assertIsDisplayed()

        composeTestRule.onNodeWithText("programa.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 KB").assertIsDisplayed()
        composeTestRule.onNodeWithText("afiche.jpg").assertIsDisplayed()
        composeTestRule.onNodeWithText("4 KB").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("Quitar").onFirst().performClick()
        assertEquals(borradorDoc, quitado)
    }

    @Test
    fun adjuntosLista_ocultaSiEstaVaciaYMuestraElementosConBotonAbrir() {
        val adjuntoInexistente = AdjuntoEntity(
            id = 1L,
            reservaId = 10L,
            nombreArchivo = "acta.pdf",
            rutaLocal = "/tmp/no_existe_acta.pdf",
            tipo = TipoAdjunto.DOCUMENTO,
            mimeType = "application/pdf",
            tamanoBytes = 1024L
        )

        composeTestRule.setContent {
            DuocTheme {
                Column {
                    AdjuntosLista(adjuntos = emptyList())
                    AdjuntosLista(adjuntos = listOf(adjuntoInexistente))
                }
            }
        }

        composeTestRule.onNodeWithText("Documentos adjuntos:").assertIsDisplayed()
        composeTestRule.onNodeWithText("acta.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abrir").assertIsDisplayed().performClick()
    }
}