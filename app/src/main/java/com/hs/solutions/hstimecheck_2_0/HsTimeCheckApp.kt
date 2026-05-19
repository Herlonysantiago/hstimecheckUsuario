package com.hs.solutions.hstimecheck_2_0

import android.app.Application
import com.hs.solutions.hstimecheck_2_0.alerts.agendarAlertas
import com.hs.solutions.hstimecheck_2_0.alerts.criarCanaisNotificacao
import com.hs.solutions.hstimecheck_2_0.alerts.testarAlertaAgora
import com.hs.solutions.hstimecheck_2_0.auth.AuthSession
import com.hs.solutions.hstimecheck_2_0.core.AppContainer

class HsTimeCheckApp : Application() {

    override fun onCreate() {
        super.onCreate()

        criarCanaisNotificacao(this)

        if (AuthSession.isSignedIn()) {
            AppContainer.init(this)
            testarAlertaAgora(this)
            agendarAlertas(this)
        }
    }
}
