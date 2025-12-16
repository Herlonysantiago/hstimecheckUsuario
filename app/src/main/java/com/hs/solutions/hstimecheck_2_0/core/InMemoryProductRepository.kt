package com.hs.solutions.hstimecheck_2_0.core

import com.hs.solutions.hstimecheck_2_0.models.Produto

class InMemoryProductRepository : ProductRepository {

    private val lista = mutableListOf<Produto>()

    override suspend fun carregar(): List<Produto> = lista.toList()

    override suspend fun salvarTodos(produtos: List<Produto>) {
        lista.clear()
        lista.addAll(produtos)
    }

    override suspend fun salvar(produto: Produto) {
        val idx = lista.indexOfFirst { it.id == produto.id }
        if (idx >= 0) lista[idx] = produto else lista.add(produto)
    }

    override suspend fun remover(id: String) {
        lista.removeAll { it.id == id }
    }
}
