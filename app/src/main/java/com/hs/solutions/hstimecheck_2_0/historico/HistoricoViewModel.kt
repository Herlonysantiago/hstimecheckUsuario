package com.hs.solutions.hstimecheck_2_0.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hs.solutions.hstimecheck_2_0.core.ProductService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ================= MODELO DE VISUALIZAÇÃO =================

data class HistoricoViewItem(
    val produtoId: String,
    val descricaoProduto: String,
    val codigoBarras: String,
    val dataEvento: String,
    val evento: String,
    val detalhe: String?
)

// ================= VIEWMODEL =================

class HistoricoViewModel(
    private val service: ProductService
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filtroEvento = MutableStateFlow<String?>(null)

    val historico: StateFlow<List<HistoricoViewItem>> =
        combine(service.produtos, query, filtroEvento) { produtos, q, eventoFiltro ->

            produtos.flatMap { produto ->
                produto.historico.map { h ->
                    HistoricoViewItem(
                        produtoId = produto.id,
                        descricaoProduto = produto.descricao,
                        codigoBarras = produto.codigoBarras,
                        dataEvento = h.dataEvento,
                        evento = h.evento,
                        detalhe = h.detalhe
                    )
                }
            }
                .filter {
                    q.isBlank() ||
                            it.descricaoProduto.contains(q, true) ||
                            it.codigoBarras.contains(q) ||
                            it.evento.contains(q, true)
                }
                .filter {
                    eventoFiltro == null || it.evento == eventoFiltro
                }
                .sortedByDescending { it.dataEvento }

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun setQuery(texto: String) {
        query.value = texto
    }

    fun setFiltroEvento(evento: String?) {
        filtroEvento.value = evento
    }

    fun carregar() {
        viewModelScope.launch {
            service.carregar()
        }
    }
}

// ================= FACTORY =================

class HistoricoViewModelFactory(
    private val service: ProductService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoricoViewModel(service) as T
    }
}
