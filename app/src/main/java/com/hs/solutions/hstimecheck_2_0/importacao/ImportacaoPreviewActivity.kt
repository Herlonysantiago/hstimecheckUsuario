package com.hs.solutions.hstimecheck_2_0.importacao

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api

class ImportacaoPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultado = ImportacaoHolder.resultado
            ?: run {
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
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onCancelar) {
                    Text("Cancelar")
                }
                Button(
                    onClick = onConfirmar,
                    enabled = resultado.erroFatal == null && resultado.novos.isNotEmpty()
                ) {
                    Text("Importar")
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
        ) {

            // 🔴 ERRO FATAL
            resultado.erroFatal?.let {
                item {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
                return@LazyColumn
            }

            // ✅ NOVOS
            item {
                Text(
                    "Novos produtos (${resultado.novos.size})",
                    fontWeight = FontWeight.Bold
                )
            }

            items(resultado.novos) { produto ->
                Text("• ${produto.codigoInterno ?: "—"} - ${produto.descricao}")
            }

            // ⚠️ DUPLICADOS
            if (resultado.duplicados.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Duplicados (${resultado.duplicados.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }

                items(resultado.duplicados) { produto ->
                    Text(
                        "• ${produto.codigoInterno ?: "—"} - ${produto.descricao}",
                        color = Color(0xFFFF9800)
                    )
                }
            }

            // ❌ ERROS
            if (resultado.erros.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Erros encontrados (${resultado.erros.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }

                items(resultado.erros) { erro ->
                    Text("• $erro", color = Color.Red)
                }
            }
        }
    }
}
