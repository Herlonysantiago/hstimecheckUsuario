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

    val texto = complemento.uppercase().trim()

    // 1. Mantém a sua regra de KG que você disse estar correta
    if (texto.contains("KG 1 X 1000 X 1G")) {
        return InfoCaixa(
            caixas = 1,
            unidadesPorCaixa = 1000, // Valor que você definiu como correto
            unidadeMedida = "G"
        )
    }

    // 2. Padrão genérico: Procura o número que está entre dois 'X'
    // Ex: "CXA 1 X 12 X 30" -> Vai capturar o "12"
    // O regex busca: um número, um X, o SEU VALOR (capturado), e outro X
    val regex = Regex("""\d+\s*[Xx]\s*(\d+)\s*[Xx]""")

    val match = regex.find(texto)

    return if (match != null) {
        val valorEntreX = match.groupValues[1].toInt()
        InfoCaixa(
            caixas = 1,
            unidadesPorCaixa = valorEntreX,
            unidadeMedida = null // Ignora unidade de medida conforme pedido
        )
    } else {
        null
    }
}

