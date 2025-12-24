package com.hs.solutions.hstimecheck_2_0.estoque
import androidx.compose.runtime.remember
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.estoque.*
import com.hs.solutions.hstimecheck_2_0.ui.*
import android.os.Bundle


class VerificacaoEstoqueActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)

        setContent {
            MaterialTheme {
                val viewModel = remember { VerificacaoEstoqueViewModel() }
                VerificacaoEstoqueScreen(viewModel)
            }
        }

    }
}

