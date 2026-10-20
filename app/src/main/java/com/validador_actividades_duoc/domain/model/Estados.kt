package com.validador_actividades_duoc.domain.model

enum class EstadoReserva {
    PENDIENTE, //<- Esta es para las areas cuando estan validando
    PENDIENTE_ADMIN, //<- Esta para cuando el admin tenga que validar
    CONFIRMADA,
    RECHAZADA }
enum class EstadoRequerimiento { PENDIENTE, APROBADO, RECHAZADO }