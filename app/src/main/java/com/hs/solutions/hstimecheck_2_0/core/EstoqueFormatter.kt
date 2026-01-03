package com.hs.solutions.hstimecheck_2_0.core

import com.hs.solutions.hstimecheck_2_0.core.DateFormatter
import com.hs.solutions.hstimecheck_2_0.models.Produto

fun formatarProdutoParaEnvio(produto: Produto): String {
    val total = produto.quantidadeAtual ?: 0
    val qpc = produto.quantidadePorCaixa

    val estoque = when {
        qpc == -1 -> "$total CX"
        qpc != null && qpc > 0 -> {
            val cx = total / qpc
            val un = total % qpc
            if (un > 0) "$cx CX + $un UND" else "$cx CX"
        }
        else -> "$total UND"
    }

    return """
        ${produto.descricao}
        CB: ${produto.codigoBarras}
        CI: ${produto.codigoInterno ?: "-"}
        Estoque: $estoque
        Validade: ${DateFormatter.isoParaBr(produto.validadeAtual)}

    """.trimIndent()
}
fun montarMensagemEnvio(produtos: List<Produto>): String {
    return produtos.joinToString("\n\n") {
        formatarProdutoParaEnvio(it)
    }
}
