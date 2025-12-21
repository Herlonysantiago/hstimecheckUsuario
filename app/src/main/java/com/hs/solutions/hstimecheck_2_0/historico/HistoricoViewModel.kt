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
        service.produtos
            .map { produtos ->
                produtos.flatMap { produto ->
                    produto.historico.mapNotNull { h ->

                        val tituloSeguro = when {
                            !h.titulo.isNullOrBlank() -> h.titulo
                            h.tipoEvento != null -> h.tipoEvento.name
                            else -> null
                        }

                        // 🔒 SE NÃO CONSEGUIR GERAR TÍTULO, DESCARTA O ITEM
                        tituloSeguro?.let {
                            HistoricoViewItem(
                                titulo = it,
                                descricao = h.descricao ?: "",
                                codigoInterno = h.codigoInterno,
                                codigoBarras = h.codigoBarras,
                                validade = h.validade,
                                dataEvento = h.dataEvento
                            )
                        }
                    }

                }.sortedByDescending { it.dataEvento }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun carregar() {
        viewModelScope.launch {
            service.carregar()
        }
    }
}

// ================= FACTORY =================

