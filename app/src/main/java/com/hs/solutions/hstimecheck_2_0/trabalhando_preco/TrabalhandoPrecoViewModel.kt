package com.hs.solutions.hstimecheck_2_0.trabalhando_preco

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
}
