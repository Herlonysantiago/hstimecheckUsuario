package com.hs.solutions.hstimecheck_2_0.models

enum class StatusProduto {
    NORMAL,
    VENCENDO,
    VENCIDO,                // ← NOVO STATUS
    AGUARDANDO_APROVACAO,
    TRABALHANDO_PRECO,
    VERIFICACAO_ESTOQUE
}

