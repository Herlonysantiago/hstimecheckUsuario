package com.hs.solutions.hstimecheck_2_0.core

import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.hs.solutions.hstimecheck_2_0.estoque.*
class ProductService(private val repo: ProductRepository) {
    private val TAG_STATUS = "STATUS_DEBUG"
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

        android.util.Log.d(
            TAG_STATUS,
            "SERVICE ENTRADA → id=${produto.id} status=${produto.status}"
        )

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

        val statusFinal = if (novo) {
            StatusRules.aplicarRegraSanitaria(base)
        } else {
            base.status   // 🔴 NÃO ALTERA STATUS NA EDIÇÃO
        }

        android.util.Log.d(
            TAG_STATUS,
            "SERVICE SAÍDA → id=${base.id} status=$statusFinal"
        )

        repo.salvar(
            base.copy(status = statusFinal)
        )

        carregar()
    }


    suspend fun removerValidade(produto: Produto) {

        val historico = HistoryService.validadeRemovida(produto)

        val atualizado = produto.copy(
            validadeAtual = null,
            status = StatusProduto.NORMAL,
            historico = (produto.historico + historico).toMutableList()
        )

        repo.salvar(atualizado)
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
            produto = produto,
            precoSugerido = produto.precoAtual ?: 0.0,
            precoAprovado = precoAprovado
        )

        val atualizado = produto.copy(
            precoAtual = precoAprovado,
            status = StatusProduto.TRABALHANDO_PRECO,
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

        val historicoItem = when (novo) {

            StatusProduto.AGUARDANDO_APROVACAO ->
                HistoryService.envioAprovacao(
                    produto = produto,
                    validade = produto.validadeAtual,
                    precoAtual = produto.precoAtual ?: 0.0,
                    precoSugerido = produto.precoAtual ?: 0.0
                )

            StatusProduto.TRABALHANDO_PRECO ->
                HistoryService.envioQueimaPreco(produto)

            StatusProduto.VERIFICACAO_ESTOQUE ->
                HistoryService.envioVerificacaoEstoque(produto)

            else -> null
        }

        val atualizado = when (novo) {

            // 🔹 NÃO muda status — apenas marca flag
            StatusProduto.VERIFICACAO_ESTOQUE ->
                produto.copy(
                    emVerificacaoEstoque = true,
                    historico = if (historicoItem != null)
                        (produto.historico + historicoItem).toMutableList()
                    else
                        produto.historico
                )

            // 🔹 Todos os outros mudam status normalmente
            else ->
                produto.copy(
                    status = novo,
                    historico = if (historicoItem != null)
                        (produto.historico + historicoItem).toMutableList()
                    else
                        produto.historico
                )
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

    suspend fun atualizarQuantidade(
        produto: Produto,
        novaQuantidade: Int
    ) {
        val historico = HistoryService.verificacaoEstoque(
            produto = produto,
            quantidadeAnterior = produto.quantidadeAtual ?: 0,
            quantidadeNova = novaQuantidade
        )

        val atualizado = produto.copy(
            quantidadeAtual = novaQuantidade,
            historico = (produto.historico + historico).toMutableList()
        )

        repo.salvar(atualizado)
        carregar()
    }

    suspend fun marcarPrecoEmNegociacao(produto: Produto, motivo: String? = null) {

        val historico = HistoryService.precoEmNegociacao(
            produto = produto,
            motivo = motivo
        )

        val atualizado = produto.copy(
            precoEmNegociacao = true,
            historico = (produto.historico + historico).toMutableList()
        )

        repo.salvar(atualizado)
        carregar()
    }

    suspend fun registrarVenda(
        produtoId: String,
        quantidadeVendida: Int,
        validadeSelecionada: String? = null
    ) {
        val produtoOriginal = produtos.value.find { it.id == produtoId } ?: return
        if (quantidadeVendida <= 0) return

        // 🔹 Cópia profunda (Compose-safe)
        val produto = produtoOriginal.copy(
            validades = produtoOriginal.validades.map { it.copy() }.toMutableList(),
            historico = produtoOriginal.historico.toMutableList()
        )

        // 🔹 FIFO de validades
        val validadesOrdenadas = produto.validades
            .filter { (it.quantidade ?: 0) > 0 }
            .sortedBy { it.validade }

        val validade = validadeSelecionada?.let { data ->
            validadesOrdenadas.find { it.validade == data }
        } ?: validadesOrdenadas.firstOrNull()
        ?: return

        val estoqueAtual = validade.quantidade ?: 0
        if (estoqueAtual < quantidadeVendida) {
            throw IllegalStateException("Estoque insuficiente")
        }

        val estoqueAntes = estoqueAtual

        // 🔻 Desconto
        validade.quantidade = estoqueAtual - quantidadeVendida

        // 🔄 Recalcula total do produto
        produto.quantidadeAtual =
            produto.validades.sumOf { it.quantidade ?: 0 }

        // 🔄 Atualiza validade atual
        produto.validadeAtual = produto.validades
            .filter { (it.quantidade ?: 0) > 0 }
            .minByOrNull { it.validade }
            ?.validade

        // 📝 Histórico
        produto.historico.add(
            HistoryService.venda(
                produto = produto,
                validade = validade.validade,
                estoqueUnAntes = estoqueAntes,
                estoqueUnDepois = validade.quantidade ?: 0
            )
        )

        // 💾 Persiste
        repo.salvar(produto)

        // 🔴 EMITE NOVA LISTA (CHAVE DO PROBLEMA)
        _produtos.value = _produtos.value.map {
            if (it.id == produto.id) produto else it
        }
    }

}
