package com.validador_actividades_duoc.data.local

import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.EstadoRequerimiento
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.RolUsuario
import com.validador_actividades_duoc.domain.model.TipoAdjunto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `convierte ida y vuelta todos los valores de RolUsuario y maneja null`() {
        RolUsuario.entries.forEach { rol ->
            val texto = converters.fromRolUsuario(rol)
            assertEquals(rol.name, texto)
            assertEquals(rol, converters.toRolUsuario(texto))
        }
        assertNull(converters.fromRolUsuario(null))
        assertNull(converters.toRolUsuario(null))
    }

    @Test
    fun `convierte ida y vuelta todos los valores de Departamento y maneja null`() {
        Departamento.entries.forEach { depto ->
            val texto = converters.fromDepartamento(depto)
            assertEquals(depto.name, texto)
            assertEquals(depto, converters.toDepartamento(texto))
        }
        assertNull(converters.fromDepartamento(null))
        assertNull(converters.toDepartamento(null))
    }

    @Test
    fun `convierte ida y vuelta todos los valores de EstadoReserva y maneja null`() {
        EstadoReserva.entries.forEach { estado ->
            val texto = converters.fromEstadoReserva(estado)
            assertEquals(estado.name, texto)
            assertEquals(estado, converters.toEstadoReserva(texto))
        }
        assertNull(converters.fromEstadoReserva(null))
        assertNull(converters.toEstadoReserva(null))
    }

    @Test
    fun `convierte ida y vuelta todos los valores de EstadoRequerimiento y maneja null`() {
        EstadoRequerimiento.entries.forEach { estado ->
            val texto = converters.fromEstadoRequerimiento(estado)
            assertEquals(estado.name, texto)
            assertEquals(estado, converters.toEstadoRequerimiento(texto))
        }
        assertNull(converters.fromEstadoRequerimiento(null))
        assertNull(converters.toEstadoRequerimiento(null))
    }

    @Test
    fun `convierte ida y vuelta todos los valores de TipoAdjunto y maneja null`() {
        TipoAdjunto.entries.forEach { tipo ->
            val texto = converters.fromTipoAdjunto(tipo)
            assertEquals(tipo.name, texto)
            assertEquals(tipo, converters.toTipoAdjunto(texto))
        }
        assertNull(converters.fromTipoAdjunto(null))
        assertNull(converters.toTipoAdjunto(null))
    }
}