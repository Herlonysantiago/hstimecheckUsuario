package com.hs.solutions.hstimecheck_2_0.importacao

import com.hs.solutions.hstimecheck_2_0.models.Produto
import java.io.Serializable

data class ImportacaoErro(
    val linha: Int,
    val motivo: String,
    val conteudo: String
) : Serializable

data class ImportacaoResultado(
    val produtosValidos: List<Produto>,
    val erros: List<ImportacaoErro>
) : Serializable
