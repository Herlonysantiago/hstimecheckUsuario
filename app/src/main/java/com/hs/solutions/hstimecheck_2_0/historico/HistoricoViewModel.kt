package com.hs.solutions.hstimecheck_2_0.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hs.solutions.hstimecheck_2_0.core.ProductService
import com.hs.solutions.hstimecheck_2_0.models.TipoEventoHistorico
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ================= UI MODEL =================
data class HistoricoViewItem(
    val titulo: String,
    val descricao: String,
    val codigoInterno: String?,
    val codigoBarras: String?,
    val validade: String?,
    val dataEvento: String
)




// ================= VIEWMODEL =================
class HistoricoViewModel(
    private val service: ProductService
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filtroEvento = MutableStateFlow<TipoEventoHistorico?>(null)


    fun setQuery(texto: String) {
        query.value = texto
    }

    fun setFiltroEvento(tipo: TipoEventoHistorico?) {
        filtroEvento.value = tipo
    }
    val historico: StateFlow<List<HistoricoViewItem>> =
        combine(service.produtos, query, filtroEvento) { produtos, q, filtro ->

            produtos.flatMap { produto ->
                produto.historico.mapNotNull { h ->

                    val titulo = h.titulo ?: h.tipoEvento?.name ?: return@mapNotNull null

                    HistoricoViewItem(
                        titulo = titulo,
                        descricao = h.descricao ?: "",
                        codigoInterno = h.codigoInterno,
                        codigoBarras = h.codigoBarras,
                        validade = h.validade,
                        dataEvento = h.dataEvento
                    )
                }
            }
                .filter { item ->

                    val matchBusca =
                        q.isBlank() ||
                                item.descricao.contains(q, true) ||
                                item.codigoBarras?.contains(q) == true ||
                                item.codigoInterno?.contains(q) == true ||
                                item.titulo.contains(q, true)

                    val matchFiltro =
                        filtro == null || item.titulo.contains(filtro.name, true)

                    matchBusca && matchFiltro
                }
                .sortedByDescending { it.dataEvento }

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    fun carregar() {
        viewModelScope.launch {
            service.carregar()
        }
    }
}

// ================= FACTORY =================

