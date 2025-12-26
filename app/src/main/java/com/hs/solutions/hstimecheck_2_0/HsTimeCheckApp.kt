package com.hs.solutions.hstimecheck_2_0

import com.hs.solutions.hstimecheck_2_0.alerts.criarCanaisNotificacao
import android.app.Application
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.alerts.agendarAlertas
import com.hs.solutions.hstimecheck_2_0.alerts.testarAlertaAgora
class HsTimeCheckApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicialização global do app
        AppContainer.init(this)
        testarAlertaAgora(this)

        criarCanaisNotificacao(this)
        // Agenda os alertas (WorkManager)
        agendarAlertas(this)
    }
}
