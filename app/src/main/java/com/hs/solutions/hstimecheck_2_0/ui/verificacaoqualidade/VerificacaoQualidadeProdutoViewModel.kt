package com.hs.solutions.hstimecheck_2_0.ui.verificacaoqualidade

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hs.solutions.hstimecheck_2_0.core.ProductService
import com.hs.solutions.hstimecheck_2_0.models.HistoricoItem
import com.hs.solutions.hstimecheck_2_0.models.Produto
import kotlinx.coroutines.launch
import android.util.Log

class VerificacaoQualidadeProdutoViewModel(
    private val productService: ProductService
) : ViewModel() {

    var produto by mutableStateOf<Produto?>(null)
        private set

    var historicoValidades by mutableStateOf<List<HistoricoItem>>(emptyList())
        private set

    var erro by mutableStateOf<String?>(null)
        private set

    var carregando by mutableStateOf(false)
        private set

    fun carregar(codigoBarras: String?, codigoInterno: String?) {

        // evita chamadas repetidas
        if (carregando) return

        carregando = true
        erro = null

        viewModelScope.launch {

            productService.carregar()

            val encontrado = when {
                !codigoBarras.isNullOrBlank() ->
                    productService.buscarPorCodigoBarrasLocal(codigoBarras)

                !codigoInterno.isNullOrBlank() ->
                    productService.buscarPorCodigoInternoLocal(codigoInterno)

                else -> null
            }

            if (encontrado == null) {
                erro = "Produto não encontrado"
                produto = null
                historicoValidades = emptyList()
                carregando = false
                return@launch
            }

            produto = encontrado
            produto = encontrado

// =====================
// 🔎 DEBUG – HISTÓRICO BRUTO
// =====================
            Log.e("DEBUG_VAL", "Produto: ${encontrado.descricao}")
            Log.e("DEBUG_VAL", "Total de eventos no histórico: ${encontrado.historico.size}")

            encontrado.historico.forEachIndexed { index, item ->
                Log.e(
                    "DEBUG_VAL",
                    "[$index] validade=${item.validade} | evento=${item.tipoEvento} | data=${item.dataEvento}"
                )
            }

            historicoValidades = encontrado.historico
                .filter { !it.validade.isNullOrBlank() }
                .groupBy { it.validade }
                .map { (_, eventos) ->
                    eventos.last()   // ✅ último evento real daquela validade
                }
                .sortedByDescending { it.dataEvento }


            carregando = false
        }
    }
}
