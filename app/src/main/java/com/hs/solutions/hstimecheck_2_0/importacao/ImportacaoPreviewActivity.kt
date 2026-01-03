package com.hs.solutions.hstimecheck_2_0.importacao

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
class ImportacaoPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultado = intent.getSerializableExtra("resultado")
                as? ImportacaoResultado

        if (resultado == null) {
            Toast.makeText(
                this,
                "Importação ainda não está disponível.\nEm breve estará funcionando.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        setContent {
            ImportacaoPreviewScreen(
                resultado = resultado,
                onConfirmar = {
                    setResult(Activity.RESULT_OK)
                    finish()
                },
                onCancelar = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportacaoPreviewScreen(
    resultado: ImportacaoResultado,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pré-visualização da Importação") })
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onCancelar) {
                    Text("Cancelar")
                }
                Button(
                    onClick = onConfirmar,
                    enabled = resultado.produtosValidos.isNotEmpty()
                ) {
                    Text("Importar")
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            item {
                Text(
                    "Produtos válidos (${resultado.produtosValidos.size})",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }

            items(resultado.produtosValidos) {
                Text(
                    "• ${it.codigoInterno} - ${it.descricao}",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            if (resultado.erros.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Erros encontrados (${resultado.erros.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                items(resultado.erros) {
                    Text(
                        "Linha ${it.linha}: ${it.motivo}",
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}
