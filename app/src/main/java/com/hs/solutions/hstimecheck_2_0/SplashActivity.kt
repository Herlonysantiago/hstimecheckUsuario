package com.hs.solutions.hstimecheck_2_0

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hs.solutions.hstimecheck_2_0.alerts.agendarAlertas
import com.hs.solutions.hstimecheck_2_0.auth.AuthSession
import com.hs.solutions.hstimecheck_2_0.auth.SignInActivity
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.ui.SplashScreen
import com.hs.solutions.hstimecheck_2_0.ui.TelaPrincipalActivity
import com.hs.solutions.hstimecheck_2_0.ui.theme.HsTimeCheckTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {

    private val splashScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HsTimeCheckTheme {
                SplashScreen()
            }
        }

        splashScope.launch {
            delay(1200)

            if (AuthSession.isSignedIn()) {
                AppContainer.init(applicationContext)
                agendarAlertas(applicationContext)
                startActivity(Intent(this@SplashActivity, TelaPrincipalActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, SignInActivity::class.java))
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        splashScope.cancel()
    }
}
