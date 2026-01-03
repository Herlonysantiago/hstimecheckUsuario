package com.hs.solutions.hstimecheck_2_0.alerts

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.work.OneTimeWorkRequestBuilder

fun testarAlertaAgora(context: Context) {

    val work = OneTimeWorkRequestBuilder<AlertaWorker>()
        .setInitialDelay(30, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context)
        .enqueue(work)
}

fun agendarAlertas(context: Context) {

    val work = PeriodicWorkRequestBuilder<AlertaWorker>(
        120, TimeUnit.MINUTES
    ).build()

    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "alertas_produtos",
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )


}
