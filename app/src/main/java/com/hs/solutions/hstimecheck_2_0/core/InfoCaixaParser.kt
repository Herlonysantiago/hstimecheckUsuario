package com.hs.solutions.hstimecheck_2_0.core

data class InfoCaixa(
    val caixas: Int,
    val unidadesPorCaixa: Int,
    val unidadeMedida: String?
) {
    val totalUnidades: Int
        get() = caixas * unidadesPorCaixa
}

fun extrairInfoCaixa(complemento: String?): InfoCaixa? {
    if (complemento.isNullOrBlank()) return null

    val texto = complemento.uppercase()

    // Exemplos aceitos:
    // CXA 2 X 6 X 50ML
    // CX 1 X 30 X 250G
    val regex = Regex(
        """CXA?\s*(\d+)\s*[Xx]\s*(\d+)\s*[Xx]\s*(\d+)\s*(ML|G|KG|L)"""
    )

    val match = regex.find(texto) ?: return null

    return InfoCaixa(
        caixas = match.groupValues[1].toInt(),
        unidadesPorCaixa = match.groupValues[2].toInt(),
        unidadeMedida = match.groupValues[4]
    )
}
