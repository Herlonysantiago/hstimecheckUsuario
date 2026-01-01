package com.hs.solutions.hstimecheck_2_0.ui.verificacaoqualidade

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.hs.solutions.hstimecheck_2_0.scanner.ScannerActivity
import androidx.activity.compose.setContent

class VerificacaoQualidadeProdutoActivity : ComponentActivity() {

    private val viewModel: VerificacaoQualidadeProdutoViewModel by viewModels {
        VerificacaoQualidadeProdutoViewModelFactory()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val codigoBarrasInicial = intent.getStringExtra("codigo_barras")
        val codigoInternoInicial = intent.getStringExtra("codigo_interno")

        setContent {

            val context = LocalContext.current

            // ✅ launcher CORRETO (dentro do Compose)
            val scannerLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val codigo = result.data
                        ?.getStringExtra("codigo_barras")
                        ?: return@rememberLauncherForActivityResult

                    viewModel.carregar(codigo, null)
                }
            }

            VerificacaoQualidadeProdutoScreen(
                codigoBarras = codigoBarrasInicial,
                codigoInterno = codigoInternoInicial,
                viewModel = viewModel,
                onBack = { finish() },
                onAbrirScanner = {
                    scannerLauncher.launch(
                        Intent(context, ScannerActivity::class.java)
                    )
                }
            )
        }
    }
}
