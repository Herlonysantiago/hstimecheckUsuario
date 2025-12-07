package com.hs.solutions.hstimecheck.aprovacao

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.hs.solutions.hstimecheck.R
import com.hs.solutions.hstimecheck.core.services.ProductService
import com.hs.solutions.hstimecheck.core.services.HistoryService
import com.hs.solutions.hstimecheck.model.StatusProduto

class AprovacaoComercialActivity : AppCompatActivity() {

    private lateinit var adapter: AprovacaoAdapter
    private lateinit var recyclerView: RecyclerView
    private val listaExibida = mutableListOf<AprovacaoItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aprovacao_comercial)

        recyclerView = findViewById(R.id.recyclerViewAprovacao)

        carregarDados()
        configurarAdapter()
    }

    private fun carregarDados() {
        val produtos = ProductService.getProdutos()

        val filtrados = produtos.filter {
            it.status == StatusProduto.AGUARDANDO_APROVACAO
        }

        listaExibida.clear()
        listaExibida.addAll(
            filtrados.map { p ->
                AprovacaoItem(
                    id = p.id,
                    descricao = p.descricao,
                    codigo = p.codigoBarras,
                    precoAtual = p.precoAtual ?: 0.0,
                    precoSugerido = p.precoAtual ?: 0.0  // ajustar lógica futura
                )
            }
        )
    }

    private fun configurarAdapter() {
        adapter = AprovacaoAdapter(
            listaExibida,
            onAprovar = { item -> aprovar(item) },
            onRejeitar = { item -> rejeitar(item) },
            onEditar = { item -> editar(item) }
        )

        recyclerView.adapter = adapter
    }

    private fun aprovar(item: AprovacaoItem) {
        val produto = ProductService.getProdutoPorId(item.id) ?: return

        // Atualiza o preço
        produto.precoAtual = item.precoSugerido
        produto.status = StatusProduto.NORMAL

        // SALVA no histórico centralizado
        HistoryService.registrarEvento(
            produtoId = produto.id,
            evento = "Aprovação Comercial",
            detalhe = "Preço aprovado: R$ ${item.precoSugerido}",
            preco = item.precoSugerido
        )

        // PERSISTE
        ProductService.salvarProduto(produto)

        // REMOVE DA LISTA
        listaExibida.remove(item)
        adapter.notifyDataSetChanged()

        Toast.makeText(this, "Aprovado!", Toast.LENGTH_SHORT).show()
    }

    private fun rejeitar(item: AprovacaoItem) {
        val produto = ProductService.getProdutoPorId(item.id) ?: return

        produto.status = StatusProduto.NORMAL

        HistoryService.registrarEvento(
            produtoId = produto.id,
            evento = "Rejeição Comercial",
            detalhe = "Sugestão não aprovada"
        )

        ProductService.salvarProduto(produto)

        listaExibida.remove(item)
        adapter.notifyDataSetChanged()

        Toast.makeText(this, "Rejeitado!", Toast.LENGTH_SHORT).show()
    }

    private fun editar(item: AprovacaoItem) {
        Toast.makeText(this, "Abrir tela de edição", Toast.LENGTH_SHORT).show()
        // próximo passo: criar BottomSheet de edição profissional
    }
}
