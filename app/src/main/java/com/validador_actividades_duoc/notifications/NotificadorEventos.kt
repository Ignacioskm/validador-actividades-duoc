package com.validador_actividades_duoc.notifications

import android.content.Context
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.EstadoReserva
import com.validador_actividades_duoc.domain.model.RolUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificadorEventos(
    context: Context,
    private val reservaRepository: ReservaRepository
) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("notificaciones_enviadas", Context.MODE_PRIVATE)
    private var job: Job? = null

    /** Empieza a observar lo que le importa al usuario según su rol. */
    fun iniciar(scope: CoroutineScope, usuario: UsuarioEntity) {
        detener()
        job = scope.launch {
            when (usuario.rol) {

                // Validador: solicitudes pendientes que involucran a su área
                RolUsuario.VALIDADOR -> {
                    val departamento = usuario.departamento ?: return@launch
                    reservaRepository.solicitudesDeArea(departamento).collect { lista ->
                        lista.forEach { datos ->
                            avisarUnaVez(
                                clave = "u${usuario.id}:area:${datos.reserva.id}",
                                titulo = "Nueva solicitud para ${departamento.etiqueta}",
                                texto = "${datos.reserva.nombreActividad} · ${datos.solicitante.nombre}. " +
                                        "Revisa los recursos que te piden."
                            )
                        }
                    }
                }

                // Admin: todas las áreas ya respondieron
                RolUsuario.ADMIN -> {
                    reservaRepository.todas().collect { lista ->
                        lista.filter { it.reserva.estado == EstadoReserva.PENDIENTE_ADMIN }.forEach { datos ->
                            avisarUnaVez(
                                clave = "u${usuario.id}:admin:${datos.reserva.id}",
                                titulo = "Actividad lista para tu confirmación",
                                texto = "${datos.reserva.nombreActividad}: todas las áreas ya respondieron."
                            )
                        }
                    }
                }

                // Solicitante: su actividad fue confirmada o rechazada
                RolUsuario.SOLICITANTE -> {
                    reservaRepository.delSolicitante(usuario.id).collect { lista ->
                        lista.forEach { datos ->
                            val reserva = datos.reserva
                            when (reserva.estado) {
                                EstadoReserva.CONFIRMADA -> avisarUnaVez(
                                    clave = "u${usuario.id}:confirmada:${reserva.id}",
                                    titulo = "Actividad confirmada",
                                    texto = "\"${reserva.nombreActividad}\" fue confirmada por la administración."
                                )
                                EstadoReserva.RECHAZADA -> avisarUnaVez(
                                    clave = "u${usuario.id}:rechazada:${reserva.id}",
                                    titulo = "Actividad rechazada",
                                    texto = "\"${reserva.nombreActividad}\" fue rechazada." +
                                            (reserva.motivoRechazo?.let { " Motivo: $it" } ?: "")
                                )
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    fun detener() {
        job?.cancel()
        job = null
    }

    /** Muestra la notificación solo si no se envió antes y si realmente se pudo mostrar. */
    private fun avisarUnaVez(clave: String, titulo: String, texto: String) {
        if (prefs.getBoolean(clave, false)) return
        if (NotificacionesHelper.mostrar(app, clave.hashCode(), titulo, texto)) {
            prefs.edit().putBoolean(clave, true).apply()
        }
    }
}
