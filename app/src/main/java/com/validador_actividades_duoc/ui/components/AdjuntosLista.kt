package com.validador_actividades_duoc.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.validador_actividades_duoc.data.local.entity.AdjuntoEntity
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun MiniaturaAdjunto(ruta: String, modificador: Modifier = Modifier) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, ruta) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                // inSampleSize = 4 evita cargar la imagen completa en memoria
                val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
                BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
            }.getOrNull()
        }
    }
    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = "Imagen adjunta",
            contentScale = ContentScale.Crop,
            modifier = modificador.size(56.dp)
        )
    }
}

@Composable
fun AdjuntosLista(adjuntos: List<AdjuntoEntity>, modificador: Modifier = Modifier) {
    if (adjuntos.isEmpty()) return
    val context = LocalContext.current

    Column(modifier = modificador, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Documentos adjuntos:", style = MaterialTheme.typography.labelLarge)
        adjuntos.forEach { adjunto ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (adjunto.tipo == TipoAdjunto.IMAGEN) MiniaturaAdjunto(adjunto.rutaLocal)
                Text(
                    text = adjunto.nombreArchivo,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = { abrirAdjunto(context, adjunto) }) { Text("Abrir") }
            }
        }
    }
}

fun abrirAdjunto(context: Context, adjunto: AdjuntoEntity) {
    val archivo = File(adjunto.rutaLocal)
    if (!archivo.exists()) {
        Toast.makeText(context, "El archivo ya no está disponible", Toast.LENGTH_SHORT).show()
        return
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, adjunto.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay una app instalada para abrir este archivo", Toast.LENGTH_SHORT).show()
    }
}
