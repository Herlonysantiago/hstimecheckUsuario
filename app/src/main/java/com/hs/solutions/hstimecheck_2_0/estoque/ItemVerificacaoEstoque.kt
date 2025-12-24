package com.hs.solutions.hstimecheck_2_0.estoque

import androidx.annotation.experimental.Experimental
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hs.solutions.hstimecheck_2_0.models.Produto
import androidx.compose.ui.text.font.FontWeight

// =======================================================
// ITEM DA LISTA (RESUMO)
// =======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemVerificacaoEstoqueResumo(
    produto: Produto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(12.dp)) {

            Text("CB: ${produto.codigoBarras}")

            Text(
                "CI: ${produto.codigoInterno ?: "—"} - ${produto.descricao}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                "Validade: ${produto.validadeAtual ?: "—"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// =======================================================
// DIALOG DE AJUSTE
// =======================================================

@Composable
fun DialogAjusteEstoque(
    produto: Produto,
    onSalvar: (Int) -> Unit,
    onCancelar: () -> Unit
) {
    val qtdPorCaixa = produto.quantidadePorCaixa ?: 0
    val estoqueAtual = produto.quantidadeAtual ?: 0

    var cxTexto by remember { mutableStateOf("") }
    var undTexto by remember { mutableStateOf("") }

    val totalCalculado = remember(cxTexto, undTexto) {
        val cx = cxTexto.toIntOrNull() ?: 0
        val und = undTexto.toIntOrNull() ?: 0
        (cx * qtdPorCaixa) + und
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            Button(
                enabled = totalCalculado >= 0,
                onClick = { onSalvar(totalCalculado) }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        },
        title = { Text("Ajuste de Estoque") },
        text = {
            Column {

                Text("Estoque atual: $estoqueAtual un")

                if (qtdPorCaixa > 0) {
                    Text(
                        "1 caixa = $qtdPorCaixa unidades",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = cxTexto,
                    onValueChange = { novo ->
                        if (novo.all { it.isDigit() } || novo.isEmpty()) {
                            cxTexto = novo
                        }
                    },
                    label = { Text("Caixas (CX)") },
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = undTexto,
                    onValueChange = { novo ->
                        if (novo.all { it.isDigit() } || novo.isEmpty()) {
                            undTexto = novo
                        }
                    },
                    label = { Text("Unidades (UND)") },
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Total calculado: $totalCalculado unidades",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
