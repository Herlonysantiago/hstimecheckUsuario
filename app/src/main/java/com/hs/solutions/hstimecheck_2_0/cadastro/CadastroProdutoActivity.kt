package com.hs.solutions.hstimecheck_2_0.cadastro

import com.hs.solutions.hstimecheck_2_0.ui.FullImageActivity
import android.content.Intent
import android.widget.Toast
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.core.*
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import com.hs.solutions.hstimecheck_2_0.scanner.ScannerActivity
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class CadastroProdutoActivity : AppCompatActivity() {

    private lateinit var productService: ProductService
    private lateinit var lookup: ProductLookupService
    private val scope = MainScope()

    private var produtoId: String? = null
    private var produtoFotoUrl: String? = null

    // FOTO
    private var fotoUriLocal: Uri? = null
    private var fotoFile: File? = null

    // VIEWS
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

    // ------------------------------------------------------------
    // LAUNCHERS
    // ------------------------------------------------------------

    private lateinit var launcherCamera: ActivityResultLauncher<Uri>

    private val launcherGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult

            val arquivo = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "produto_${System.currentTimeMillis()}.jpg"
            )

            contentResolver.openInputStream(uri)?.use { input ->
                arquivo.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            produtoFotoUrl = arquivo.absolutePath

            Glide.with(this)
                .load(arquivo)
                .into(imgProduto)

            val codigo = edtCodigoBarras.text.toString().trim()
            if (codigo.isNotBlank()) {
                FotoRepository.salvar(this, codigo, produtoFotoUrl!!)
            }
        }

    private val launcherScanner =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                edtCodigoBarras.setText(it.data?.getStringExtra("codigo_barras") ?: "")
                carregarDadosJson()
            }
        }

    // ------------------------------------------------------------
    // LIFECYCLE
    // ------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        FotoRepository.carregar(this)

        productService = AppContainer.productService
        lookup = ProductLookupService(this)

        setContentView(R.layout.activity_cadastro_produto)
        initViews()

        produtoId = intent.getStringExtra("produto_id")

        produtoId?.let {
            btnExcluir.visibility = View.VISIBLE
            carregarProdutoExistente(it)
        }

        edtValidade.setOnClickListener { abrirCalendario() }

        switchContinuo.isChecked = AppContainer.lancamentoContinuo
        switchContinuo.setOnCheckedChangeListener { _, v ->
            AppContainer.lancamentoContinuo = v
        }

        launcherCamera = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { sucesso ->
            if (sucesso && fotoFile != null) {
                produtoFotoUrl = fotoFile!!.absolutePath
                Glide.with(this).load(fotoFile).into(imgProduto)

                val codigo = edtCodigoBarras.text.toString().trim()
                if (codigo.isNotBlank()) {
                    FotoRepository.salvar(this, codigo, produtoFotoUrl!!)
                }
            }
        }
        imgProduto.setOnClickListener {

            if (produtoFotoUrl.isNullOrBlank()) {
                Toast.makeText(this, "Nenhuma foto disponível", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, FullImageActivity::class.java)
            intent.putExtra("fotoUrl", produtoFotoUrl)
            startActivity(intent)
        }
        imgProduto.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        setupButtons()
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
        imgProduto.isClickable = true
        imgProduto.isFocusable = true
    }

    // ------------------------------------------------------------
    // FOTO
    // ------------------------------------------------------------

    private fun abrirCamera() {
        fotoFile = File.createTempFile(
            "produto_",
            ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        fotoUriLocal = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            fotoFile!!
        )

        launcherCamera.launch(fotoUriLocal!!)
    }

    private fun carregarFotoSeNecessario(codigo: String) {

        if (!produtoFotoUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(produtoFotoUrl)
                .into(imgProduto)
            return
        }

        FotoRepository.buscar(codigo)?.let { foto ->
            produtoFotoUrl = foto
            Glide.with(this).load(foto).into(imgProduto)
            return
        }

        scope.launch {
            val fotoOnline = withContext(Dispatchers.IO) {
                lookup.buscarFoto(codigo)
            }

            if (!fotoOnline.isNullOrBlank()) {
                produtoFotoUrl = fotoOnline
                FotoRepository.salvar(this@CadastroProdutoActivity, codigo, fotoOnline)
                Glide.with(this@CadastroProdutoActivity)
                    .load(fotoOnline)
                    .into(imgProduto)
            }
        }
    }


    // ------------------------------------------------------------
    // DADOS / JSON
    // ------------------------------------------------------------

    private fun carregarDadosJson() {
        val codBar = edtCodigoBarras.text.toString().trim()
        if (codBar.isBlank()) return

        scope.launch {
            val item = withContext(Dispatchers.IO) {
                lookup.buscarPorCodigoBarras(codBar)
            } ?: return@launch

            if (edtCodigoInterno.text.isNullOrBlank()) {
                edtCodigoInterno.setText(item.codigo?.toString() ?: "")
            }

            if (edtDescricao.text.isNullOrBlank()) {
                edtDescricao.setText(item.descricao ?: item.complemento ?: "")
            }

            extrairInfoCaixa(item.complemento)?.let {
                if (edtQtdPorCaixa.text.isNullOrBlank()) {
                    edtQtdPorCaixa.setText(it.unidadesPorCaixa.toString())
                }
            }

            carregarFotoSeNecessario(codBar)
        }
    }

    // ------------------------------------------------------------
    // BOTÕES
    // ------------------------------------------------------------

    private fun setupButtons() {
        btnBuscar.setOnClickListener { carregarDadosJson() }

        btnFoto.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Foto do produto")
                .setItems(arrayOf("Câmera", "Galeria")) { _, which ->
                    when (which) {
                        0 -> abrirCamera()
                        1 -> launcherGaleria.launch("image/*")
                    }
                }
                .show()
        }

        btnScan.setOnClickListener {
            launcherScanner.launch(Intent(this, ScannerActivity::class.java))
        }

        btnSalvar.setOnClickListener { salvarProduto() }
        btnLimpar.setOnClickListener { limparFormulario() }
        btnExcluir.setOnClickListener { excluirProduto() }
        imgProduto.isClickable = true
        imgProduto.isFocusable = true



    }

    // ------------------------------------------------------------
    // SALVAR / EXCLUIR
    // ------------------------------------------------------------

    private fun salvarProduto() {
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

    private fun excluirProduto() {
        produtoId ?: return
        AlertDialog.Builder(this)
            .setTitle("Excluir")
            .setMessage("Deseja excluir o produto?")
            .setPositiveButton("Excluir") { _, _ ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        productService.remover(produtoId!!)
                    }
                    finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ------------------------------------------------------------
    // UTIL
    // ------------------------------------------------------------

    private fun carregarProdutoExistente(id: String) {
        productService.produtos.value.find { it.id == id }?.let {
            edtCodigoBarras.setText(it.codigoBarras)
            edtCodigoInterno.setText(it.codigoInterno)
            edtDescricao.setText(it.descricao)
            edtValidade.setText(dataParaTela(it.validadeAtual))
            edtPreco.setText(it.precoAtual?.toString())
            edtQtdPorCaixa.setText(it.quantidadePorCaixa?.toString())

            val (cx, un) = estoqueParaTela(
                it.quantidadeAtual,
                it.quantidadePorCaixa
            )

            edtCaixa.setText(if (cx > 0) cx.toString() else "")
            edtUnidade.setText(if (un > 0) un.toString() else "")

            produtoFotoUrl = it.fotoUrl
            it.codigoBarras?.let { c -> carregarFotoSeNecessario(c) }
        }
    }

    private fun limparFormulario() {
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

    private fun abrirCalendario() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            edtValidade.setText("%02d/%02d/%04d".format(d, m + 1, y))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun converterData(raw: String?): String? =
        try {
            raw?.split("/")?.let {
                "%04d-%02d-%02d".format(it[2].toInt(), it[1].toInt(), it[0].toInt())
            }
        } catch (_: Exception) { null }
    private fun dataParaTela(iso: String?): String? {
        return try {
            iso?.split("-")?.let {
                "%02d/%02d/%04d".format(
                    it[2].toInt(),
                    it[1].toInt(),
                    it[0].toInt()
                )
            }
        } catch (_: Exception) {
            null
        }
    }
    private fun estoqueParaTela(
        total: Int?,
        qtdPorCaixa: Int?
    ): Pair<Int, Int> {
        if (total == null || qtdPorCaixa == null || qtdPorCaixa <= 0) {
            return 0 to (total ?: 0)
        }

        val caixas = total / qtdPorCaixa
        val unidades = total % qtdPorCaixa
        return caixas to unidades
    }


}
