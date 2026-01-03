package com.hs.solutions.hstimecheck_2_0.importacao

import android.content.Context
import android.net.Uri
import com.hs.solutions.hstimecheck_2_0.core.DateFormatter
import com.hs.solutions.hstimecheck_2_0.models.Produto
import java.util.UUID

fun importarPlanilhaRebaixaCsv(
    context: Context,
    uri: Uri,
    produtoExistentePorCodigo: (String) -> Produto?
): ImportacaoResultado {

    val produtosValidos = mutableListOf<Produto>()
    val erros = mutableListOf<ImportacaoErro>()

    val linhas = context.contentResolver
        .openInputStream(uri)
        ?.bufferedReader()
        ?.readLines()
        ?: return ImportacaoResultado(emptyList(), listOf(
            ImportacaoErro(0, "Não foi possível ler o arquivo", "")
        ))

    if (linhas.isEmpty())
        return ImportacaoResultado(emptyList(), listOf(
            ImportacaoErro(0, "Arquivo vazio", "")
        ))

    // 🔴 CABEÇALHO ESPERADO
    val esperado = listOf(
        "CÓDIGO",
        "DIG",
        "DESCRIÇÃO",
        "EMBALAGEM",
        "COMPRADOR",
        "QTDE CX",
        "PREÇO ATUAL",
        "SUGESTÃO",
        "DATA VENCIMENTO"
    )

    val header = linhas.first().split(";").map { it.trim().uppercase() }

    if (!header.containsAll(esperado)) {
        return ImportacaoResultado(
            emptyList(),
            listOf(
                ImportacaoErro(
                    1,
                    "Cabeçalho incompatível com Planilha de Rebaixas",
                    linhas.first()
                )
            )
        )
    }

    linhas.drop(1).forEachIndexed { index, linha ->

        val numeroLinha = index + 2
        val col = linha.split(";")

        try {
            val codigo = col[0].trim()
            val descricao = col[2].trim()
            val embalagem = col[3].trim()
            val sugestaoRaw = col[7].trim()
            val validadeRaw = col[8].trim()

            if (codigo.isBlank()) {
                erros += ImportacaoErro(numeroLinha, "Código vazio", linha)
                return@forEachIndexed
            }

            val produtoBase = produtoExistentePorCodigo(codigo)
                ?: run {
                    erros += ImportacaoErro(numeroLinha, "Produto não existe no sistema", linha)
                    return@forEachIndexed
                }

            val validadeIso = DateFormatter.brParaIso(validadeRaw)
                ?: run {
                    erros += ImportacaoErro(numeroLinha, "Validade inválida", linha)
                    return@forEachIndexed
                }

            if (produtoBase.validades.any { it.validade == validadeIso }) {
                // ignora duplicidade
                return@forEachIndexed
            }

            val estoque = parseEstoque(embalagem)
                .getOrElse {
                    erros += ImportacaoErro(numeroLinha, it.message ?: "Erro no estoque", linha)
                    return@forEachIndexed
                }

            val precoSugerido = sugestaoRaw
                .replace(",", ".")
                .toDoubleOrNull()
                ?: run {
                    erros += ImportacaoErro(numeroLinha, "Preço sugerido inválido", linha)
                    return@forEachIndexed
                }

            val produtoNovo = produtoBase.copy(
                id = UUID.randomUUID().toString(),
                validadeAtual = validadeIso,
                quantidadeAtual = estoque.quantidadeAtual,
                quantidadePorCaixa = estoque.quantidadePorCaixa,
                precoAtual = precoSugerido
            )

            produtosValidos += produtoNovo

        } catch (e: Exception) {
            erros += ImportacaoErro(
                numeroLinha,
                "Erro inesperado: ${e.message}",
                linha
            )
        }
    }

    return ImportacaoResultado(produtosValidos, erros)
}
