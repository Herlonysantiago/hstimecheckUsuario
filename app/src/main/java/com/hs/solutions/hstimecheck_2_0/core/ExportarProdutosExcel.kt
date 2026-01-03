package com.hs.solutions.hstimecheck_2_0.core

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import com.hs.solutions.hstimecheck_2_0.models.Produto



fun exportarProdutosCsv(
    context: Context,
    produtos: List<Produto>
): File {

    val file = File(
        context.getExternalFilesDir(null),
        "produtos_hs_timecheck.csv"
    )

    FileOutputStream(file).bufferedWriter().use { writer ->

        // Cabeçalho
        writer.appendLine(
            "Codigo Interno;Codigo Barras;Descricao;Validade;Estoque;Qtd por Caixa;Preco;Status"
        )

        produtos.forEach { p ->
            writer.appendLine(
                listOf(
                    p.codigoInterno ?: "",
                    p.codigoBarras ?: "",
                    p.descricao,
                    p.validadeAtual ?: "",
                    p.quantidadeAtual ?: 0,
                    p.quantidadePorCaixa ?: "",
                    p.precoAtual ?: "",
                    p.status.name
                ).joinToString(";")
            )
        }
    }

    return file
}
fun compartilharCsv(context: Context, file: File) {
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        android.content.Intent.createChooser(intent, "Exportar produtos")
    )
}

