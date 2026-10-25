package com.validador_actividades_duoc.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.validador_actividades_duoc.MainActivity
import com.validador_actividades_duoc.R

object NotificacionesHelper {

    private const val CANAL_ID = "solicitudes"

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    private fun crearCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_ID,
            "Solicitudes de actividades",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos sobre la validación y confirmación de actividades"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    /**
     * Muestra una notificación. Devuelve false si no se pudo mostrar
     * (permiso denegado o notificaciones desactivadas), para no darla por "enviada".
     */
    @SuppressLint("MissingPermission") // el permiso se comprueba arriba con tienePermiso()
    fun mostrar(context: Context, id: Int, titulo: String, texto: String): Boolean {
        if (!tienePermiso(context)) return false
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false

        crearCanal(context)

        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notificacion)
        return true
    }
}