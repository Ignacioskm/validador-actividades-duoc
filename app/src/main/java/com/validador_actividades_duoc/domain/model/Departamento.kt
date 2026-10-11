package com.validador_actividades_duoc.domain.model

//Aca van todos los enums en departamente irian : Digitales, Auxiliares , comunicación , porteria etc.

enum class Departamento(val etiqueta: String) {
    AUXILIARES("Servicios Auxiliares"),
    PORTERIA("Portería y Accesos"),
    DIGITALES("Servicios Digitales"),
    COMUNICACION("Comunicaciones")
}