package com.hs.solutions.hstimecheck_2_0.alerts

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.hs.solutions.hstimecheck_2_0.vencendo.ProdutosVencendoActivity
import com.hs.solutions.hstimecheck_2_0.core.StatusRules
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
class AlertaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!AppContainer.isInitialized) return Result.success()

        val productService = AppContainer.productService
        productService.carregar()

        val produtos = productService.produtos.value

        val vencidos = produtos.filter {
            StatusRules.aplicarRegraSanitaria(it) == StatusProduto.VENCIDO
        }

        val vencendo = produtos.filter {
            StatusRules.aplicarRegraSanitaria(it) == StatusProduto.VENCENDO
        }


        if (vencidos.isNotEmpty()) {
            notificar(
                titulo = "Produtos vencidos",
                texto = "${vencidos.size} produto(s) vencidos.",
                modo = "VENCIDOS"
            )
        }

        if (vencendo.isNotEmpty()) {
            notificar(
                titulo = "Produtos vencendo",
                texto = "${vencendo.size} produto(s) próximos do vencimento.",
                modo = "VENCENDO"
            )
        }

        return Result.success()
    }


    private fun notificar(
        titulo: String,
        texto: String,
        modo: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(applicationContext, ProdutosVencendoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("modo", modo) // "VENCIDOS" ou "VENCENDO"
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            modo.hashCode(), // requestCode diferente por tipo
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CANAL_ALERTAS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // evita erro de ícone
            .setContentTitle(titulo)
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // 🔥 CLIQUE FUNCIONA
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext)
                .notify(modo.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission can be revoked after scheduling the worker.
        }
    }


}
