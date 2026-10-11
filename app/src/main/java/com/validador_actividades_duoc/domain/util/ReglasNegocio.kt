package com.validador_actividades_duoc.domain.util

// Lista de dominios institucionales permitidos
val DOMINIOS_PERMITIDOS = listOf("duoc.cl", "duocuc.cl", "profesor.duoc.cl")

/**
 * Función pura para verificar si un correo pertenece a un dominio permitido arriba
 */
fun esCorreoValido(correo: String): Boolean {
    val correoLimpio = correo.trim().lowercase()
    val dominio = correoLimpio.substringAfter('@', missingDelimiterValue = "")
    return DOMINIOS_PERMITIDOS.contains(dominio)
}

