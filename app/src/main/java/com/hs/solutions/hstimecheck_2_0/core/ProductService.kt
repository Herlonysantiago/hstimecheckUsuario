package com.hs.solutions.hstimecheck_2_0.core


import kotlinx.coroutines.launch
import android.util.Log
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.hs.solutions.hstimecheck_2_0.estoque.*
import java.util.UUID

class ProductService(private val repo: ProductRepository) {

    // 🔹 REPOSITORY DO FIREBASE ADICIONADO
    private val firebaseRepo = ProductRepositoryFirebase()

    private val TAG_STATUS = "STATUS_DEBUG"
    private val _produtos = MutableStateFlow<List<Produto>>(emptyList())
    val produtos: StateFlow<List<Produto>> = _produtos.asStateFlow()

    suspend fun carregar() {
        _produtos.value = repo.carregar()
    }
    init {
        // 👂 Inicia o "Ouvinte Realtime" do Firebase
        firebaseRepo.iniciarEscutaRemota { listaVindaDaNuvem ->
            // 1. Atualiza o StateFlow na memória (O que o usuário vê na tela agora)
            _produtos.value = listaVindaDaNuvem

            // 2. Sincronização Silenciosa com o Banco Local (Opcional, mas recomendado)
            // Isso garante que, se o celular ficar sem internet, as edições do PC já estejam salvas no SQLite
            kotlinx.coroutines.MainScope().launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    listaVindaDaNuvem.forEach { produtoNuvem ->
                        // Salva no Room/SQLite local para persistência offline
                        repo.salvar(produtoNuvem)
                    }
                    Log.d("fire_sync", "Banco local atualizado via PC: ${listaVindaDaNuvem.size} itens")
                } catch (e: Exception) {
                    Log.e("fire_sync", "Erro ao persistir dados do PC no local: ${e.message}")
                }
            }
        }
    }
    fun buscarPorCodigoBarrasLocal(codigo: String): Produto? =
        _produtos.value.firstOrNull { it.codigoBarras == codigo }

    fun buscarPorCodigoInternoLocal(codigo: String): Produto? {
        return _produtos.value.firstOrNull { it.codigoInterno == codigo }
    }

    // =========================
    // INSERIR OU ATUALIZAR
    // =========================
    suspend fun inserirOuAtualizar(produto: Produto) {
        if (existeDuplicidade(produto)) {
            throw IllegalStateException("Já existe um produto com o mesmo código e a mesma validade.")
        }

        val novo = repo.carregar().none { it.id == produto.id }
        val base = produto.copy()

        if (novo) {
            base.historico.add(HistoryService.cadastro(base))
        }

        val statusFinal = if (novo) StatusProduto.NORMAL else base.status
        val produtoFinal = base.copy(status = statusFinal)

        // ✅ SALVA LOCALMENTE
        repo.salvar(produtoFinal)

        // 🚀 SINCRONIZA NO FIREBASE
        firebaseRepo.salvarRemoto(produtoFinal)

        carregar()
    }

    // =========================
    // REMOÇÃO (CORRIGIDO)
    // =========================
    suspend fun remover(id: String) {
        // ✅ REMOVE LOCALMENTE
        repo.remover(id)

        // 🚀 REMOVE NO FIREBASE
        firebaseRepo.remover(id)

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
        firebaseRepo.salvarRemoto(atualizado) // 🚀 Sincroniza
        carregar()
    }

    // =========================
    // STATUS E NEGOCIAÇÕES
    // =========================
    suspend fun mudarStatus(produto: Produto, novo: StatusProduto) {
        val historicoItem = when (novo) {
            StatusProduto.AGUARDANDO_APROVACAO -> HistoryService.envioAprovacao(produto, produto.validadeAtual, produto.precoAtual ?: 0.0, produto.precoAtual ?: 0.0)
            StatusProduto.TRABALHANDO_PRECO -> HistoryService.envioQueimaPreco(produto)
            StatusProduto.VERIFICACAO_ESTOQUE -> HistoryService.envioVerificacaoEstoque(produto)
            else -> null
        }

        val atualizado = when (novo) {
            StatusProduto.VERIFICACAO_ESTOQUE -> produto.copy(
                emVerificacaoEstoque = true,
                historico = if (historicoItem != null) (produto.historico + historicoItem).toMutableList() else produto.historico
            )
            else -> produto.copy(
                status = novo,
                historico = if (historicoItem != null) (produto.historico + historicoItem).toMutableList() else produto.historico
            )
        }

        repo.salvar(atualizado)
        firebaseRepo.salvarRemoto(atualizado) // 🚀 Sincroniza
        carregar()
    }

    suspend fun aprovarComercial(produto: Produto, precoAprovado: Double) {
        val itemHistorico = HistoryService.aprovacao(produto, produto.precoAtual ?: 0.0, precoAprovado)
        val atualizado = produto.copy(
            precoAtual = precoAprovado,
            status = StatusProduto.TRABALHANDO_PRECO,
            historico = (produto.historico + itemHistorico).toMutableList()
        )
        repo.salvar(atualizado)
        firebaseRepo.salvarRemoto(atualizado) // 🚀 Sincroniza
        carregar()
    }

    suspend fun rejeitarComercial(produto: Produto, motivo: String? = null) {
        val historico = HistoryService.rejeicao(produto, produto.validadeAtual, motivo)
        val atualizado = produto.copy(
            status = StatusProduto.NORMAL,
            historico = (produto.historico + historico).toMutableList()
        )
        repo.salvar(atualizado)
        firebaseRepo.salvarRemoto(atualizado) // 🚀 Sincroniza
        carregar()
    }

    // =========================
    // VENDAS E ESTOQUE
    // =========================
    suspend fun registrarVenda(
        produtoId: String,
        quantidadeVendida: Int,
        validadeSelecionada: String? = null
    ) {
        val produtoOriginal = produtos.value.find { it.id == produtoId } ?: return
        if (quantidadeVendida <= 0) return

        val produto = produtoOriginal.copy(
            validades = produtoOriginal.validades.map { it.copy() }.toMutableList(),
            historico = produtoOriginal.historico.toMutableList()
        )

        // 🔹 Filtra validades que ainda têm estoque (usando .quantidade)
        val validadesOrdenadas = produto.validades
            .filter { (it.quantidade ?: 0) > 0 }
            .sortedBy { it.validade }

        val validade = validadeSelecionada?.let { data ->
            validadesOrdenadas.find { it.validade == data }
        } ?: validadesOrdenadas.firstOrNull()
        ?: return

        val estoqueAntes = validade.quantidade ?: 0
        if (estoqueAntes < quantidadeVendida) {
            throw IllegalStateException("Estoque insuficiente")
        }

        // 🔻 Realiza a venda
        validade.quantidade = estoqueAntes - quantidadeVendida

        // 🔄 Recalcula o total do produto (usando .quantidade)
        produto.quantidadeAtual =
            produto.validades.sumOf { it.quantidade ?: 0 }

        // 🔄 Atualiza qual é a validade mais próxima que ainda tem estoque
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

        // 💾 SALVAMENTO DUPLO (LOCAL + FIREBASE)
        repo.salvar(produto)
        firebaseRepo.salvarRemoto(produto)

        // 🔴 Atualiza a lista na tela
        _produtos.value = _produtos.value.map {
            if (it.id == produto.id) produto else it
        }
    }
    // ==========================================
    // REGRA DE DUPLICIDADE PARA IMPORTAÇÃO
    // ==========================================
    suspend fun existeDuplicidadeParaImportacao(novo: Produto): Boolean {
        val validade = novo.validadeAtual ?: return false

        // Buscamos a lista atual (que agora já inclui o Firebase se você carregar)
        val existentes = repo.carregar()

        return existentes.any { existente ->
            // Se for o mesmo ID, não é duplicidade, é o mesmo produto
            if (existente.id == novo.id) return@any false

            // Só checa se a validade for a mesma
            if (existente.validadeAtual != validade) return@any false

            val codigoInternoIgual = !novo.codigoInterno.isNullOrBlank() &&
                    novo.codigoInterno == existente.codigoInterno

            val codigoBarrasIgual = !novo.codigoBarras.isNullOrBlank() &&
                    novo.codigoBarras == existente.codigoBarras

            codigoInternoIgual || codigoBarrasIgual
        }
    }
    // ==========================================
    // SINCRONIZAR TUDO (LOCAL -> FIREBASE)
    // ==========================================
    suspend fun sincronizarTudoComFirebase() {
        try {
            // 1. Pega todos os produtos que estão no celular
            val listaLocal = repo.carregar()

            if (listaLocal.isEmpty()) {
                Log.d("fire_sync", "Nenhum produto local para sincronizar.")
                return
            }

            Log.d("fire_sync", "Iniciando sincronização de ${listaLocal.size} produtos...")

            // 2. Envia a lista completa para o Repository do Firebase
            // O método salvarTodosRemoto já percorre a lista e salva um por um
            firebaseRepo.salvarTodosRemoto(listaLocal)

            Log.d("fire_sync", "✅ Sincronização concluída com sucesso!")
        } catch (e: Exception) {
            Log.e("fire_sync", "❌ Erro na sincronização: ${e.message}")
        }
    }
    suspend fun atualizarQuantidade(produto: Produto, novaQuantidade: Int) {
        val historico = HistoryService.verificacaoEstoque(produto, produto.quantidadeAtual ?: 0, novaQuantidade)
        val atualizado = produto.copy(
            quantidadeAtual = novaQuantidade,
            historico = (produto.historico + historico).toMutableList()
        )
        repo.salvar(atualizado)
        firebaseRepo.salvarRemoto(atualizado) // 🚀 Sincroniza
        carregar()
    }

    suspend fun marcarPrecoEmNegociacao(produto: Produto, motivo: String? = null) {
        val historico = HistoryService.precoEmNegociacao(produto, motivo)
        val atualizado = produto.copy(
            precoEmNegociacao = true,
            historico = (produto.historico + historico).toMutableList()
        )
        repo.salvar(atualizado)
        firebaseRepo.salvarRemoto(atualizado) // 🚀 Sincroniza
        carregar()
    }

    // =========================
    // UTILITÁRIOS
    // =========================
    private suspend fun existeDuplicidade(novo: Produto): Boolean {
        val validade = novo.validadeAtual ?: return false
        val existentes = repo.carregar()
        return existentes.any { existente ->
            if (existente.id == novo.id) return@any false
            if (existente.validadeAtual != validade) return@any false
            val codigoBarrasIgual = !novo.codigoBarras.isNullOrBlank() && novo.codigoBarras == existente.codigoBarras
            val codigoInternoIgual = !novo.codigoInterno.isNullOrBlank() && novo.codigoInterno == existente.codigoInterno
            codigoBarrasIgual || codigoInternoIgual
        }
    }

    fun getProdutoById(id: String): Produto? = _produtos.value.find { it.id == id }
}