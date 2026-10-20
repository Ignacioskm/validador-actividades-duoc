package com.validador_actividades_duoc.domain.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class BloqueHorario(
    val indice: Int,
    val inicio: LocalTime,
    val fin: LocalTime
) {
    val etiqueta: String get() = "$inicio a $fin"
}

// Los 13 bloques oficiales (tomados del prototipo)
val BLOQUES: List<BloqueHorario> = listOf(
    BloqueHorario(1,  LocalTime.of(9, 31),  LocalTime.of(10, 10)),
    BloqueHorario(2,  LocalTime.of(10, 11), LocalTime.of(10, 50)),
    BloqueHorario(3,  LocalTime.of(11, 1),  LocalTime.of(11, 40)),
    BloqueHorario(4,  LocalTime.of(11, 41), LocalTime.of(12, 20)),
    BloqueHorario(5,  LocalTime.of(12, 31), LocalTime.of(13, 10)),
    BloqueHorario(6,  LocalTime.of(13, 11), LocalTime.of(13, 50)),
    BloqueHorario(7,  LocalTime.of(14, 1),  LocalTime.of(14, 40)),
    BloqueHorario(8,  LocalTime.of(14, 41), LocalTime.of(15, 20)),
    BloqueHorario(9,  LocalTime.of(15, 31), LocalTime.of(16, 10)),
    BloqueHorario(10, LocalTime.of(16, 11), LocalTime.of(16, 50)),
    BloqueHorario(11, LocalTime.of(17, 1),  LocalTime.of(17, 40)),
    BloqueHorario(12, LocalTime.of(17, 41), LocalTime.of(18, 20)),
    BloqueHorario(13, LocalTime.of(18, 21), LocalTime.of(19, 0))
)

const val ANTICIPACION_IDEAL_DIAS = 14L
const val ANTICIPACION_MINIMA_HORAS = 48L   // CONFIRMAR con el cliente: ¿mínimo o máximo?

enum class ResultadoAnticipacion { OK, ADVERTENCIA, BLOQUEADA }

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