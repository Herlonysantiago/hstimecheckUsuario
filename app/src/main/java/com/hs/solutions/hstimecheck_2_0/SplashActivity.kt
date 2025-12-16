package com.hs.solutions.hstimecheck_2_0

import com.hs.solutions.hstimecheck_2_0.ui.SplashScreen
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hs.solutions.hstimecheck_2_0.ui.TelaPrincipalActivity
import com.hs.solutions.hstimecheck_2_0.ui.theme.HsTimeCheckTheme
import kotlinx.coroutines.*

class SplashActivity : ComponentActivity() {

    private val splashScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apenas exibe a SplashScreen
        setContent {
            HsTimeCheckTheme {
                SplashScreen()
            }
        }

        // Aguarda e abre a Tela Principal
        splashScope.launch {
            delay(1200)

            startActivity(Intent(this@SplashActivity, TelaPrincipalActivity::class.java))
            finish() // remove a splash
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        splashScope.cancel()
    }
}
