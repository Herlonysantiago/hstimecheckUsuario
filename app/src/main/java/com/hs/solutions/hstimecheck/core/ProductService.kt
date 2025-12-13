package com.hs.solutions.hstimecheck.core
import android.util.Log
import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductService(private val repo: ProductRepository) {

    private val _produtos = MutableStateFlow<List<Produto>>(emptyList())
    val produtos: StateFlow<List<Produto>> = _produtos.asStateFlow()

    suspend fun carregar() {
        _produtos.value = repo.carregar()
    }

    suspend fun inserirOuAtualizar(produto: Produto) {
        Log.e("SERVICE", "Inserindo/atualizando produto: ${produto.id}")

        repo.salvar(produto)
        Log.e("SERVICE", "Inserindo/atualizando produto: ${produto.id}")

        carregar()
    }
    suspend fun aprovarComercial(
        produto: Produto,
        precoAprovado: Double,
        observacao: String? = null
    ) {
        val atualizado = produto.copy(
            precoAtual = precoAprovado,
            status = StatusProduto.NORMAL
        )

        atualizado.historico.add(
            HistoryService.registrar(
                evento = "Aprovação comercial",
                detalhe = observacao ?: "Preço aprovado: R$ $precoAprovado",
                preco = precoAprovado
            )
        )

        repo.salvar(atualizado)
        carregar()
    }

    suspend fun rejeitarComercial(
        produto: Produto,
        motivo: String? = null
    ) {
        val atualizado = produto.copy(
            status = StatusProduto.NORMAL
        )

        atualizado.historico.add(
            HistoryService.registrar(
                evento = "Rejeição comercial",
                detalhe = motivo ?: "Preço não aprovado"
            )
        )

        repo.salvar(atualizado)
        carregar()
    }

    suspend fun remover(id: String) {
        repo.remover(id)
        carregar()
    }

    suspend fun mudarStatus(produto: Produto, novo: StatusProduto) {
        val atualizado = produto.copy(status = novo)
        repo.salvar(atualizado)
        carregar()
    }

    fun getProdutoById(id: String): Produto? {
        return _produtos.value.find { it.id == id }
    }
}
