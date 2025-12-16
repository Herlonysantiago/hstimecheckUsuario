package com.hs.solutions.hstimecheck_2_0.cadastro

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.core.*
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import com.hs.solutions.hstimecheck_2_0.scanner.ScannerActivity
import kotlinx.coroutines.*
import java.util.*

class CadastroProdutoActivity : AppCompatActivity() {

    private lateinit var productService: ProductService
    private lateinit var lookup: ProductLookupService
    private val scope = MainScope()

    private var produtoFotoUrl: String? = null
    private var fotoUriLocal: Uri? = null
    private var produtoId: String? = null
    private var codigoBarras: String = ""

    private lateinit var edtCodigoBarras: EditText
    private lateinit var edtCodigoInterno: EditText
    private lateinit var edtDescricao: EditText
    private lateinit var edtValidade: EditText
    private lateinit var edtPreco: EditText
    private lateinit var edtCaixa: EditText
    private lateinit var edtUnidade: EditText
    private lateinit var edtQtdPorCaixa: EditText

    private lateinit var imgProduto: ImageView
    private lateinit var btnBuscar: ImageButton
    private lateinit var btnFoto: ImageButton
    private lateinit var btnExcluir: Button
    private lateinit var btnScan: Button
    private lateinit var btnLimpar: Button
    private lateinit var btnSalvar: Button
    private lateinit var switchContinuo: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        productService = AppContainer.productService
        lookup = ProductLookupService(this)

        setContentView(R.layout.activity_cadastro_produto)
        initViews()

        produtoId = intent.getStringExtra("produto_id")
        codigoBarras = intent.getStringExtra("codigo_barras") ?: ""
        edtCodigoBarras.setText(codigoBarras)

        if (produtoId != null) {
            btnExcluir.visibility = View.VISIBLE
            carregarProdutoExistente(produtoId!!)
        }

        edtValidade.setOnClickListener { abrirCalendario() }

        switchContinuo.isChecked = AppContainer.lancamentoContinuo
        switchContinuo.setOnCheckedChangeListener { _, checked ->
            AppContainer.lancamentoContinuo = checked
        }

        carregarDadosJson()
        setupButtonSalvar()
        setupButtonExcluir()
        setupButtonsExtra()
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
        imgProduto = findViewById(R.id.imgProduto)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnFoto = findViewById(R.id.btnFoto)
        btnExcluir = findViewById(R.id.btnExcluir)
        btnScan = findViewById(R.id.btnScan)
        btnLimpar = findViewById(R.id.btnLimpar)
        btnSalvar = findViewById(R.id.btnSalvar)
        switchContinuo = findViewById(R.id.switchContinuo)
    }

    // ------------------------------------------------------------
    // BUSCA
    // ------------------------------------------------------------

    private fun buscarProdutoManual() {
        val codBar = edtCodigoBarras.text.toString().trim()
        val codInt = edtCodigoInterno.text.toString().trim()
        if (codBar.isBlank() && codInt.isBlank()) return

        scope.launch {
            val item = withContext(Dispatchers.IO) {
                if (codBar.isNotBlank())
                    lookup.buscarPorCodigoBarras(codBar)
                else
                    lookup.buscarPorCodigoInterno(codInt)
            } ?: return@launch

            edtDescricao.setText(item.descricao ?: item.complemento ?: "")
            edtCodigoInterno.setText(item.codigo?.toString() ?: "")
            edtCodigoBarras.setText(item.codigoBarras ?: codBar)

            // 🔹 AQUI ENTRA O extrairInfoCaixa
            extrairInfoCaixa(item.complemento)?.let {

                edtQtdPorCaixa.setText(it.unidadesPorCaixa.toString())
            }

            carregarFotoSeNecessario(item.codigoBarras ?: codBar)
        }
    }

    // ------------------------------------------------------------
    // FOTO
    // ------------------------------------------------------------

    private fun carregarFotoSeNecessario(codigo: String) {
        if (!produtoFotoUrl.isNullOrBlank()) return

        scope.launch {
            val foto = withContext(Dispatchers.IO) { lookup.buscarFoto(codigo) }
            if (!foto.isNullOrBlank()) {
                produtoFotoUrl = foto
                Glide.with(this@CadastroProdutoActivity).load(foto).into(imgProduto)
            }
        }
    }

    private val launcherFoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                fotoUriLocal = it.data?.data
                produtoFotoUrl = fotoUriLocal.toString()
                Glide.with(this).load(fotoUriLocal).into(imgProduto)
            }
        }

    // ------------------------------------------------------------
    // SALVAR
    // ------------------------------------------------------------

    private fun setupButtonSalvar() {
        btnSalvar.setOnClickListener {
            val cx = edtCaixa.text.toString().toIntOrNull()
            val un = edtUnidade.text.toString().toIntOrNull()
            val qpc = edtQtdPorCaixa.text.toString().toIntOrNull()

            val total = when {
                cx != null && qpc != null -> cx * qpc + (un ?: 0)
                un != null -> un
                else -> 0
            }

            val produto = Produto(
                id = produtoId ?: UUID.randomUUID().toString(),
                codigoBarras = edtCodigoBarras.text.toString(),
                codigoInterno = edtCodigoInterno.text.toString().ifBlank { null },
                descricao = edtDescricao.text.toString(),
                quantidadeAtual = total,
                quantidadePorCaixa = qpc,
                validadeAtual = converterData(edtValidade.text.toString()),
                precoAtual = edtPreco.text.toString().toDoubleOrNull(),
                status = StatusProduto.NORMAL,
                fotoUrl = produtoFotoUrl
            )

            scope.launch {
                withContext(Dispatchers.IO) {
                    productService.inserirOuAtualizar(produto)
                }
                finish()
            }
        }
    }

    // ------------------------------------------------------------
    // UTIL
    // ------------------------------------------------------------

    private fun carregarProdutoExistente(id: String) {
        productService.produtos.value.find { it.id == id }?.let {
            edtDescricao.setText(it.descricao)
            edtCodigoBarras.setText(it.codigoBarras)
            edtCodigoInterno.setText(it.codigoInterno)
            edtValidade.setText(it.validadeAtual)
            edtPreco.setText(it.precoAtual?.toString())
            edtQtdPorCaixa.setText(it.quantidadePorCaixa?.toString())
            edtUnidade.setText(it.quantidadeAtual?.toString())
        }
    }

    private fun setupButtonsExtra() {
        btnBuscar.setOnClickListener { buscarProdutoManual() }
        btnScan.setOnClickListener {
            launcherScanner.launch(Intent(this, ScannerActivity::class.java))
        }
        btnLimpar.setOnClickListener { limparFormularioCompleto() }
        btnFoto.setOnClickListener {
            launcherFoto.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
        }
    }

    private fun setupButtonExcluir() {
        btnExcluir.setOnClickListener {
            produtoId?.let {
                AlertDialog.Builder(this)
                    .setTitle("Excluir")
                    .setMessage("Deseja excluir o produto?")
                    .setPositiveButton("Excluir") { _, _ ->
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                productService.remover(it)
                            }
                            finish()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    private fun limparFormularioCompleto() {
        edtCodigoBarras.text.clear()
        edtCodigoInterno.text.clear()
        edtDescricao.text.clear()
        edtValidade.text.clear()
        edtPreco.text.clear()
        edtCaixa.text.clear()
        edtUnidade.text.clear()
        edtQtdPorCaixa.text.clear()
        imgProduto.setImageDrawable(null)
        produtoFotoUrl = null
        produtoId = null
    }

    private val launcherScanner =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                edtCodigoBarras.setText(it.data?.getStringExtra("codigo_barras") ?: "")
                carregarDadosJson()
            }
        }

    private fun carregarDadosJson() {
        val codBar = edtCodigoBarras.text.toString()
        if (codBar.isBlank()) return

        scope.launch {
            val item = withContext(Dispatchers.IO) {
                lookup.buscarPorCodigoBarras(codBar)
            } ?: return@launch
            edtCodigoInterno.setText(item.codigo?.toString() ?: "")

            edtDescricao.setText(item.descricao ?: item.complemento ?: "")
            extrairInfoCaixa(item.complemento)?.let {

                edtQtdPorCaixa.setText(it.unidadesPorCaixa.toString())
            }

            carregarFotoSeNecessario(codBar)
        }
    }

    private fun abrirCalendario() {
        val hoje = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            edtValidade.setText("%02d/%02d/%04d".format(d, m + 1, y))
        }, hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH),
            hoje.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun converterData(raw: String?): String? =
        try {
            raw?.split("/")?.let {
                "%04d-%02d-%02d".format(it[2].toInt(), it[1].toInt(), it[0].toInt())
            }
        } catch (_: Exception) { null }
}
