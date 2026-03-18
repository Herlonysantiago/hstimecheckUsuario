package com.hs.solutions.hstimecheck_2_0.models

import java.util.UUID

data class ValidadeItem(
    var id: String = UUID.randomUUID().toString(),
    var validade: String = "",
    var quantidade: Int? = null,
    var dataCadastro: String = ""
)