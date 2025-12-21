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
                        codigo = p.codigoBarras,
                        precoAtual = precoAtual,
                        precoSugerido = (precoAtual - 2.0).coerceAtLeast(0.0)
                    )
                }
        )
    }

    private fun configurarAdapter() {
        adapter = AprovacaoAdapter(
            listaExibida,

            // ✔ APROVAR
            onAprovar = { item ->
                val produto = productService.getProdutoById(item.id) ?: return@AprovacaoAdapter
                val edtPrecoAprovado = EditText(this).apply {
                    hint = "Preço aprovado"
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    setText(item.precoSugerido.toString())
                }

                AlertDialog.Builder(this)
                    .setTitle("Aprovação Comercial")
                    .setView(edtPrecoAprovado)
                    .setPositiveButton("Aprovar", null)
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .also { dialog ->
                        dialog.setOnShowListener {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setOnClickListener {

                                    val precoAprovado =
                                        edtPrecoAprovado.text.toString().toDoubleOrNull()

                                    if (precoAprovado == null) {
                                        Toast.makeText(
                                            this,
                                            "Preço inválido",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@setOnClickListener
                                    }

                                    dialog.dismiss()

                                    lifecycleScope.launch {
                                        productService.aprovarComercial(
                                            produto = produto,
                                            precoAprovado = precoAprovado,

                                        )


                                        listaExibida.remove(item)
                                        adapter.notifyDataSetChanged()

                                        if (listaExibida.isEmpty()) {
                                            Toast.makeText(
                                                this@AprovacaoComercialActivity,
                                                "Nenhum produto aguardando aprovação",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                        }
                    }
                    .show()
            },

            // ✔ REJEITAR
            onRejeitar = { item ->
                val produto = productService.getProdutoById(item.id) ?: return@AprovacaoAdapter

                lifecycleScope.launch {
                    productService.rejeitarComercial(produto, "Rejeitado na aprovação")
                    listaExibida.remove(item)
                    adapter.notifyDataSetChanged()
                }
            },

            // ✔ EDITAR
            onEditar = { item ->
                abrirDialogEditarPreco(item)
            }
        )

        recyclerView.adapter = adapter
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
}

