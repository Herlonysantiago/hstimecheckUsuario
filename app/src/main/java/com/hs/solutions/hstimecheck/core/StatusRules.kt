package com.hs.solutions.hstimecheck.core

import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object StatusRules {

    var diasVencendo = 7  // configurável

    fun aplicarRegraSanitaria(produto: Produto): StatusProduto {
        val validade = produto.validadeAtual ?: return produto.status

        val dias = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(validade))

        return when {
            dias < 0 -> StatusProduto.VENCENDO // vencido (visual sanitário)
            dias == 0L -> StatusProduto.VENCENDO
            dias in 1..diasVencendo -> StatusProduto.VENCENDO
            else -> produto.status
        }
    }
}
