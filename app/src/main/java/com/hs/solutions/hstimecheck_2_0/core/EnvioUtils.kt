package com.hs.solutions.hstimecheck_2_0.utils

import android.content.Context
import android.content.Intent
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.core.montarMensagemEnvio
fun enviarProdutos(context: Context, produtos: List<Produto>) {
    if (produtos.isEmpty()) return

    val mensagem = montarMensagemEnvio(produtos)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, mensagem)
    }

    context.startActivity(
        Intent.createChooser(intent, "Enviar produtos")
    )
}
