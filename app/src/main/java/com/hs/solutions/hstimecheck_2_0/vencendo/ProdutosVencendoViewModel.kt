package com.hs.solutions.hstimecheck_2_0.vencendo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hs.solutions.hstimecheck_2_0.core.DateFormatter

class ProdutosVencendoViewModel : ViewModel() {

    private val productService = AppContainer.productService
    private var modo: String = "VENCENDO"

    fun setModo(novoModo: String) {
        modo = novoModo
        carregar()
    }

    private val _produtos = MutableStateFlow<List<Produto>>(emptyList())
    val produtos: StateFlow<List<Produto>> = _produtos

    fun carregar() {
        viewModelScope.launch {
            productService.carregar()

            val hoje = LocalDate.now()

            val lista = productService.produtos.value
                .filter { it.validadeAtual != null }
                .filter { produto ->
                    val validade = LocalDate.parse(produto.validadeAtual!!)
                    when (modo) {
                        "VENCIDOS" -> validade < hoje
                        else -> validade >= hoje && validade <= hoje.plusDays(3)
                    }
                }
                .sortedBy { it.validadeAtual }



            _produtos.value = lista
        }
    }

    fun enviarParaAprovacao(produto: Produto) {
        viewModelScope.launch {
            productService.mudarStatus(
                produto,
                StatusProduto.AGUARDANDO_APROVACAO
            )
            carregar()
        }
    }

    fun trabalharPreco(produto: Produto) {
        viewModelScope.launch {
            productService.mudarStatus(
                produto,
                StatusProduto.TRABALHANDO_PRECO
            )
            carregar()
        }
    }

    fun excluirValidade(produto: Produto, contexto: Context) {
        viewModelScope.launch {

            val codigo = produto.codigoInterno ?: "—"
            val descricao = produto.descricao
            val validade = DateFormatter.isoParaBr(produto.validadeAtual)

            AlertDialog.Builder(contexto)
                .setTitle("Excluir validade")
                .setMessage("Deseja solicitar voltar o preço ao normal antes de excluir a validade?")
                .setPositiveButton("Sim") { _, _ ->

                    val mensagem = """
                    Código: $codigo
                    Produto: $descricao
                    Validade: $validade

                    Gentileza voltar o preço.
                    Data esgotada.

                    HS TIMECHECK
                """.trimIndent()

                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/?text=${Uri.encode(mensagem)}")
                    )
                    contexto.startActivity(intent)

                    viewModelScope.launch {
                        productService.removerValidade(produto)
                        carregar()
                    }
                }
                .setNegativeButton("Não") { _, _ ->
                    viewModelScope.launch {
                        productService.removerValidade(produto)
                        carregar()
                    }
                }
                .setNeutralButton("Cancelar", null)
                .show()
        }
    }


}
