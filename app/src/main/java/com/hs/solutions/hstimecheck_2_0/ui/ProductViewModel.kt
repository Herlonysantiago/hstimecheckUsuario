package com.hs.solutions.hstimecheck_2_0.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hs.solutions.hstimecheck_2_0.core.ProductService
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel(
    private val service: ProductService
) : ViewModel() {

    val produtos = service.produtos         // StateFlow<List<Produto>>
    var query = MutableStateFlow("")

    // -------------------- CARREGAR --------------------
    init {
        viewModelScope.launch { service.carregar() }
    }

    // -------------------- FILTRAGEM --------------------
    val produtosFiltrados = combine(produtos, query) { lista, busca ->
        lista.filter {
            busca.isBlank() ||
                    it.descricao.contains(busca, ignoreCase = true) ||
                    it.codigoBarras.contains(busca) ||
                    (it.codigoInterno?.contains(busca) ?: false)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // -------------------- ATUALIZAR STATUS --------------------
    fun enviarParaAprovacao(ids: List<String>) {
        viewModelScope.launch {
            ids.forEach { id ->
                val p = produtos.value.find { it.id == id }
                if (p != null) {
                    service.mudarStatus(p, StatusProduto.AGUARDANDO_APROVACAO)
                }
            }
        }
    }

    // -------------------- ADICIONAR PRODUTO (scanner) --------------------
    fun adicionarProduto(codigo: String) {
        viewModelScope.launch {
            val novo = Produto(
                codigoBarras = codigo,
                descricao = "Produto via scanner",
                quantidadeAtual = 1
            )
            service.inserirOuAtualizar(novo)
        }
    }
}
