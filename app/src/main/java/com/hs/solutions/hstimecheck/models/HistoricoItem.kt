package com.hs.solutions.hstimecheck.models

data class HistoricoItem(
    val dataEvento: String,
    val evento: String,
    val detalhe: String? = null,
    val quantidade: Int? = null,
    val preco: Double? = null
)
