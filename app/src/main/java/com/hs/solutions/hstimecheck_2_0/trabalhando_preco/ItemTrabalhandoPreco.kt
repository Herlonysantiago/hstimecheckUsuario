package com.hs.solutions.hstimecheck_2_0.trabalhando_preco

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.core.DateFormatter


 fun estoqueTexto(produto: Produto): String {
    val total = produto.quantidadeAtual ?: return "—"
    val qpc = produto.quantidadePorCaixa

    return when {
        qpc == -1 -> "$total cx"
        qpc == null || qpc <= 0 -> "$total un"
        else -> {
            val cx = total / qpc
            val un = total % qpc
            if (un > 0) "$cx cx • $un un" else "$cx cx"
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemTrabalhandoPreco(
    produto: Produto,
    selecionado: Boolean,
    modoSelecao: Boolean,
    onSelecionar: () -> Unit,
    onAbrirMenu: () -> Unit
) {
    val fundo = if (selecionado) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(fundo)
            .combinedClickable(
                onClick = {
                    if (modoSelecao) onSelecionar() else onAbrirMenu()
                },
                onLongClick = onSelecionar
            )
            .padding(12.dp)
    ) {
        Text("CB: ${produto.codigoBarras ?: "—"}", style = MaterialTheme.typography.bodyMedium)

        Text(
            "CI: ${produto.codigoInterno ?: "—"} - ${produto.descricao}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            "Validade: ${DateFormatter.isoParaBr(produto.validadeAtual)}",
            style = MaterialTheme.typography.bodySmall
        )


        Spacer(Modifier.height(4.dp))

        Text(
            "Estoque: ${estoqueTexto(produto)}",
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            "Preço: ${
                produto.precoAtual?.let { "R$ %.2f".format(it) } ?: "—"
            }",
            style = MaterialTheme.typography.bodySmall
        )
    }



}
