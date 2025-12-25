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
            "Validade: ${produto.validadeAtual ?: "—"}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
