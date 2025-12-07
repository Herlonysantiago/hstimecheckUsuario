package com.hs.solutions.Hstimecheck

import com.hs.solutions.Hstimecheck.models.Produto

object MainActivityGlobalRepo {

    private val produtos: MutableList<Produto> = mutableListOf()

    fun setProdutos(lista: List<Produto>) {
        produtos.clear()
        produtos.addAll(lista)
    }

    fun getProdutos(): List<Produto> = produtos

    fun getProdutoById(id: String?): Produto? {
        if (id == null) return null
        return produtos.find { it.id == id }
    }
}
