package com.hs.solutions.hstimecheck.cadastro

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
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
    private lateinit var lookup: ProductLookupService

    private lateinit var edtCodigoBarras: EditText
    private lateinit var edtCodigoInterno: EditText
    private lateinit var edtDescricao: EditText
    private lateinit var edtValidade: EditText
    private lateinit var edtPreco: EditText

    private lateinit var edtCaixa: EditText
    private lateinit var edtUnidade: EditText

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

        Log.e("CADASTRO", ">>> onCreate executou <<<")

        lookup = ProductLookupService(this)

        produtoId = intent.getStringExtra("produto_id")   // se vier ID → é edição
        codigoBarras = intent.getStringExtra("codigo_barras") ?: ""

        initViews()


        // Se está editando um produto existente → mostra botão EXCLUIR
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


    /* -------------------------------------------------------------------------------------- */
    /*   EXIBIR PRODUTO EXISTENTE PARA EDIÇÃO                                                 */
    /* -------------------------------------------------------------------------------------- */

    private fun carregarProdutoExistente(id: String) {
        val produto = productService.produtos.value.find { it.id == id } ?: return

        edtCodigoBarras.setText(produto.codigoBarras)
        edtCodigoInterno.setText(produto.codigoInterno ?: "")
        edtDescricao.setText(produto.descricao)
        edtValidade.setText(produto.validadeAtual ?: "")
        edtPreco.setText(produto.precoAtual?.toString() ?: "")
        edtCaixa.setText("")     // ajustar se usar caixa/und no modelo principal
        edtUnidade.setText(produto.quantidadeAtual?.toString() ?: "0")
    }


    /* -------------------------------------------------------------------------------------- */
    /*   BOTÃO EXCLUIR                                                                        */
    /* -------------------------------------------------------------------------------------- */

    private fun setupButtonExcluir() {

        btnExcluir.setOnClickListener {

            val id = produtoId ?: return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Excluir produto")
                .setMessage("Tem certeza que deseja excluir este produto?")
                .setPositiveButton("Excluir") { _, _ ->

                    Log.e("EXCLUIR", "Excluindo produto ID: $id")

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


    /* -------------------------------------------------------------------------------------- */
    /*   CONTORNO MOTOROLA G24                                                                */
    /* -------------------------------------------------------------------------------------- */




    /* -------------------------------------------------------------------------------------- */
    /*   INICIALIZAR VIEWS                                                                    */
    /* -------------------------------------------------------------------------------------- */

    private fun initViews() {
        edtCodigoBarras = findViewById(R.id.edtCodigoBarras)
        edtCodigoInterno = findViewById(R.id.edtCodigoInterno)
        edtDescricao = findViewById(R.id.edtDescricao)
        edtValidade = findViewById(R.id.edtValidade)
        edtPreco = findViewById(R.id.edtPreco)

        edtCaixa = findViewById(R.id.edtCaixa)
        edtUnidade = findViewById(R.id.edtUnidade)

        imgProduto = findViewById(R.id.imgProduto)
        btnFoto = findViewById(R.id.btnFoto)

        btnSalvar = findViewById(R.id.btnSalvar)
        btnExcluir = findViewById(R.id.btnExcluir)
        btnScan = findViewById(R.id.btnScan)
        btnLimpar = findViewById(R.id.btnLimpar)
    }


    private fun preencherCodigoBarras() {
        edtCodigoBarras.setText(codigoBarras)
    }


    /* -------------------------------------------------------------------------------------- */
    /*   CALENDÁRIO                                                                           */
    /* -------------------------------------------------------------------------------------- */

    private fun abrirCalendario() {
        val hoje = java.util.Calendar.getInstance()
        val ano = hoje.get(java.util.Calendar.YEAR)
        val mes = hoje.get(java.util.Calendar.MONTH)
        val dia = hoje.get(java.util.Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, y, m, d ->
                edtValidade.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
            },
            ano, mes, dia
        ).show()
    }


    /* -------------------------------------------------------------------------------------- */
    /*   CARREGAR COMPLEMENTO DO JSON PADRÃO                                                  */
    /* -------------------------------------------------------------------------------------- */

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


    /* -------------------------------------------------------------------------------------- */
    /*   SALVAR PRODUTO                                                                       */
    /* -------------------------------------------------------------------------------------- */

    private fun setupButtonSalvar() {

        btnSalvar.setOnClickListener {
            Toast.makeText(this, "CLIQUE DETECTADO!", Toast.LENGTH_LONG).show()
            Log.e("SALVAR_TESTE", "Clique DETECTADO no botão SALVAR!")
            Log.e("SALVAR", "Clique recebido no botão SALVAR")

            val descricao = edtDescricao.text.toString().trim()
            val codigoInterno = edtCodigoInterno.text.toString().trim()
            val validadeString = edtValidade.text.toString().trim()
            val preco = edtPreco.text.toString().toDoubleOrNull()

            val caixas = edtCaixa.text.toString().toIntOrNull() ?: 0
            val unidades = edtUnidade.text.toString().toIntOrNull() ?: 0
            val quantidadeTotal = caixas + unidades

            if (descricao.isBlank()) {
                Toast.makeText(this, "Informe a descrição!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novo = Produto(
                id = produtoId ?: java.util.UUID.randomUUID().toString(),
                codigoBarras = edtCodigoBarras.text.toString(),
                codigoInterno = codigoInterno.ifBlank { null },
                descricao = descricao,
                quantidadeAtual = quantidadeTotal,
                validadeAtual = converterData(validadeString),
                precoAtual = preco,
                status = StatusProduto.NORMAL
            )

            Log.e("SALVAR", "Produto criado: $novo")

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


    /* -------------------------------------------------------------------------------------- */
    /*   EXTRA: SCAN / LIMPAR                                                                  */
    /* -------------------------------------------------------------------------------------- */

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
        }
    }


    /* ---------------------------- SCANNER RETURN --------------------------------- */

    private val launcherScanner = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val codigo = result.data?.getStringExtra("codigo_barras") ?: ""
            val descricao = result.data?.getStringExtra("descricao") ?: ""
            val interno = result.data?.getStringExtra("codigo_interno") ?: ""

            Log.e("SCANNER", "Recebido do scanner: código=$codigo desc=$descricao interno=$interno")

            edtCodigoBarras.setText(codigo)
            edtDescricao.setText(descricao)
            edtCodigoInterno.setText(interno)

            codigoBarras = codigo

            carregarDadosJson()
        }
    }
}
