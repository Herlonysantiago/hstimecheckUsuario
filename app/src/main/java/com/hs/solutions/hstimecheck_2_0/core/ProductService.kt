package com.hs.solutions.hstimecheck_2_0.core

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

    fun buscarPorCodigoBarrasLocal(codigo: String): Produto? =
        _produtos.value.firstOrNull { it.codigoBarras == codigo }

    fun buscarPorCodigoInternoLocal(codigo: String): Produto? =
        _produtos.value.firstOrNull { it.codigoInterno == codigo }

    // =========================
    // INSERIR
    // =========================
    suspend fun inserirOuAtualizar(produto: Produto) {

        if (existeDuplicidade(produto)) {
            throw IllegalStateException(
                "Já existe um produto com o mesmo código e a mesma validade."
            )
        }

        val novo = repo.carregar().none { it.id == produto.id }
        val base = produto.copy()

        // 🔹 HISTÓRICO APENAS NO CADASTRO
        if (novo) {
            base.historico.add(
                HistoryService.cadastro(base)
            )
        }

        val statusAjustado = StatusRules.aplicarRegraSanitaria(base)

        repo.salvar(
            base.copy(status = statusAjustado)
        )
        carregar()
    }

    // =========================
    // APROVAÇÃO COMERCIAL
    // =========================
    suspend fun aprovarComercial(
        produto: Produto,
        precoAprovado: Double
    ) {
        val itemHistorico = HistoryService.aprovacao(
            produto = produto, // ainda com preço antigo
            precoSugerido = produto.precoAtual ?: 0.0,
            precoAprovado = precoAprovado
        )

        val atualizado = produto.copy(
            precoAtual = precoAprovado,
            status = StatusProduto.NORMAL,
            historico = (produto.historico + itemHistorico).toMutableList()
        )

        repo.salvar(atualizado)
        carregar()
    }


    // =========================
    // REJEIÇÃO COMERCIAL
    // =========================
    suspend fun rejeitarComercial(
        produto: Produto,
        motivo: String? = null
    ) {
        val historico = HistoryService.rejeicao(
            produto = produto,
            validade = produto.validadeAtual,
            motivo = motivo
        )

        val atualizado = produto.copy(
            status = StatusProduto.NORMAL,
            historico = (produto.historico + historico).toMutableList()

        )

        repo.salvar(atualizado)
        carregar()
    }

    // =========================
    // REMOÇÃO
    // =========================
    suspend fun remover(id: String) {
        repo.remover(id)
        carregar()
    }

    suspend fun mudarStatus(produto: Produto, novo: StatusProduto) {

        val atualizado = when (novo) {
            StatusProduto.VERIFICACAO_ESTOQUE ->
                produto.copy(emVerificacaoEstoque = true)

            else ->
                produto.copy(status = novo)
        }

        repo.salvar(atualizado)
        carregar()
    }

    fun getProdutoById(id: String): Produto? =
        _produtos.value.find { it.id == id }

    // =========================
    // REGRA DE DUPLICIDADE
    // =========================
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
