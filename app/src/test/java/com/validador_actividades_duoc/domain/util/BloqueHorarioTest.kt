package com.validador_actividades_duoc.domain.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class BloqueHorarioTest {

    @Test
    fun `la lista oficial debe tener exactamente 13 bloques horarios`() {
        assertEquals(13, BLOQUES.size)
        assertEquals(1, BLOQUES.first().indice)
        assertEquals(13, BLOQUES.last().indice)
    }

    @Test
    fun `la etiqueta del bloque debe formatear inicio y fin correctamente`() {
        val bloque = BloqueHorario(1, LocalTime.of(9, 31), LocalTime.of(10, 10))
        assertEquals("09:31 a 10:10", bloque.etiqueta)
    }

    @Test
    fun `evaluarAnticipacion retorna BLOQUEADA cuando faltan menos de 48 horas`() {
        val ahora = LocalDateTime.of(2026, 11, 10, 10, 0)
        val fechaEvento = LocalDate.of(2026, 11, 11)
        val bloque = BLOQUES.first()

        val resultado = evaluarAnticipacion(fechaEvento, bloque, ahora)

        assertEquals(ResultadoAnticipacion.BLOQUEADA, resultado)
    }

    @Test
    fun `evaluarAnticipacion retorna ADVERTENCIA entre 48 horas y menos de 14 dias`() {
        val ahora = LocalDateTime.of(2026, 11, 10, 9, 0)
        val fechaEvento = LocalDate.of(2026, 11, 15)
        val bloque = BLOQUES.first()

        val resultado = evaluarAnticipacion(fechaEvento, bloque, ahora)

        assertEquals(ResultadoAnticipacion.ADVERTENCIA, resultado)
    }

    @Test
    fun `evaluarAnticipacion retorna OK cuando faltan 14 dias o mas`() {
        val ahora = LocalDateTime.of(2026, 11, 10, 9, 0)
        val fechaEvento = LocalDate.of(2026, 11, 24)
        val bloque = BLOQUES.first()

        val resultado = evaluarAnticipacion(fechaEvento, bloque, ahora)

        assertEquals(ResultadoAnticipacion.OK, resultado)
    }
}