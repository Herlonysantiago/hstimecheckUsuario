package com.hs.solutions.hstimecheck_2_0.core

import com.hs.solutions.hstimecheck_2_0.models.HistoricoItem
import java.text.SimpleDateFormat
import java.util.*

object HistoryService {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun registrar(
        evento: String,
        detalhe: String? = null,
        quantidade: Int? = null,
        preco: Double? = null
    ): HistoricoItem {
        return HistoricoItem(
            dataEvento = sdf.format(Date()),
            evento = evento,
            detalhe = detalhe,
            quantidade = quantidade,
            preco = preco
        )
    }
}
