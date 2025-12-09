package com.hs.solutions.hstimecheck.cadastro

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hs.solutions.hstimecheck.R
import com.hs.solutions.hstimecheck.core.AppContainer
import com.hs.solutions.hstimecheck.core.ProductLookupService
import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CadastroProdutoActivity : AppCompatActivity() {

    private val productService = AppContainer.productService
    private val scope = MainScope()
    private lateinit var lookup: ProductLookupService

    private lateinit var edtCodigoBarras: EditText
    private lateinit var edtCodigoInterno: EditText
    private lateinit var edtDescricao: EditText
    private lateinit var edtQuantidade: EditText
    private lateinit var edtValidade: EditText
    private lateinit var edtPreco: EditText

    private var codigoBarras: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_produto)

        lookup = ProductLookupService(this)

        // Recebido do ScannerActivity
        codigoBarras = intent.getStringExtra("codigo_barras") ?: ""

        initViews()
        edtValidade.setOnClickListener {
            abrirCalendario()
        }

        preencherCodigoBarras()
        carregarDadosJson()
        setupButton()
    }

    private fun initViews() {
        edtCodigoBarras = findViewById(R.id.edtCodigoBarras)
        edtCodigoInterno = findViewById(R.id.edtCodigoInterno)
        edtDescricao = findViewById(R.id.edtDescricao)
        edtQuantidade = findViewById(R.id.edtQuantidade)
        edtValidade = findViewById(R.id.edtValidade)
        edtPreco = findViewById(R.id.edtPreco)
    }

    // Preenche o campo do código de barras automaticamente
    private fun preencherCodigoBarras() {
        edtCodigoBarras.setText(codigoBarras)
    }
    private fun abrirCalendario() {
        val hoje = java.util.Calendar.getInstance()

        val ano = hoje.get(java.util.Calendar.YEAR)
        val mes = hoje.get(java.util.Calendar.MONTH)
        val dia = hoje.get(java.util.Calendar.DAY_OF_MONTH)

        val datePicker = android.app.DatePickerDialog(
            this,
            { _, anoSelecionado, mesSelecionado, diaSelecionado ->

                // formata dd/MM/yyyy
                val data = String.format("%02d/%02d/%04d", diaSelecionado, mesSelecionado + 1, anoSelecionado)
                edtValidade.setText(data)
            },
            ano, mes, dia
        )

        datePicker.show()
    }

    // ----------------------------------------------------------
    // 🚀 CARREGAR AUTOMATICAMENTE DESCRIÇÃO E CÓDIGOS DO JSON
    // ----------------------------------------------------------
    private fun carregarDadosJson() {
        scope.launch {
            val jsonItem = lookup.buscarPorCodigoBarras(codigoBarras)

            if (jsonItem != null) {

                // Descrição automática
                edtDescricao.setText(
                    jsonItem.descricao?.trim()
                        ?: jsonItem.complemento?.trim()
                        ?: ""
                )

                // Código interno automático
                edtCodigoInterno.setText(jsonItem.codigo?.toString() ?: "")
            }
        }
    }

    // ----------------------------------------------------------
    // SALVAR PRODUTO
    // ----------------------------------------------------------
    private fun setupButton() {
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)

        btnSalvar.setOnClickListener {

            val descricao = edtDescricao.text.toString().trim()
            val codigoInterno = edtCodigoInterno.text.toString().trim()
            val qtd = edtQuantidade.text.toString().toIntOrNull() ?: 0
            val validade = edtValidade.text.toString().trim()
            val preco = edtPreco.text.toString().toDoubleOrNull()

            if (descricao.isBlank()) {
                Toast.makeText(this, "Informe a descrição!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novo = Produto(
                codigoBarras = codigoBarras,
                codigoInterno = codigoInterno.ifBlank { null },
                descricao = descricao,
                quantidadeAtual = qtd,
                validadeAtual = validade.ifBlank { null },
                precoAtual = preco,
                status = StatusProduto.NORMAL
            )

            scope.launch {
                productService.inserirOuAtualizar(novo)

                val result = intent
                result.putExtra("produto_id", novo.id)

                setResult(Activity.RESULT_OK, result)
                finish()
            }
        }
    }
}
