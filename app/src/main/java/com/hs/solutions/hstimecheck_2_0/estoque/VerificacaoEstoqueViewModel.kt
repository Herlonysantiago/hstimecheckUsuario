package com.hs.solutions.hstimecheck_2_0.estoque

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VerificacaoEstoqueViewModel : ViewModel() {

    private val service = AppContainer.productService

    private val _produtos = MutableStateFlow<List<Produto>>(emptyList())
    val produtos: StateFlow<List<Produto>> = _produtos

    fun carregar() {
        viewModelScope.launch {
            service.carregar()
            _produtos.value = service.produtos.value
                .filter { it.emVerificacaoEstoque }
        }
    }

    fun confirmarEstoque(produto: Produto, novaQuantidade: Int) {
        viewModelScope.launch {

            // 1️⃣ atualiza quantidade + histórico
            service.atualizarQuantidade(produto, novaQuantidade)

            // 2️⃣ remove da fila de verificação (SEM mudar status)
            service.mudarStatus(
                produto.copy(emVerificacaoEstoque = false),
                produto.status
            )

            carregar()
        }
    }
}


