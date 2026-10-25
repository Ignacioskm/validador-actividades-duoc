package com.validador_actividades_duoc.domain.model

const val MAX_ADJUNTOS = 20
const val MAX_BYTES_ADJUNTO = 5L * 1024 * 1024   // 5 MB por archivo (regla del cliente)

enum class TipoAdjunto { IMAGEN, DOCUMENTO }

data class AdjuntoBorrador(
    val tipo: TipoAdjunto,
    val nombreArchivo: String,
    val rutaLocal: String,
    val mimeType: String,
    val tamanoBytes: Long
)
