package com.hs.solutions.hstimecheck_2_0.core

import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object StatusRules {

    var diasVencendo = 7 // configurável

    fun aplicarRegraSanitaria(produto: Produto): StatusProduto {
        val validadeStr = produto.validadeAtual ?: return produto.status

        return try {
            val validade = LocalDate.parse(validadeStr)
            val hoje = LocalDate.now()
            val dias = ChronoUnit.DAYS.between(hoje, validade)

            when {
                dias < 0 -> StatusProduto.VENCIDO
                dias == 0L -> StatusProduto.VENCENDO
                dias in 1..diasVencendo -> StatusProduto.VENCENDO
                else -> StatusProduto.NORMAL
            }
        } catch (_: Exception) {
            produto.status // fallback seguro
        }
    }
}

