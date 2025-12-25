package com.hs.solutions.hstimecheck_2_0.trabalhando_preco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.hs.solutions.hstimecheck_2_0.core.AppContainer

class TrabalhandoPrecoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)

        setContent {
            MaterialTheme {
                TrabalhandoPrecoScreen(
                    viewModel = TrabalhandoPrecoViewModel()
                )
            }
        }
    }
}
