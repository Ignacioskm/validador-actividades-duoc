package com.validador_actividades_duoc.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.validador_actividades_duoc.data.AdjuntoStorage
import com.validador_actividades_duoc.domain.model.AdjuntoBorrador
import com.validador_actividades_duoc.domain.model.MAX_ADJUNTOS
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
fun AdjuntosSection(
    adjuntos: List<AdjuntoBorrador>,
    alAgregar: (AdjuntoBorrador) -> Unit,
    alQuitar: (AdjuntoBorrador) -> Unit,
    modificador: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var error by remember { mutableStateOf<String?>(null) }

    // Conserva la ruta si Android recrea la pantalla
    // mientras la aplicación de cámara está abierta.
    var fotoTemporal by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    fun recibir(resultado: Result<AdjuntoBorrador>) {
        resultado
            .onSuccess {
                error = null
                alAgregar(it)
            }
            .onFailure {
                error = it.message ?: "No se pudo adjuntar el archivo"
            }
    }

    // ==========================================
    // 1. SELECCIONAR FOTO DESDE GALERÍA
    // ==========================================

    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            if (adjuntos.size >= MAX_ADJUNTOS) {
                error = "Máximo $MAX_ADJUNTOS archivos"
            } else {
                scope.launch {
                    recibir(
                        withContext(Dispatchers.IO) {
                            AdjuntoStorage.copiarDesdeUri(
                                context,
                                uri,
                                TipoAdjunto.IMAGEN
                            )
                        }
                    )
                }
            }
        }
    }

    // ==========================================
    // 2. SELECCIONAR DOCUMENTOS PDF / WORD
    // ==========================================

    val archivos = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        scope.launch {
            var total = adjuntos.size

            for (uri in uris) {
                if (total >= MAX_ADJUNTOS) {
                    error = "Máximo $MAX_ADJUNTOS archivos"
                    break
                }

                val resultado = withContext(Dispatchers.IO) {
                    AdjuntoStorage.copiarDesdeUri(
                        context,
                        uri,
                        TipoAdjunto.DOCUMENTO
                    )
                }

                if (resultado.isSuccess) total++
                recibir(resultado)
            }
        }
    }

    // ==========================================
    // 3. TOMAR FOTO CON LA CÁMARA
    // ==========================================

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { fotoTomada ->

        val ruta = fotoTemporal
        fotoTemporal = null

        if (ruta != null) {
            val archivoTemporal = File(ruta)

            if (fotoTomada && archivoTemporal.exists()) {
                scope.launch {
                    try {
                        val resultado = withContext(Dispatchers.IO) {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                archivoTemporal
                            )

                            AdjuntoStorage.copiarDesdeUri(
                                context,
                                uri,
                                TipoAdjunto.IMAGEN
                            )
                        }

                        recibir(resultado)
                    } finally {
                        withContext(Dispatchers.IO) {
                            archivoTemporal.delete()
                        }
                    }
                }
            } else {
                // Si cancela la captura, eliminamos
                // el archivo temporal.
                archivoTemporal.delete()
            }
        }
    }

    // ==========================================
    // 4. FUNCIÓN PARA ABRIR LA CÁMARA
    // ==========================================

    fun abrirCamara() {
        if (adjuntos.size >= MAX_ADJUNTOS) {
            error = "Máximo $MAX_ADJUNTOS archivos"
            return
        }

        try {
            error = null

            val carpeta = File(context.cacheDir, "camara")
            carpeta.mkdirs()

            val archivo = File(
                carpeta,
                "foto_${UUID.randomUUID()}.jpg"
            )

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )

            fotoTemporal = archivo.absolutePath

            camara.launch(uri)

        } catch (e: Exception) {
            fotoTemporal?.let { File(it).delete() }
            fotoTemporal = null
            error = "No se pudo abrir la cámara: ${e.message}"
        }
    }

    // ==========================================
    // 5. INTERFAZ DEL FORMULARIO
    // ==========================================

    Column(
        modifier = modificador,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            "Documentos de la actividad (opcional)",
            style = MaterialTheme.typography.labelLarge
        )

        Text(
            "Adjunta fotografías, programas, pautas o documentos PDF/Word. " +
                    "Máximo 5 MB por archivo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Botones de fotografía
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { abrirCamara() }
            ) {
                Text("Tomar foto")
            }

            OutlinedButton(
                onClick = {
                    galeria.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts
                                .PickVisualMedia.ImageOnly
                        )
                    )
                }
            ) {
                Text("Galería")
            }
        }

        // Botón de documentos
        OutlinedButton(
            onClick = {
                archivos.launch(AdjuntoStorage.MIME_DOCUMENTOS)
            }
        ) {
            Text("Archivo PDF / Word")
        }

        // Mensajes de error
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Lista de archivos adjuntados
        adjuntos.forEach { adjunto ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (adjunto.tipo == TipoAdjunto.IMAGEN) {
                    MiniaturaAdjunto(adjunto.rutaLocal)
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        adjunto.nombreArchivo,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )

                    Text(
                        "${adjunto.tamanoBytes / 1024} KB",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(
                    onClick = { alQuitar(adjunto) }
                ) {
                    Text("Quitar")
                }
            }
        }
    }
}

