package com.hs.solutions.hstimecheck_2_0.venda

import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.models.Produto
import kotlinx.coroutines.*

class VendaProdutoActivity : AppCompatActivity() {

    private val scope = MainScope()
    private val productService by lazy { AppContainer.productService }

    private lateinit var txtProduto: TextView
    private lateinit var txtValidade: TextView
    private lateinit var txtEstoque: TextView
    private lateinit var edtCx: EditText
    private lateinit var edtUn: EditText
    private lateinit var btnConfirmar: Button

    private var produto: Produto? = null
    private var validadeSelecionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venda_produto)

        txtProduto = findViewById(R.id.txtProduto)
        txtValidade = findViewById(R.id.txtValidade)
        txtEstoque = findViewById(R.id.txtEstoque)
        edtCx = findViewById(R.id.edtCxVenda)
        edtUn = findViewById(R.id.edtUnVenda)
        btnConfirmar = findViewById(R.id.btnConfirmarVenda)

        val produtoId = intent.getStringExtra("produto_id")
        validadeSelecionada = intent.getStringExtra("validade") ?: ""

        if (produtoId.isNullOrBlank() || validadeSelecionada.isBlank()) {
            toast("Erro ao abrir venda")
            finish()
            return
        }

        carregarProduto(produtoId)

        btnConfirmar.setOnClickListener {
            confirmarVenda()
        }
    }

    // =======================================================
    // CARREGAR PRODUTO (SOMENTE LEITURA)
    // =======================================================
    private fun carregarProduto(id: String) {
        scope.launch {
            productService.carregar()

            val encontrado = productService.produtos.value.find { it.id == id }
            if (encontrado == null) {
                toast("Produto não encontrado")
                finish()
                return@launch
            }

            val validade = encontrado.validades.find {
                it.validade == validadeSelecionada
            }

            if (validade == null) {
                toast("Validade não encontrada")
                finish()
                return@launch
            }

            produto = encontrado

            txtProduto.text = encontrado.descricao
            txtValidade.text = "Validade: ${validade.validade}"

            val qpc = encontrado.quantidadePorCaixa
            val estoque = validade.quantidade ?: 0

            txtEstoque.text = if (qpc != null && qpc > 0) {
                val cx = estoque / qpc
                val un = estoque % qpc
                if (un > 0) "Estoque: $cx CX • $un UN"
                else "Estoque: $cx CX"
            } else {
                "Estoque: $estoque UN"
            }
        }
    }

    // =======================================================
    // CONFIRMAR VENDA (APENAS COLETA DE DADOS)
    // =======================================================
    private fun confirmarVenda() {
        val produtoAtual = produto ?: return

        val cxVendidas = edtCx.text.toString().trim().toIntOrNull() ?: 0
        val unVendidas = edtUn.text.toString().trim().toIntOrNull() ?: 0

        val qtdPorCaixa = produtoAtual.quantidadePorCaixa
        if (qtdPorCaixa == null || qtdPorCaixa <= 0) {
            toast("Quantidade por caixa inválida")
            return
        }

        // 🔴 Quantidade REAL vendida (UN)
        val totalVendida = (cxVendidas * qtdPorCaixa) + unVendidas

        if (totalVendida <= 0) {
            toast("Informe a quantidade vendida")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar venda")
            .setMessage("Confirmar venda de $totalVendida unidades?")
            .setPositiveButton("Confirmar") { _, _ ->
                registrarVenda(totalVendida)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // =======================================================
    // REGISTRAR VENDA (DELEGA TOTAL AO SERVICE)
    // =======================================================
    private fun registrarVenda(totalVendida: Int) {
        val produtoAtual = produto ?: return

        scope.launch {
            try {
                productService.registrarVenda(
                    produtoId = produtoAtual.id,
                    quantidadeVendida = totalVendida,
                    validadeSelecionada = validadeSelecionada
                )

                setResult(Activity.RESULT_OK)
                finish()

            } catch (e: Exception) {
                toast(e.message ?: "Erro ao registrar venda")
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
