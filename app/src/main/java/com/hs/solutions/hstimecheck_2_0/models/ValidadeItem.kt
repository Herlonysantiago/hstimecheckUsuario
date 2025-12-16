package com.hs.solutions.hstimecheck_2_0.models

import java.util.UUID

data class ValidadeItem(
    val id: String = UUID.randomUUID().toString(),
    val validade: String,
    var quantidade: Int? = null,
    val dataCadastro: String
)
