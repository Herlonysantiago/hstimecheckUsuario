package com.hs.solutions.hstimecheck_2_0.core
import android.util.Log
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductService(private val repo: ProductRepository) {

    private val _produtos = MutableStateFlow<List<Produto>>(emptyList())
    val produtos: StateFlow<List<Produto>> = _produtos.asStateFlow()

    suspend fun carregar() {
        _produtos.value = repo.carregar()
    }
    fun buscarPorCodigoBarrasLocal(codigo: String): Produto? {
        return _produtos.value.firstOrNull {
            it.codigoBarras == codigo
        }
    }

    fun buscarPorCodigoInternoLocal(codigo: String): Produto? {
        return _produtos.value.firstOrNull {
            it.codigoInterno == codigo
        }
    }

    suspend fun inserirOuAtualizar(produto: Produto) {

        if (existeDuplicidade(produto)) {
            throw IllegalStateException(
                "Já existe um produto com o mesmo código e a mesma validade."
            )
        }

        repo.salvar(produto)
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
        val atualizado = produto.copy(status = StatusProduto.NORMAL)

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

    // ✅ REGRA DE DUPLICIDADE (ESTAVA FALTANDO)
    private suspend fun existeDuplicidade(novo: Produto): Boolean {

        val validade = novo.validadeAtual ?: return false

        val existentes = repo.carregar()

        return existentes.any { existente ->

            if (existente.id == novo.id) return@any false
            if (existente.validadeAtual != validade) return@any false

            val codigoBarrasIgual =
                !novo.codigoBarras.isNullOrBlank() &&
                        novo.codigoBarras == existente.codigoBarras

            val codigoInternoIgual =
                !novo.codigoInterno.isNullOrBlank() &&
                        novo.codigoInterno == existente.codigoInterno

            codigoBarrasIgual || codigoInternoIgual
        }
    }
}

