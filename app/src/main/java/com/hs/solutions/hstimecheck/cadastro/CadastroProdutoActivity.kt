package com.hs.solutions.hstimecheck.cadastro

import com.bumptech.glide.Glide
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.hs.solutions.hstimecheck.R
import com.hs.solutions.hstimecheck.core.AppContainer
import com.hs.solutions.hstimecheck.core.ProductLookupService
import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto
import kotlinx.coroutines.*

class CadastroProdutoActivity : AppCompatActivity() {

    private lateinit var productService: com.hs.solutions.hstimecheck.core.ProductService
    private val scope = MainScope()
    private var produtoFotoUrl: String? = null

    private lateinit var lookup: ProductLookupService

    private lateinit var edtCodigoBarras: EditText
    private lateinit var edtCodigoInterno:EditText
    private lateinit var edtDescricao: EditText
    private lateinit var edtValidade: EditText
    private lateinit var edtPreco: EditText

    // CAMPOS DE QUANTIDADE
    private lateinit var edtCaixa: EditText
    private lateinit var edtUnidade: EditText
    private lateinit var edtQtdPorCaixa: EditText   // ★ NOVO CAMPO

    private lateinit var imgProduto: ImageView
    private lateinit var btnFoto: ImageButton
    private lateinit var btnSalvar: Button
    private lateinit var btnExcluir: Button
    private lateinit var btnScan: Button
    private lateinit var btnLimpar: Button

    private var codigoBarras: String = ""
    private var produtoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        productService = AppContainer.productService

        setContentView(R.layout.activity_cadastro_produto)

        lookup = ProductLookupService(this)

        produtoId = intent.getStringExtra("produto_id")
        codigoBarras = intent.getStringExtra("codigo_barras") ?: ""

        initViews()

        if (produtoId != null) {
            btnExcluir.visibility = View.VISIBLE
            carregarProdutoExistente(produtoId!!)
        }

        preencherCodigoBarras()

        edtValidade.setOnClickListener { abrirCalendario() }

        carregarDadosJson()
        setupButtonSalvar()
        setupButtonExcluir()
        setupButtonsExtra()
    }

    private fun carregarProdutoExistente(id: String) {
        val produto = productService.produtos.value.find { it.id == id } ?: return

        // CAMPOS BÁSICOS
        edtCodigoBarras.setText(produto.codigoBarras ?: "")
        edtCodigoInterno.setText(produto.codigoInterno ?: "")
        edtDescricao.setText(produto.descricao ?: "")
        edtValidade.setText(produto.validadeAtual ?: "")
        edtPreco.setText(produto.precoAtual?.toString() ?: "")

        // BLOQUEAR EDIÇÃO DO CÓDIGO
        edtCodigoBarras.isEnabled = false
        edtCodigoBarras.isFocusable = false
        edtCodigoBarras.isFocusableInTouchMode = false

        // QUANTIDADES
        edtUnidade.setText(produto.quantidadeAtual?.toString() ?: "0")
        edtCaixa.setText("")
        edtQtdPorCaixa.setText("")

        // FOTO JÁ EXISTE → exibir
        if (!produto.fotoUrl.isNullOrBlank()) {
            Glide.with(this).load(produto.fotoUrl).into(imgProduto)
            return
        }

        // FOTO NÃO EXISTE → buscar no OpenFoodFacts
        if (!produto.codigoBarras.isNullOrBlank()) {

            scope.launch {
                val foto = withContext(Dispatchers.IO) {
                    lookup.buscarFoto(produto.codigoBarras)
                }

                if (!foto.isNullOrBlank()) {
                    produto.fotoUrl = foto

                    // salvar no repositório
                    withContext(Dispatchers.IO) {
                        productService.inserirOuAtualizar(produto)
                    }

                    // exibir
                    Glide.with(this@CadastroProdutoActivity)
                        .load(foto)
                        .into(imgProduto)
                }
            }
        }
    }



    private fun setupButtonExcluir() {
        btnExcluir.setOnClickListener {

            val id = produtoId ?: return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Excluir produto")
                .setMessage("Tem certeza que deseja excluir este produto?")
                .setPositiveButton("Excluir") { _, _ ->

                    scope.launch {
                        withContext(Dispatchers.IO) {
                            productService.remover(id)
                        }

                        Toast.makeText(
                            this@CadastroProdutoActivity,
                            "Produto removido!",
                            Toast.LENGTH_SHORT
                        ).show()

                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun initViews() {
        edtCodigoBarras = findViewById(R.id.edtCodigoBarras)
        edtCodigoInterno = findViewById(R.id.edtCodigoInterno)
        edtDescricao = findViewById(R.id.edtDescricao)
        edtValidade = findViewById(R.id.edtValidade)
        edtPreco = findViewById(R.id.edtPreco)

        edtCaixa = findViewById(R.id.edtCaixa)
        edtUnidade = findViewById(R.id.edtUnidade)
        edtQtdPorCaixa = findViewById(R.id.edtQtdPorCaixa)

        btnSalvar = findViewById(R.id.btnSalvar)
        btnExcluir = findViewById(R.id.btnExcluir)
        btnScan = findViewById(R.id.btnScan)
        btnLimpar = findViewById(R.id.btnLimpar)
        btnFoto = findViewById(R.id.btnFoto)
        imgProduto = findViewById(R.id.imgProduto)
    }


    private fun preencherCodigoBarras() {
        edtCodigoBarras.setText(codigoBarras)
    }

    private fun abrirCalendario() {
        val hoje = java.util.Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, y, m, d ->
                edtValidade.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
            },
            hoje.get(java.util.Calendar.YEAR),
            hoje.get(java.util.Calendar.MONTH),
            hoje.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun carregarDadosJson() {
        scope.launch {
            val jsonItem = withContext(Dispatchers.IO) {
                lookup.buscarPorCodigoBarras(codigoBarras)
            }

            if (jsonItem != null) {
                edtDescricao.setText(
                    jsonItem.descricao?.trim()
                        ?: jsonItem.complemento?.trim()
                        ?: ""
                )
                edtCodigoInterno.setText(jsonItem.codigo?.toString() ?: "")
            }

            val foto = withContext(Dispatchers.IO) { lookup.buscarFoto(codigoBarras) }

            if (!foto.isNullOrBlank()) {
                produtoFotoUrl = foto  // criar uma variável interna
                Glide.with(this@CadastroProdutoActivity)
                    .load(foto)
                    .into(imgProduto)
            }
        }


    }

    private fun converterData(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val partes = raw.split("/")
            if (partes.size != 3) return null
            val dia = partes[0].toInt()
            val mes = partes[1].toInt()
            val ano = partes[2].toInt()
            "%04d-%02d-%02d".format(ano, mes, dia)
        } catch (_: Exception) {
            null
        }
    }

    /* -------------------------------------------------------------------------- */
    /*  SALVAR PRODUTO COM NOVA LÓGICA CX + UND + QPC                             */
    /* -------------------------------------------------------------------------- */

    private fun setupButtonSalvar() {

        btnSalvar.setOnClickListener {

            val descricao = edtDescricao.text.toString().trim()
            val codigoInterno = edtCodigoInterno.text.toString().trim()
            val validadeString = edtValidade.text.toString().trim()
            val preco = edtPreco.text.toString().toDoubleOrNull()

            val cx = edtCaixa.text.toString().toIntOrNull()
            val un = edtUnidade.text.toString().toIntOrNull()
            val qpc = edtQtdPorCaixa.text.toString().toIntOrNull()

            if (descricao.isBlank()) {
                Toast.makeText(this, "Informe a descrição!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // REGRA: se preencher CX + UN, QPC é obrigatório
            if (cx != null && cx > 0 && un != null && un > 0 && (qpc == null || qpc == 0)) {
                Toast.makeText(
                    this,
                    "Informe a quantidade por caixa (QNT/CX) quando preencher CX + UND",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            var quantidadeFinal: Int = 0

            // Só UN informado
            if ((cx == null || cx == 0) && un != null) {
                quantidadeFinal = un
            }

            // Só CX informado (sem QPC)
            if ((un == null || un == 0) && cx != null && qpc == null) {
                quantidadeFinal = cx
            }

            // CX + QPC → converte para UN total
            if (cx != null && cx > 0 && qpc != null && qpc > 0 && (un == null || un == 0)) {
                quantidadeFinal = cx * qpc
            }

            // UN + QPC → calcula caixas + sobras (mas salva total)
            if (un != null && un > 0 && qpc != null && qpc > 0 && (cx == null || cx == 0)) {
                quantidadeFinal = un  // mantém UN como valor principal salvo
            }

            // CX + UN + QPC → soma total (cx * qpc + un)
            if (cx != null && un != null && qpc != null && qpc > 0) {
                quantidadeFinal = (cx * qpc) + un
            }

            val novo = Produto(
                id = produtoId ?: java.util.UUID.randomUUID().toString(),
                codigoBarras = edtCodigoBarras.text.toString(),
                codigoInterno = codigoInterno.ifBlank { null },
                descricao = descricao,
                quantidadeAtual = quantidadeFinal,
                quantidadePorCaixa = qpc,   // ★ NOVO
                validadeAtual = converterData(edtValidade.text.toString()),
                precoAtual = preco,
                status = StatusProduto.NORMAL,
                fotoUrl = produtoFotoUrl
            )


            scope.launch {
                withContext(Dispatchers.IO) {
                    productService.inserirOuAtualizar(novo)
                }
                Toast.makeText(this@CadastroProdutoActivity, "Salvo!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun setupButtonsExtra() {

        btnScan.setOnClickListener {
            val intent = android.content.Intent(
                this,
                com.hs.solutions.hstimecheck.scanner.ScannerActivity::class.java
            )
            launcherScanner.launch(intent)
        }

        btnLimpar.setOnClickListener {
            edtCodigoInterno.text.clear()
            edtDescricao.text.clear()
            edtPreco.text.clear()
            edtValidade.text.clear()
            edtCaixa.text.clear()
            edtUnidade.text.clear()
            edtQtdPorCaixa.text.clear()
        }
    }

    private val launcherScanner = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val codigo = result.data?.getStringExtra("codigo_barras") ?: ""
            val descricao = result.data?.getStringExtra("descricao") ?: ""
            val interno = result.data?.getStringExtra("codigo_interno") ?: ""

            edtCodigoBarras.setText(codigo)
            edtDescricao.setText(descricao)
            edtCodigoInterno.setText(interno)

            codigoBarras = codigo

            carregarDadosJson()
        }
    }
}
