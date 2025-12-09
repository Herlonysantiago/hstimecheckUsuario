package com.hs.solutions.hstimecheck

import com.hs.solutions.hstimecheck.ui.SplashScreen
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hs.solutions.hstimecheck.ui.TelaPrincipalActivity
import androidx.lifecycle.ViewModelProvider
import com.hs.solutions.hstimecheck.core.ProductRepositoryImpl
import com.hs.solutions.hstimecheck.core.ProductService
import com.hs.solutions.hstimecheck.ui.ProductViewModel
import com.hs.solutions.hstimecheck.ui.theme.HsTimeCheckTheme
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
