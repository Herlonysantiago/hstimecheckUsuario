package com.hs.solutions.hstimecheck_2_0.models

data class HistoricoItem(

    // 🔹 Identificação do produto (snapshot)
    val produtoId: String,
    val codigoInterno: String?,
    val codigoBarras: String?,
    val descricaoProduto: String,

    // 🔹 Contexto da validade
    val validade: String?,

    // 🔹 Evento
    val tipoEvento: TipoEventoHistorico,
    val titulo: String,
    val descricao: String,
    val dataEvento: String,

    // 🔹 Estoque - CAIXA
    val estoqueCxAnterior: Int? = null,
    val estoqueCxAtual: Int? = null,

    // 🔹 Estoque - UNIDADE
    val estoqueUnAnterior: Int? = null,
    val estoqueUnAtual: Int? = null,

    // 🔹 Estoque total (somente se aplicável)
    val estoqueTotalAnterior: Int? = null,
    val estoqueTotalAtual: Int? = null,

    // 🔹 Preço
    val precoAnterior: Double? = null,
    val precoAtual: Double? = null,
    val precoSugerido: Double? = null,
    val precoAprovado: Double? = null,

    // 🔹 Auditoria
    val observacao: String? = null
)
