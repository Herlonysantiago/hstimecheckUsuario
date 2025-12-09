package com.hs.solutions.hstimecheck.core

import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductService(
    private val repository: ProductRepository
) {

    private val _produtos = MutableStateFlow<List<Produto>>(emptyList())
    val produtos: StateFlow<List<Produto>> = _produtos

    // -------------------------------------------------------------
    // Carrega dados do repositório e aplica regras sanitárias
    // -------------------------------------------------------------
    suspend fun carregar() {
        val lista = repository.carregar()
        val ajustada = lista.map { aplicarRegras(it) }
        _produtos.value = ajustada
    }

    // -------------------------------------------------------------
    // Busca um produto pelo ID
    // -------------------------------------------------------------
    fun getProdutoById(id: String?): Produto? {
        if (id == null) return null
        return _produtos.value.find { it.id == id }
    }

    // -------------------------------------------------------------
    // Adiciona ou atualiza produto
    // -------------------------------------------------------------
    suspend fun inserirOuAtualizar(produto: Produto) {
        val listaAtual = _produtos.value.toMutableList()

        val idx = listaAtual.indexOfFirst { it.id == produto.id }

        if (idx >= 0) {
            listaAtual[idx] = aplicarRegras(produto)
        } else {
            listaAtual.add(aplicarRegras(produto))
        }

        repository.salvar(produto)
        _produtos.value = listaAtual
    }

    // -------------------------------------------------------------
    // Muda o status manualmente (fluxos comerciais)
    // -------------------------------------------------------------
    suspend fun mudarStatus(produto: Produto, novoStatus: StatusProduto) {
        produto.status = novoStatus

        // histórico
        produto.historico.add(
            HistoryService.registrar(
                evento = "Status alterado",
                detalhe = "Novo status: $novoStatus",
                quantidade = produto.quantidadeAtual,
                preco = produto.precoAtual
            )
        )

        inserirOuAtualizar(produto)
    }

    // -------------------------------------------------------------
    // Remove um produto do sistema
    // -------------------------------------------------------------
    suspend fun removerProduto(id: String) {
        repository.remover(id)
        _produtos.value = _produtos.value.filterNot { it.id == id }
    }

    // -------------------------------------------------------------
    // Aplica status de validade (regras sanitárias)
    // -------------------------------------------------------------
    private fun aplicarRegras(produto: Produto): Produto {
        val statusSanitario = StatusRules.aplicarRegraSanitaria(produto)

        // STATUS SANITÁRIO SOBRESCREVE somente quando vencendo/vencido
        if (statusSanitario == StatusProduto.VENCENDO) {
            produto.status = StatusProduto.VENCENDO
        }

        return produto
    }
}
