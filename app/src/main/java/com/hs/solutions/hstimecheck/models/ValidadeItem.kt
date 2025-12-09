package com.hs.solutions.hstimecheck.models

import java.util.UUID

data class ValidadeItem(
    val id: String = UUID.randomUUID().toString(),
    val validade: String,
    var quantidade: Int? = null,
    val dataCadastro: String
)
