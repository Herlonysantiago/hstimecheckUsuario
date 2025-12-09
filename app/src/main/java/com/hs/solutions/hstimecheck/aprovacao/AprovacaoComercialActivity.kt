package com.hs.solutions.hstimecheck.aprovacao

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.hs.solutions.hstimecheck.R
import com.hs.solutions.hstimecheck.core.ProductRepository
import com.hs.solutions.hstimecheck.core.ProductService
import com.hs.solutions.hstimecheck.models.StatusProduto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.hs.solutions.hstimecheck.core.ProductRepositoryImpl
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
        val repository = ProductRepositoryImpl()
        productService = ProductService(repository)

        productService = ProductService(repository)

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
            onAprovar = { item ->
                lifecycleScope.launch { aprovar(item) }
            },
            onRejeitar = { item ->
                lifecycleScope.launch { rejeitar(item) }
            },
            onEditar = { item ->
                editar(item)
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
