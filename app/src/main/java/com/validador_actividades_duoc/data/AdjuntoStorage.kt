package com.validador_actividades_duoc.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.validador_actividades_duoc.domain.model.AdjuntoBorrador
import com.validador_actividades_duoc.domain.model.MAX_BYTES_ADJUNTO
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import java.io.File
import java.util.UUID

object AdjuntoStorage {

    /** PDF y Word (.doc y .docx). */
    val MIME_DOCUMENTOS = arrayOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    private fun carpeta(context: Context) =
        File(context.filesDir, "adjuntos").apply { mkdirs() }

    /** Copia el archivo elegido a la carpeta de la app. Llamar desde Dispatchers.IO. */
    fun copiarDesdeUri(context: Context, uri: Uri, tipo: TipoAdjunto): Result<AdjuntoBorrador> = runCatching {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "application/octet-stream"

        when (tipo) {
            TipoAdjunto.DOCUMENTO -> require(mime in MIME_DOCUMENTOS) { "Solo se permiten archivos PDF o Word" }
            TipoAdjunto.IMAGEN -> require(mime.startsWith("image/")) { "El archivo no es una imagen" }
        }

        // Si el sistema informa el tamaño, se rechaza antes de copiar
        val tamanoInformado = tamanoDe(resolver, uri)
        if (tamanoInformado != null && tamanoInformado > MAX_BYTES_ADJUNTO) error("El archivo supera los 5 MB")

        val nombre = nombreDe(resolver, uri) ?: "archivo"
        val seguro = nombre.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val destino = File(carpeta(context), "${UUID.randomUUID()}_$seguro")

        val entrada = resolver.openInputStream(uri) ?: error("No se pudo leer el archivo")
        entrada.use { input -> destino.outputStream().use { salida -> input.copyTo(salida) } }

        if (destino.length() > MAX_BYTES_ADJUNTO) {
            destino.delete()
            error("El archivo supera los 5 MB")
        }
        AdjuntoBorrador(tipo, nombre, destino.absolutePath, mime, destino.length())
    }

    fun eliminarArchivo(ruta: String) {
        runCatching { File(ruta).delete() }
    }

    private fun nombreDe(resolver: ContentResolver, uri: Uri): String? =
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (indice >= 0 && cursor.moveToFirst()) cursor.getString(indice) else null
        }

    private fun tamanoDe(resolver: ContentResolver, uri: Uri): Long? =
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val indice = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (indice >= 0 && cursor.moveToFirst() && !cursor.isNull(indice)) cursor.getLong(indice) else null
        }
}