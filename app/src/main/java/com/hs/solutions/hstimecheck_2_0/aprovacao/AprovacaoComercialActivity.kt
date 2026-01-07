package com.hs.solutions.hstimecheck_2_0.aprovacao

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import android.widget.ImageView
import com.hs.solutions.hstimecheck_2_0.ui.*
import com.hs.solutions.hstimecheck_2_0.core.*
class AprovacaoComercialActivity : AppCompatActivity() {

    private lateinit var adapter: AprovacaoAdapter
    private lateinit var recyclerView: RecyclerView
    private val listaExibida = mutableListOf<AprovacaoItem>()

    private val productService by lazy { AppContainer.productService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aprovacao_comercial)

        recyclerView = findViewById(R.id.recyclerViewAprovacao)

        lifecycleScope.launch {
            productService.carregar()
            carregarDados()
            configurarAdapter()
        }
    }

    private suspend fun carregarDados() {
        val produtos = productService.produtos.first()

        listaExibida.clear()
        listaExibida.addAll(
            produtos
                .filter { it.status == StatusProduto.AGUARDANDO_APROVACAO }
                .map { p ->
                    val precoAtual = p.precoAtual ?: 0.0
                    AprovacaoItem(
                        id = p.id,
                        descricao = p.descricao,
                        codigoInterno = p.codigoInterno,
                        codigoBarras = p.codigoBarras,
                        validade = p.validadeAtual,
                        precoAtual = precoAtual,
                        precoSugerido = (precoAtual - 2.0).coerceAtLeast(0.0)
                    )

                }
        )
    }

    private fun configurarAdapter() {
        adapter = AprovacaoAdapter(
            listaExibida,

            // ✅ APROVAR — exatamente como antes
            onAprovar = { item ->
                abrirDialogAprovarPreco(item)
            }
            ,

            // ✅ REJEITAR — exatamente como antes
            onRejeitar = { item ->
                lifecycleScope.launch {
                    val produto = productService.getProdutoById(item.id) ?: return@launch

                    productService.rejeitarComercial(
                        produto = produto,
                        motivo = "Preço rejeitado na aprovação comercial"
                    )

                    Toast.makeText(
                        this@AprovacaoComercialActivity,
                        "Preço rejeitado",
                        Toast.LENGTH_SHORT
                    ).show()

                    carregarDados()
                    adapter.notifyDataSetChanged()
                }
            },

            // ✏️ EDITAR — já estava correto
            onEditar = { item ->
                abrirDialogEditarPreco(item)
            },

            // 👆 Clique simples → código de barras
            onClickCodigoBarras = { codigo ->
                abrirCodigoBarras(codigo)
            },

            // 👆👆 Clique longo → WhatsApp
            onLongClickWhatsapp = { item ->
                abrirDialogWhatsapp(item)
            }
        )

        recyclerView.adapter = adapter
    }

    private fun abrirCodigoBarras(codigo: String) {
        val intent = Intent(this, FullImageActivity::class.java)
        intent.putExtra("isBarcode", true)
        intent.putExtra("barcodeContent", codigo)
        startActivity(intent)
    }


    private fun abrirDialogEditarPreco(item: AprovacaoItem) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val edtAtual = EditText(this).apply {
            hint = "Preço atual"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(item.precoAtual.toString())
        }

        val edtSugerido = EditText(this).apply {
            hint = "Preço sugerido"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(item.precoSugerido.toString())
        }

        layout.addView(edtAtual)
        layout.addView(edtSugerido)

        AlertDialog.Builder(this)
            .setTitle("Editar preços")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val novoAtual = edtAtual.text.toString().toDoubleOrNull()
                val novoSugerido = edtSugerido.text.toString().toDoubleOrNull()

                if (novoAtual == null || novoSugerido == null) {
                    Toast.makeText(this, "Valores inválidos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                item.precoAtual = novoAtual
                item.precoSugerido = novoSugerido

                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun abrirDialogWhatsapp(item: AprovacaoItem) {

        val mensagem = buildString {
            append("Ola?\n\n")
            append("Ja tivemos retorno do comprodor? \n\n")
            append("Produto: ${item.descricao}\n")
            append("Código interno: ${item.codigoInterno}\n")
            append("Validade: ${item.validade}\n")
            append("Preço sugerido: R$ %.2f\n\n".format(item.precoSugerido))
            append("HS")
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://wa.me/?text=${URLEncoder.encode(mensagem, "UTF-8")}"
            )
        }

        startActivity(intent)
    }
    private fun abrirDialogAprovarPreco(item: AprovacaoItem) {

        val edtPrecoAprovado = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Preço aprovado"
            setText(item.precoSugerido.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Aprovar preço")
            .setMessage("Informe o preço aprovado")
            .setView(edtPrecoAprovado)
            .setPositiveButton("Aprovar") { _, _ ->

                val precoAprovado = edtPrecoAprovado.text
                    .toString()
                    .toDoubleOrNull()

                if (precoAprovado == null) {
                    Toast.makeText(
                        this,
                        "Preço inválido",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val produto = productService.getProdutoById(item.id)
                        ?: return@launch

                    productService.aprovarComercial(
                        produto = produto,
                        precoAprovado = precoAprovado
                    )

                    Toast.makeText(
                        this@AprovacaoComercialActivity,
                        "Preço aprovado com sucesso",
                        Toast.LENGTH_SHORT
                    ).show()

                    carregarDados()
                    adapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}

