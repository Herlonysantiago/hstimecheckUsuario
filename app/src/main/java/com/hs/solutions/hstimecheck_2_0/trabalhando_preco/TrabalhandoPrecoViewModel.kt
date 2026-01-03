package com.hs.solutions.hstimecheck_2_0.trabalhando_preco

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.hs.solutions.hstimecheck_2_0.core.DateFormatter
class TrabalhandoPrecoViewModel : ViewModel() {

    private val service = AppContainer.productService

    private val _produtos = MutableStateFlow<List<Produto>>(emptyList())
    val produtos: StateFlow<List<Produto>> = _produtos

    fun carregar() {
        viewModelScope.launch {
            service.carregar()
            _produtos.value = service.produtos.value
                .filter { it.status == StatusProduto.TRABALHANDO_PRECO }
        }
    }

    fun marcarPrecoEmNegociacao(produto: Produto, motivo: String?) {
        viewModelScope.launch {
            service.marcarPrecoEmNegociacao(produto, motivo)
            carregar()
        }
    }

    fun enviarParaComprador(produto: Produto) {
        // Status NÃO muda
        // Histórico será ligado depois
    }

    fun enviarParaClientes(produtos: List<Produto>) {
        if (produtos.isEmpty()) return

        val mensagem = produtos.joinToString("\n\n") { produto ->
            """
        Produto: ${produto.codigoInterno ?: "-"} - ${produto.descricao}
        Código de barras: ${produto.codigoBarras ?: "-"}
        Validade: ${DateFormatter.isoParaBr(produto.validadeAtual)}

        Estoque: ${estoqueTexto(produto)}\n\n
}
        """.trimIndent()
        }
    }

}
