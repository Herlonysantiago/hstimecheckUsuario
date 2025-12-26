package com.hs.solutions.hstimecheck_2_0.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

const val CANAL_ALERTAS = "alertas_produtos"

fun criarCanaisNotificacao(context: Context) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val canal = NotificationChannel(
            CANAL_ALERTAS,
            "Alertas de Produtos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas de validade e aprovação comercial"
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        manager.createNotificationChannel(canal)
    }
}
