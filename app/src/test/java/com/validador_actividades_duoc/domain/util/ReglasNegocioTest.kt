package com.validador_actividades_duoc.domain.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReglasNegocioTest {

    @Test
    fun `esCorreoValido acepta los tres dominios institucionales de Duoc`() {
        assertTrue(esCorreoValido("estudiante@duocuc.cl"))
        assertTrue(esCorreoValido("administrativo@duoc.cl"))
        assertTrue(esCorreoValido("docente@profesor.duoc.cl"))
    }

    @Test
    fun `esCorreoValido ignora mayusculas y espacios alrededor`() {
        assertTrue(esCorreoValido("   ALUMNO.PRUEBA@DUOCUC.CL  "))
    }

    @Test
    fun `esCorreoValido rechaza correos externos o mal formados`() {
        assertFalse(esCorreoValido("usuario@gmail.com"))
        assertFalse(esCorreoValido("usuario@hotmail.com"))
        assertFalse(esCorreoValido("correo_sin_arroba.cl"))
        assertFalse(esCorreoValido(""))
    }
}