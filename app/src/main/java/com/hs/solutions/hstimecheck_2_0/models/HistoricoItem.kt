package com.hs.solutions.hstimecheck_2_0.models

data class HistoricoItem(
    var produtoId: String = "",
    var codigoInterno: String? = null,
    var codigoBarras: String? = null,
    var descricaoProduto: String = "",
    var validade: String? = null,
    var tipoEvento: TipoEventoHistorico = TipoEventoHistorico.CADASTRO_PRODUTO,
    var titulo: String = "",
    var descricao: String = "",
    var dataEvento: String = "",
    var estoqueCxAnterior: Int? = null,
    var estoqueCxAtual: Int? = null,
    var estoqueUnAnterior: Int? = null,
    var estoqueUnAtual: Int? = null,
    var estoqueTotalAnterior: Int? = null,
    var estoqueTotalAtual: Int? = null,
    var precoAnterior: Double? = null,
    var precoAtual: Double? = null,
    var precoSugerido: Double? = null,
    var precoAprovado: Double? = null,
    var observacao: String? = null
)