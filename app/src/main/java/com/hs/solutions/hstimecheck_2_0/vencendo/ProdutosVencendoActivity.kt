package com.hs.solutions.hstimecheck_2_0.vencendo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hs.solutions.hstimecheck_2_0.R
import kotlinx.coroutines.launch
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle

class ProdutosVencendoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtVazio: TextView

    private val viewModel: ProdutosVencendoViewModel by viewModels()
    private lateinit var adapter: ProdutosVencendoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produtos_vencendo)
        val modo = intent.getStringExtra("modo") ?: "VENCENDO"
        viewModel.setModo(modo)

        recyclerView = findViewById(R.id.recyclerProdutosVencendo)
        txtVazio = findViewById(R.id.txtVazio)

        adapter = ProdutosVencendoAdapter(
            onAprovacao = { viewModel.enviarParaAprovacao(it) },
            onPreco = { viewModel.trabalharPreco(it) },
            onExcluir = { produto, contexto ->
                viewModel.excluirValidade(produto, contexto)
            }

        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.produtos.collect { lista ->
                    txtVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
                    adapter.atualizar(lista)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.carregar()
    }
}
