package com.hs.solutions.Hstimecheck.models

import java.util.UUID

data class Produto(
    val id: String = UUID.randomUUID().toString(),
    val codigoBarras: String,
    var codigoInterno: String? = null,
    var descricao: String,
    var validadeAtual: String? = null,
    var quantidadeAtual: Int? = null,
    var precoAtual: Double? = null,
    var status: StatusProduto = StatusProduto.NORMAL,
    val validades: MutableList<ValidadeItem> = mutableListOf(),
    val historico: MutableList<HistoricoItem> = mutableListOf()
)
