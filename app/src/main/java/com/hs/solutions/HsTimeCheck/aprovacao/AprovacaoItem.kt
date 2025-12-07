package com.hs.solutions.hstimecheck.aprovacao

data class AprovacaoItem(
    val id: String,
    val descricao: String,
    val codigo: String,
    val precoAtual: Double,
    val precoSugerido: Double
) {
    val diferencaPercentual: Int
        get() {
            if (precoAtual == 0.0) return 0
            return (((precoSugerido - precoAtual) / precoAtual) * 100).toInt()
        }
}
