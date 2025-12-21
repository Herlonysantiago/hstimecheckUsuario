package com.hs.solutions.hstimecheck_2_0.models

/**
 * Representa uma alteração em um campo do produto.
 * Ex: Preço: 12,90 -> 9,99
 */
data class AlteracaoCampo(
    val campo: String,
    val valorAnterior: String?,
    val valorNovo: String?
)

