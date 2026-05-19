package com.hs.solutions.hstimecheck_2_0.historico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hs.solutions.hstimecheck_2_0.core.ProductService
import com.hs.solutions.hstimecheck_2_0.models.TipoEventoHistorico
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ================= UI MODEL =================
data class HistoricoViewItem(
    val titulo: String,
    val descricao: String,
    val descricaoProduto: String,
    val codigoInterno: String?,
    val codigoBarras: String?,
    val validade: String?,
    val dataEvento: String,
    val tipoEvento: TipoEventoHistorico
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
                produto.historico.map { h ->
                    HistoricoViewItem(
                        titulo = h.titulo.ifBlank { tituloPadrao(h.tipoEvento) },
                        descricao = h.descricao,
                        descricaoProduto = h.descricaoProduto.ifBlank { produto.descricao },
                        codigoInterno = h.codigoInterno ?: produto.codigoInterno,
                        codigoBarras = h.codigoBarras ?: produto.codigoBarras,
                        validade = h.validade,
                        dataEvento = h.dataEvento,
                        tipoEvento = h.tipoEvento
                    )
                }
            }
                .filter { item ->
                    val textoBusca = q.trim()
                    val matchBusca = textoBusca.isBlank() ||
                            item.descricaoProduto.contains(textoBusca, ignoreCase = true) ||
                            item.descricao.contains(textoBusca, ignoreCase = true) ||
                            item.codigoBarras?.contains(textoBusca, ignoreCase = true) == true ||
                            item.codigoInterno?.contains(textoBusca, ignoreCase = true) == true ||
                            item.titulo.contains(textoBusca, ignoreCase = true)

                    val matchFiltro = filtro == null || item.tipoEvento == filtro

                    matchBusca && matchFiltro
                }
                .sortedByDescending { parseDataEventoMillis(it.dataEvento) }
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

    private fun tituloPadrao(tipo: TipoEventoHistorico): String =
        tipo.name.lowercase(Locale.getDefault())
            .split("_")
            .joinToString(" ") { palavra -> palavra.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

    private fun parseDataEventoMillis(data: String): Long {
        data.toLongOrNull()?.let { return it }

        val patterns = listOf(
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )

        return patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                LocalDateTime.parse(data, DateTimeFormatter.ofPattern(pattern))
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()
        } ?: 0L
    }
}