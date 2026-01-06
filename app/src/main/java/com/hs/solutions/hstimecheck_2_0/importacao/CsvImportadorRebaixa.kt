package com.hs.solutions.hstimecheck_2_0.importacao

import android.content.Context
import android.net.Uri
import com.hs.solutions.hstimecheck_2_0.core.ProductService
import com.hs.solutions.hstimecheck_2_0.models.Produto
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

fun importarPlanilhaRebaixaCsv(
    context: Context,
    uri: Uri,
    importId: String,
    productService: ProductService
): ImportacaoResultado {

    val reader = BufferedReader(
        InputStreamReader(context.contentResolver.openInputStream(uri)!!)
    )

    val linhas = reader.readLines()
    reader.close()

    if (linhas.isEmpty()) {
        return ImportacaoResultado.erro("CSV vazio")
    }

    val esperado = listOf(
        "CODIGO",
        "DIG",
        "DESCRICAO",
        "EMBALAGEM",
        "COMPRADOR",
        "QTDE CX",
        "PRECO ATUAL",
        "SUGESTAO",
        "VENCIMENTO"
    )

    val cabecalho = linhas.first().split(";").map { it.trim().uppercase() }

    if (cabecalho != esperado) {
        return ImportacaoResultado.erro(
            "Cabeçalho inválido.\nEsperado: $esperado\nEncontrado: $cabecalho"
        )
    }

    val resultado = ImportacaoResultado()

    linhas.drop(1).forEachIndexed { index, linha ->

        val colunas = linha.split(";")

        if (colunas.size < esperado.size) {
            resultado.erros.add("Linha ${index + 2}: colunas insuficientes")
            return@forEachIndexed
        }

        val codigo = colunas[0].trim()
        val descricao = colunas[2].trim()
        val validade = colunas[8].trim()

        if (codigo.isBlank() || validade.isBlank()) {
            resultado.erros.add("Linha ${index + 2}: código ou validade vazios")
            return@forEachIndexed
        }

        val candidato = Produto(
            id = UUID.randomUUID().toString(),
            codigoInterno = codigo,
            codigoBarras = "",
            descricao = descricao,
            validadeAtual = validade,
            validades = mutableListOf(),
            historico = mutableListOf()
        )

        val duplicado = try {
            runBlocking {
                productService.existeDuplicidadeParaImportacao(candidato)
            }
        } catch (e: Exception) {
            resultado.erros.add("Linha ${index + 2}: erro ao validar duplicidade")
            return@forEachIndexed
        }

        if (duplicado) {
            resultado.duplicados += candidato
        } else {

            val produtoComImportId = candidato.copy(
                importId = importId
            )

            resultado.novos += produtoComImportId
        }

    }

    return resultado
}
