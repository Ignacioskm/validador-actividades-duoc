package com.validador_actividades_duoc.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.validador_actividades_duoc.domain.model.AdjuntoBorrador
import com.validador_actividades_duoc.domain.model.MAX_ADJUNTOS
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import com.validador_actividades_duoc.ui.theme.DuocTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class AdjuntosSectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val archivosTemporales = mutableListOf<File>()

    private fun crearImagenTemporal(nombre: String = "test_miniatura.jpg"): File {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val archivo = File(context.cacheDir, nombre)
        FileOutputStream(archivo).use { out ->
            val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.YELLOW)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            bitmap.recycle()
        }
        archivosTemporales.add(archivo)
        return archivo
    }

    @After
    fun tearDown() {
        archivosTemporales.forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        archivosTemporales.clear()
    }

    @Test
    fun muestraAdjuntosYPermiteQuitar() {
        val imagen = crearImagenTemporal("foto_temporal.jpg")
        val adjuntoImagen = AdjuntoBorrador(
            nombreArchivo = "foto_evento.jpg",
            rutaLocal = imagen.absolutePath,
            tamanoBytes = 2048L,
            tipo = TipoAdjunto.IMAGEN,
            mimeType = "image/jpeg"
        )
        val adjuntoDoc = AdjuntoBorrador(
            nombreArchivo = "programa.pdf",
            rutaLocal = imagen.absolutePath,
            tamanoBytes = 4096L,
            tipo = TipoAdjunto.DOCUMENTO,
            mimeType = "application/pdf"
        )
        var quitado: AdjuntoBorrador? = null

        composeTestRule.setContent {
            DuocTheme {
                AdjuntosSection(
                    adjuntos = listOf(adjuntoImagen, adjuntoDoc),
                    alAgregar = {},
                    alQuitar = { quitado = it }
                )
            }
        }

        composeTestRule.onNodeWithText("foto_evento.jpg").assertIsDisplayed()
        composeTestRule.onNodeWithText("programa.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 KB").assertIsDisplayed()
        composeTestRule.onNodeWithText("4 KB").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("Quitar")[0].performClick()
        assertEquals(adjuntoImagen, quitado)
    }

    @Test
    fun alAlcanzarMaximoAdjuntos_muestraMensajeDeErrorAlIntentarAgregar() {
        val archivo = crearImagenTemporal("doc_temporal.jpg")
        val listaLlena = List(MAX_ADJUNTOS) { idx ->
            AdjuntoBorrador(
                nombreArchivo = "archivo_$idx.pdf",
                rutaLocal = archivo.absolutePath,
                tamanoBytes = 1024L,
                tipo = TipoAdjunto.DOCUMENTO,
                mimeType = "application/pdf"
            )
        }

        composeTestRule.setContent {
            DuocTheme {
                AdjuntosSection(
                    adjuntos = listaLlena,
                    alAgregar = {},
                    alQuitar = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Tomar foto").performClick()
        composeTestRule.onNodeWithText("Máximo $MAX_ADJUNTOS archivos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Galería").assertIsDisplayed()
        composeTestRule.onNodeWithText("Archivo PDF / Word").assertIsDisplayed()
    }
}