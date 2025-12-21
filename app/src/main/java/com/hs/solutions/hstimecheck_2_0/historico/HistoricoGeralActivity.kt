package com.hs.solutions.hstimecheck_2_0.historico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.models.*
import com.hs.solutions.hstimecheck_2_0.historico.*
class HistoricoGeralActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)

        setContent {
            MaterialTheme {
                val vm: HistoricoViewModel = viewModel(
                    factory = HistoricoViewModelFactory(
                        AppContainer.productService
                    )
                )

                TelaHistoricoRobusto(vm)
            }
        }
    }

}
