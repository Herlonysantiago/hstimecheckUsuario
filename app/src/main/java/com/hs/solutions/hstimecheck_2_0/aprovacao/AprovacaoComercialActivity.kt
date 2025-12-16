package com.hs.solutions.hstimecheck_2_0.aprovacao

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.core.ProductService
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.widget.EditText
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import androidx.appcompat.app.AlertDialog

class AprovacaoComercialActivity : AppCompatActivity() {

    private lateinit var adapter: AprovacaoAdapter
    private lateinit var recyclerView: RecyclerView
    private val listaExibida = mutableListOf<AprovacaoItem>()

    private lateinit var productService: ProductService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aprovacao_comercial)

        recyclerView = findViewById(R.id.recyclerViewAprovacao)

        // ✅ INSTÂNCIA REAL DO SERVICE

        productService = AppContainer.productService


        lifecycleScope.launch {
            productService.carregar()
            carregarDados()
            configurarAdapter()
        }
    }

    private suspend fun carregarDados() {
        val produtos = productService.produtos.first()

        val filtrados = produtos.filter { it.status == StatusProduto.AGUARDANDO_APROVACAO }

        listaExibida.clear()
        listaExibida.addAll(
            filtrados.map { p ->
                AprovacaoItem(
                    id = p.id,
                    descricao = p.descricao,
                    codigo = p.codigoBarras,
                    precoAtual = p.precoAtual ?: 0.0,
                    precoSugerido = p.precoAtual ?: 0.0
                )
            }
        )
    }

    private fun configurarAdapter() {

        adapter = AprovacaoAdapter(
            listaExibida,

            // ================= APROVAR =================
            onAprovar = { item ->
                val produto = productService.getProdutoById(item.id) ?: return@AprovacaoAdapter

                val editPreco = EditText(this).apply {
                    hint = "Preço aprovado"
                    setText(item.precoSugerido.toString())
                    inputType =
                        android.text.InputType.TYPE_CLASS_NUMBER or
                                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                }

                AlertDialog.Builder(this)
                    .setTitle("Aprovação Comercial")
                    .setMessage("Informe o preço aprovado")
                    .setView(editPreco)
                    .setPositiveButton("Aprovar") { _, _ ->
                        val preco = editPreco.text.toString().toDoubleOrNull()
                        if (preco != null) {
                            lifecycleScope.launch {
                                productService.aprovarComercial(produto, preco)
                                listaExibida.remove(item)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },

            // ================= REJEITAR =================
            onRejeitar = { item ->
                val produto = productService.getProdutoById(item.id) ?: return@AprovacaoAdapter

                val editMotivo = EditText(this).apply {
                    hint = "Motivo da rejeição (opcional)"
                }

                AlertDialog.Builder(this)
                    .setTitle("Rejeitar Aprovação")
                    .setView(editMotivo)
                    .setPositiveButton("Rejeitar") { _, _ ->
                        lifecycleScope.launch {
                            productService.rejeitarComercial(produto, editMotivo.text.toString())
                            listaExibida.remove(item)
                            adapter.notifyDataSetChanged()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },

            // ================= EDITAR (mantido) =================
            onEditar = { item ->
                Toast.makeText(this, "Edição futura", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.adapter = adapter
    }


    private suspend fun aprovar(item: AprovacaoItem) {
        val produto = productService.produtos.first().find { it.id == item.id } ?: return

        produto.precoAtual = item.precoSugerido

        productService.mudarStatus(produto, StatusProduto.NORMAL)
        productService.inserirOuAtualizar(produto)

        listaExibida.remove(item)
        adapter.notifyDataSetChanged()

        Toast.makeText(this, "Aprovado!", Toast.LENGTH_SHORT).show()
    }

    private suspend fun rejeitar(item: AprovacaoItem) {
        val produto = productService.produtos.first().find { it.id == item.id } ?: return

        productService.mudarStatus(produto, StatusProduto.NORMAL)
        productService.inserirOuAtualizar(produto)

        listaExibida.remove(item)
        adapter.notifyDataSetChanged()

        Toast.makeText(this, "Rejeitado!", Toast.LENGTH_SHORT).show()
    }

    private fun editar(item: AprovacaoItem) {
        Toast.makeText(this, "Abrir edição", Toast.LENGTH_SHORT).show()
    }
}
