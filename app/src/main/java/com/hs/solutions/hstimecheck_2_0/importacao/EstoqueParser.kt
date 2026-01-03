package com.hs.solutions.hstimecheck_2_0.importacao

data class EstoqueParseResult(
    val quantidadeAtual: Int,
    val quantidadePorCaixa: Int?
)

sealed class EstoqueParseError(val motivo: String) {
    object FormatoInvalido : EstoqueParseError("Formato de estoque inválido")
    object TipoNaoSuportado : EstoqueParseError("Tipo de embalagem não suportado")
    object QuantidadeInvalida : EstoqueParseError("Quantidade inválida")
}

fun parseEstoque(valorRaw: String): Result<EstoqueParseResult> {

    val valor = valorRaw.trim().uppercase()

    // 🔹 Caso misto: "1 CXA + 6 UND" → considerar somente CX
    if (valor.contains("+") && valor.contains("CX")) {
        val cxRegex = Regex("""(\d+)\s*(CXA|CX)""")
        val match = cxRegex.find(valor)
            ?: return Result.failure(Exception(EstoqueParseError.FormatoInvalido.motivo))

        val cx = match.groupValues[1].toIntOrNull()
            ?: return Result.failure(Exception(EstoqueParseError.QuantidadeInvalida.motivo))

        if (cx <= 0)
            return Result.failure(Exception(EstoqueParseError.QuantidadeInvalida.motivo))

        return Result.success(
            EstoqueParseResult(
                quantidadeAtual = cx,
                quantidadePorCaixa = -1
            )
        )
    }

    // 🔹 Formato simples
    val regex = Regex("""^\s*(\d+)\s*(CXA|CX|UND|UN|KG|G)\s*$""")
    val match = regex.find(valor)
        ?: return Result.failure(Exception(EstoqueParseError.FormatoInvalido.motivo))

    val quantidade = match.groupValues[1].toIntOrNull()
        ?: return Result.failure(Exception(EstoqueParseError.QuantidadeInvalida.motivo))

    if (quantidade <= 0)
        return Result.failure(Exception(EstoqueParseError.QuantidadeInvalida.motivo))

    return when (val tipo = match.groupValues[2]) {

        "CXA", "CX" -> Result.success(
            EstoqueParseResult(
                quantidadeAtual = quantidade,
                quantidadePorCaixa = -1
            )
        )

        "UND", "UN" -> Result.success(
            EstoqueParseResult(
                quantidadeAtual = quantidade,
                quantidadePorCaixa = null
            )
        )

        "KG" -> Result.success(
            EstoqueParseResult(
                quantidadeAtual = quantidade * 1000,
                quantidadePorCaixa = 1000
            )
        )

        "G" -> Result.success(
            EstoqueParseResult(
                quantidadeAtual = quantidade,
                quantidadePorCaixa = 1000
            )
        )

        else -> Result.failure(Exception(EstoqueParseError.TipoNaoSuportado.motivo))
    }
}

