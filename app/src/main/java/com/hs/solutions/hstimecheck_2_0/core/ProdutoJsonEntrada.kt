package com.hs.solutions.hstimecheck_2_0.core

data class ProdutoJsonEntrada(
    val codigoBarras: String, //codigo de barras
    val codigo: Long?,        // código interno
    val bar_cod: Long?,       // código de barras
    val descricao: String?,   // descrição normal (sem acento)
    val complemento: String?, // subtítulo / complemento
    val comprador: String?    // opcional

)
fun ProdutoJsonEntrada.getCodigoBarras(): String? {
    return when {
        !codigoBarras.isNullOrBlank() -> codigoBarras
        bar_cod != null -> bar_cod.toString()
        else -> null
    }
}


