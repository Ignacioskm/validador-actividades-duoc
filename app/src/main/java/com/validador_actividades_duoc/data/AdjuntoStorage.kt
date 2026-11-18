package com.validador_actividades_duoc.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import com.validador_actividades_duoc.domain.model.AdjuntoBorrador
import com.validador_actividades_duoc.domain.model.MAX_BYTES_ADJUNTO
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import java.io.File
import java.util.UUID

object AdjuntoStorage {

    val MIME_DOCUMENTOS = arrayOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    private fun carpeta(context: Context) =
        File(context.filesDir, "adjuntos").apply {
            mkdirs()
        }

    fun copiarDesdeUri(
        context: Context,
        uri: Uri,
        tipo: TipoAdjunto
    ): Result<AdjuntoBorrador> = runCatching {

        val resolver = context.contentResolver
        val mime = resolver.getType(uri)
            ?: "application/octet-stream"

        when (tipo) {
            TipoAdjunto.DOCUMENTO ->
                require(mime in MIME_DOCUMENTOS) {
                    "Solo se permiten archivos PDF o Word"
                }

            TipoAdjunto.IMAGEN ->
                require(mime.startsWith("image/")) {
                    "El archivo no es una imagen"
                }
        }

        val nombre = nombreDe(resolver, uri) ?: "archivo"
        val seguro = nombre.replace(
            Regex("[^A-Za-z0-9._-]"),
            "_"
        )

        // Guardamos primero en un archivo temporal
        val temporal = File.createTempFile(
            "adjunto_",
            ".tmp",
            context.cacheDir
        )

        var destino: File? = null

        try {
            resolver.openInputStream(uri)?.use { entrada ->
                temporal.outputStream().use { salida ->
                    entrada.copyTo(salida)
                }
            } ?: error("No se pudo leer el archivo")

            // Los documentos siguen teniendo límite de 5 MB
            if (tipo == TipoAdjunto.DOCUMENTO) {
                require(temporal.length() <= MAX_BYTES_ADJUNTO) {
                    "El documento supera los 5 MB"
                }
            }

            // Para imágenes grandes activamos compresión
            val comprimir = tipo == TipoAdjunto.IMAGEN &&
                    temporal.length() > MAX_BYTES_ADJUNTO

            val nombreFinal = if (comprimir) {
                "${seguro.substringBeforeLast('.', seguro)}.jpg"
            } else {
                seguro
            }

            val mimeFinal = if (comprimir) {
                "image/jpeg"
            } else {
                mime
            }

            destino = File(
                carpeta(context),
                "${UUID.randomUUID()}_$nombreFinal"
            )

            if (comprimir) {
                comprimirImagen(temporal, destino)
            } else {
                temporal.copyTo(destino, overwrite = true)
            }

            require(destino.length() <= MAX_BYTES_ADJUNTO) {
                "El archivo supera los 5 MB"
            }

            AdjuntoBorrador(
                tipo = tipo,
                nombreArchivo = nombreFinal,
                rutaLocal = destino.absolutePath,
                mimeType = mimeFinal,
                tamanoBytes = destino.length()
            )

        } catch (e: Exception) {
            destino?.delete()
            throw e

        } finally {
            // Siempre eliminamos la copia temporal
            temporal.delete()
        }
    }

    // ============================================
    // COMPRESIÓN AUTOMÁTICA DE FOTOGRAFÍAS
    // ============================================

    private fun comprimirImagen(
        origen: File,
        destino: File
    ) {
        val resoluciones = listOf(
            1920,
            1440,
            1080,
            800
        )

        val calidades = listOf(
            88,
            78,
            68,
            55,
            40
        )

        for (resolucion in resoluciones) {

            // Decodificamos limitando la resolución
            val fuente = ImageDecoder.createSource(origen)

            val bitmap = ImageDecoder.decodeBitmap(fuente) {
                    decoder, info, _ ->

                decoder.allocator =
                    ImageDecoder.ALLOCATOR_SOFTWARE

                val ancho = info.size.width
                val alto = info.size.height

                val escala = minOf(
                    1f,
                    resolucion.toFloat() /
                            maxOf(ancho, alto)
                )

                decoder.setTargetSize(
                    maxOf(1, (ancho * escala).toInt()),
                    maxOf(1, (alto * escala).toInt())
                )
            }

            // JPEG no admite transparencia.
            // Aplicamos fondo blanco si es necesario.
            val imagenJpeg = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Bitmap.Config.RGB_565
            )

            val canvas = Canvas(imagenJpeg)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            bitmap.recycle()

            try {
                for (calidad in calidades) {

                    destino.outputStream().use { salida ->
                        val correcto = imagenJpeg.compress(
                            Bitmap.CompressFormat.JPEG,
                            calidad,
                            salida
                        )

                        check(correcto) {
                            "No se pudo comprimir la imagen"
                        }
                    }

                    if (destino.length() <= MAX_BYTES_ADJUNTO) {
                        return
                    }
                }

            } finally {
                imagenJpeg.recycle()
            }
        }

        destino.delete()

        error(
            "No se pudo reducir la imagen a menos de 5 MB"
        )
    }

    // ============================================
    // ELIMINACIÓN DE ARCHIVOS
    // ============================================

    fun eliminarArchivo(ruta: String) {
        runCatching {
            File(ruta).delete()
        }
    }

    // ============================================
    // INFORMACIÓN DEL ARCHIVO
    // ============================================

    private fun nombreDe(
        resolver: ContentResolver,
        uri: Uri
    ): String? =
        resolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->

            val indice = cursor.getColumnIndex(
                OpenableColumns.DISPLAY_NAME
            )

            if (indice >= 0 && cursor.moveToFirst()) {
                cursor.getString(indice)
            } else {
                null
            }
        }

    private fun tamanoDe(
        resolver: ContentResolver,
        uri: Uri
    ): Long? =
        resolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->

            val indice = cursor.getColumnIndex(
                OpenableColumns.SIZE
            )

            if (
                indice >= 0 &&
                cursor.moveToFirst() &&
                !cursor.isNull(indice)
            ) {
                cursor.getLong(indice)
            } else {
                null
            }
        }
}
