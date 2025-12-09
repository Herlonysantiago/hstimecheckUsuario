package com.hs.solutions.hstimecheck.core

data class ProdutoJsonEntrada(
    val codigo: Long?,        // código interno
    val bar_cod: Long?,       // código de barras
    val descricao: String?,   // descrição normal (sem acento)
    val complemento: String?, // subtítulo / complemento
    val comprador: String?    // opcional
)
