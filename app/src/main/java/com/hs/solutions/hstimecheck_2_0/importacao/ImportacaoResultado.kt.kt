package com.hs.solutions.hstimecheck_2_0.importacao

import com.hs.solutions.hstimecheck_2_0.models.Produto
import java.io.Serializable

class ImportacaoResultado(
    val novos: MutableList<Produto> = mutableListOf(),
    val duplicados: MutableList<Produto> = mutableListOf(),
    val erros: MutableList<String> = mutableListOf(),
    val erroFatal: String? = null
) : Serializable {

    companion object {
        fun erro(msg: String): ImportacaoResultado {
            return ImportacaoResultado(erroFatal = msg)
        }
    }
}
