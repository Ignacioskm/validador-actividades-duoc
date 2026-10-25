package com.validador_actividades_duoc.ui.components

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.validador_actividades_duoc.data.AdjuntoStorage
import com.validador_actividades_duoc.domain.model.AdjuntoBorrador
import com.validador_actividades_duoc.domain.model.MAX_ADJUNTOS
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun recibir(resultado: Result<AdjuntoBorrador>) {
        resultado
            .onSuccess { error = null; alAgregar(it) }
            .onFailure { error = it.message ?: "No se pudo adjuntar el archivo" }
    }

    // Galería: selector de fotos del sistema (no necesita permisos)
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        if (adjuntos.size >= MAX_ADJUNTOS) {
            error = "Máximo $MAX_ADJUNTOS archivos"
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            recibir(withContext(Dispatchers.IO) {
                AdjuntoStorage.copiarDesdeUri(context, uri, TipoAdjunto.IMAGEN)
            })
        }
    }

    // Archivos PDF / Word: selector de documentos del sistema (no necesita permisos)
    val archivos = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        scope.launch {
            var total = adjuntos.size
            for (uri in uris) {
                if (total >= MAX_ADJUNTOS) {
                    error = "Máximo $MAX_ADJUNTOS archivos"
                    break
                }
                val resultado = withContext(Dispatchers.IO) {
                    AdjuntoStorage.copiarDesdeUri(context, uri, TipoAdjunto.DOCUMENTO)
                }
                if (resultado.isSuccess) total++
                recibir(resultado)
            }
        }
    }

    Column(modifier = modificador, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Documentos de la actividad (opcional)",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            "Programa, pauta, libreto o layout en PDF o Word, o una imagen (máx. 5 MB por archivo).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Galería") }
            OutlinedButton(onClick = { archivos.launch(AdjuntoStorage.MIME_DOCUMENTOS) }) { Text("Archivo PDF/Word") }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        adjuntos.forEach { adjunto ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (adjunto.tipo == TipoAdjunto.IMAGEN) MiniaturaAdjunto(adjunto.rutaLocal)
                Column(modifier = Modifier.weight(1f)) {
                    Text(adjunto.nombreArchivo, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    Text("${adjunto.tamanoBytes / 1024} KB", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { alQuitar(adjunto) }) { Text("Quitar") }
            }
        }
    }
}