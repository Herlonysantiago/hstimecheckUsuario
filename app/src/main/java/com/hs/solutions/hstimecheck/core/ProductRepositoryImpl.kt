package com.hs.solutions.hstimecheck.core

import com.hs.solutions.hstimecheck.models.Produto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class ProductRepositoryImpl : ProductRepository {

    private val produtos = ConcurrentHashMap<String, Produto>()

    override suspend fun carregar(): List<Produto> = withContext(Dispatchers.IO) {
        produtos.values.toList()
    }

    override suspend fun salvarTodos(produtosList: List<Produto>) = withContext(Dispatchers.IO) {
        produtosList.forEach { produto ->
            produtos[produto.id] = produto
        }
    }

    override suspend fun salvar(produto: Produto) = withContext(Dispatchers.IO) {
        produtos[produto.id] = produto
    }

    override suspend fun remover(id: String): Unit = withContext(Dispatchers.IO) {
        produtos.remove(id)
    }

}
