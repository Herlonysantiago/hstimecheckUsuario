package com.hs.solutions.hstimecheck_2_0.core

import com.hs.solutions.hstimecheck_2_0.models.Produto

interface ProductRepository {

    suspend fun carregar(): List<Produto>

    suspend fun salvarTodos(produtos: List<Produto>)

    suspend fun salvar(produto: Produto)

    suspend fun remover(id: String)
}