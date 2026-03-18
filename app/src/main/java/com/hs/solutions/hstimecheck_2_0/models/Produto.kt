package com.hs.solutions.hstimecheck_2_0.models

import java.util.UUID

data class Produto(
    var id: String = UUID.randomUUID().toString(),
    var codigoBarras: String = "", // Valor padrão adicionado
    var codigoInterno: String? = null,
    var descricao: String = "",    // Valor padrão adicionado
    var validadeAtual: String? = null,
    var quantidadeAtual: Int? = null,
    var quantidadePorCaixa: Int? = null,

    var precoAtual: Double? = null,
    var status: StatusProduto = StatusProduto.NORMAL,
    var emVerificacaoEstoque: Boolean = false,
    var precoEmNegociacao: Boolean = false,

    var vendaDia: Int = 0,
    var fotoUrl: String? = null,
    var fotpLocal: String? = null,

    var validades: MutableList<ValidadeItem> = mutableListOf(),
    var historico: MutableList<HistoricoItem> = mutableListOf(),
    var importId: String? = null,

    var lastZapSent: Long? = null,
    var alertaAtivo: Boolean = true,
    var diasAntecedencia: Int = 7
)