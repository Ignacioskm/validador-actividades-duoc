package com.duocuc.reservas.domain.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

// Modelo para los bloques horarios
data class BloqueHorario(
    val indice: Int,
    val inicio: LocalTime,
    val fin: LocalTime
) {
    val etiqueta: String get() = "$inicio a $fin"
}

// 1. Los 13 bloques oficiales
val BLOQUES: List<BloqueHorario> = listOf(
    BloqueHorario(1,  LocalTime.of(9, 31),  LocalTime.of(10, 10)),
    BloqueHorario(2,  LocalTime.of(10, 11), LocalTime.of(10, 50)),
    BloqueHorario(3,  LocalTime.of(10, 51), LocalTime.of(11, 30)),
    BloqueHorario(4,  LocalTime.of(11, 31), LocalTime.of(12, 10)),
    BloqueHorario(5,  LocalTime.of(12, 11), LocalTime.of(12, 50)),
    BloqueHorario(6,  LocalTime.of(12, 51), LocalTime.of(13, 30)),
    BloqueHorario(7,  LocalTime.of(13, 31), LocalTime.of(14, 10)),
    BloqueHorario(8,  LocalTime.of(14, 11), LocalTime.of(14, 50)),
    BloqueHorario(9,  LocalTime.of(14, 51), LocalTime.of(15, 30)),
    BloqueHorario(10, LocalTime.of(15, 31), LocalTime.of(16, 10)),
    BloqueHorario(11, LocalTime.of(16, 11), LocalTime.of(16, 50)),
    BloqueHorario(12, LocalTime.of(16, 51), LocalTime.of(17, 30)),
    BloqueHorario(13, LocalTime.of(18, 21), LocalTime.of(19, 0))
)

// Constantes de anticipación y correos
const val ANTICIPACION_IDEAL_DIAS = 14L
const val ANTICIPACION_MINIMA_HORAS = 48L

// 2. Enum de resultado
enum class ResultadoAnticipacion { OK, ADVERTENCIA, BLOQUEADA }

/**
 * 3 y 4. Función pura para evaluar la anticipación de una reserva
 */
fun evaluarAnticipacion(
    fecha: LocalDate,
    bloque: BloqueHorario,
    ahora: LocalDateTime = LocalDateTime.now()
): ResultadoAnticipacion {
    val fechaHoraEvento = LocalDateTime.of(fecha, bloque.inicio)

    val horasDiferencia = ChronoUnit.HOURS.between(ahora, fechaHoraEvento)
    val diasDiferencia = ChronoUnit.DAYS.between(ahora.toLocalDate(), fecha)

    return when {
        horasDiferencia < ANTICIPACION_MINIMA_HORAS -> ResultadoAnticipacion.BLOQUEADA
        diasDiferencia >= ANTICIPACION_IDEAL_DIAS -> ResultadoAnticipacion.OK
        else -> ResultadoAnticipacion.ADVERTENCIA
    }
}