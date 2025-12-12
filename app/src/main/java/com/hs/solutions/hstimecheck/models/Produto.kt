package com.hs.solutions.hstimecheck.models

import java.util.UUID

data class Produto(
    val id: String = UUID.randomUUID().toString(),
    val codigoBarras: String,
    var codigoInterno: String? = null,
    var descricao: String,
    var validadeAtual: String? = null,
    var quantidadeAtual: Int? = null,
    var quantidadePorCaixa: Int? = null,

    var precoAtual: Double? = null,
    var status: StatusProduto = StatusProduto.NORMAL,

    // ➜ Campo adicionado conforme sua escolha (OPÇÃO B)
    var vendaDia: Int = 0,
    var fotoUrl: String? = null,
    var fotpLocal: String?= null,
    val validades: MutableList<ValidadeItem> = mutableListOf(),
    val historico: MutableList<HistoricoItem> = mutableListOf()
)
