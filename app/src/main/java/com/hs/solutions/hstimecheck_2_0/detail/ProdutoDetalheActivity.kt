package com.hs.solutions.hstimecheck_2_0.detail

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import java.text.SimpleDateFormat
import java.util.*
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import android.content.Intent
import android.util.Log
import com.hs.solutions.hstimecheck_2_0.ui.FullImageActivity   // ← IMPORT NECESSÁRIO

class ProdutoDetalheActivity : AppCompatActivity() {

    private var produto: Produto? = null

    private lateinit var img: ImageView
    private lateinit var tvDescricao: TextView
    private lateinit var tvCodigo: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvValidade: TextView
    private lateinit var tvQuantidade: TextView
    private lateinit var tvPreco: TextView
    private lateinit var btnMenu: ImageButton
    private lateinit var btnValidades: Button
    private lateinit var btnHistorico: Button
    private val productService = AppContainer.productService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produto_detalhe)
        val btn = findViewById<Button?>(R.id.btnSalvar)
        Log.d("TESTE_SALVAR", "BOTÃO SALVAR ENCONTRADO? -> $btn")

        val id = intent.getStringExtra("id") ?: return
        produto = productService.getProdutoById(id)

        if (produto == null) {
            Toast.makeText(this, "Erro: Produto não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        preencherDados()
        configurarAcoes()
    }

    private fun initViews() {
        img = findViewById(R.id.imgProduto)
        tvDescricao = findViewById(R.id.tvDescricao)
        tvCodigo = findViewById(R.id.tvCodigo)
        tvStatus = findViewById(R.id.tvStatus)
        tvValidade = findViewById(R.id.tvValidade)
        tvQuantidade = findViewById(R.id.tvQuantidade)
        tvPreco = findViewById(R.id.tvPreco)
        btnMenu = findViewById(R.id.btnMenu)
        btnValidades = findViewById(R.id.btnIrValidades)
        btnHistorico = findViewById(R.id.btnIrHistorico)
    }

    private fun preencherDados() {
        val p = produto ?: return

        tvDescricao.text = p.descricao
        tvCodigo.text = "Código: ${p.codigoBarras}"
        tvStatus.text = "Status: ${p.status}"
        tvValidade.text = "Validade: ${p.validadeAtual ?: "—"}"
        tvQuantidade.text = "Quantidade: ${p.quantidadeAtual ?: "—"}"
        tvPreco.text = "Preço: R$ ${p.precoAtual ?: 0.0}"

        val url = p.fotoUrl ?: "https://images.openfoodfacts.org/images/products/${p.codigoBarras}/front_pt.400.jpg"

        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.ic_placeholder)
            .into(img)
    }

    private fun configurarAcoes() {

        btnValidades.setOnClickListener {
            Toast.makeText(this, "Tela de validades será implementada", Toast.LENGTH_SHORT).show()
        }

        btnHistorico.setOnClickListener {
            Toast.makeText(this, "Tela de histórico será implementada", Toast.LENGTH_SHORT).show()
        }

        btnMenu.setOnClickListener {
            abrirMenu()
        }

        // -------------------------------------------------------
        //    FOTO EM TELA CHEIA  (ADICIONADO SEM REMOVER NADA)
        // -------------------------------------------------------
        img.setOnClickListener {
            val p = produto ?: return@setOnClickListener

            val foto = p.fotoUrl
            if (foto.isNullOrBlank()) {
                Toast.makeText(this, "Produto sem foto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, FullImageActivity::class.java)
            intent.putExtra("fotoUrl", foto)
            startActivity(intent)
        }
        // -------------------------------------------------------
    }

    private fun abrirMenu() {
        val p = produto ?: return

        val opcoes = arrayOf(
            "Ver Validades",
            "Ver Histórico",
            "Adicionar novo alerta",
            "Enviar para aprovação",
            "Enviar para rebaixa",
            "Enviar para verificação"
        )

        AlertDialog.Builder(this)
            .setTitle("Ações")
            .setItems(opcoes) { _, which ->
                when (opcoes[which]) {
                    "Ver Validades" ->
                        Toast.makeText(this, "Abrir validades", Toast.LENGTH_SHORT).show()

                    "Ver Histórico" ->
                        Toast.makeText(this, "Abrir histórico", Toast.LENGTH_SHORT).show()

                    "Adicionar novo alerta" ->
                        Toast.makeText(this, "Funcionalidade futura", Toast.LENGTH_SHORT).show()

                    "Enviar para aprovação" ->
                        alterarStatus(StatusProduto.AGUARDANDO_APROVACAO)

                    "Enviar para rebaixa" ->
                        alterarStatus(StatusProduto.TRABALHANDO_PRECO)

                    "Enviar para verificação" ->
                        alterarStatus(StatusProduto.VERIFICACAO_ESTOQUE)
                }
            }
            .show()
    }

    private fun alterarStatus(novoStatus: StatusProduto) {
        val p = produto ?: return
        val antigo = p.status

        p.status = novoStatus
        adicionarHistorico("Status alterado", "De $antigo para $novoStatus")

        Toast.makeText(this, "Status atualizado", Toast.LENGTH_SHORT).show()
        preencherDados()
    }

    private fun adicionarHistorico(evento: String, detalhe: String?) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val data = sdf.format(Date())

        produto?.historico?.add(
            com.hs.solutions.hstimecheck_2_0.models.HistoricoItem(
                dataEvento = data,
                evento = evento,
                detalhe = detalhe,
                quantidade = produto?.quantidadeAtual,
                preco = produto?.precoAtual
            )
        )
    }
}
