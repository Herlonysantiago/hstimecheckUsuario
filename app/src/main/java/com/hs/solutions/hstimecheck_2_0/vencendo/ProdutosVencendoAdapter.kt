package com.hs.solutions.hstimecheck_2_0.vencendo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.models.Produto
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.hs.solutions.hstimecheck_2_0.core.DateFormatter
import android.content.Context
class ProdutosVencendoAdapter(
    private val onAprovacao: (Produto) -> Unit,
    private val onPreco: (Produto) -> Unit,
    private val onExcluir: (Produto, Context) -> Unit
) : RecyclerView.Adapter<ProdutosVencendoAdapter.ViewHolder>() {

    private val lista = mutableListOf<Produto>()

    fun atualizar(novaLista: List<Produto>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto_vencendo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txtNome: TextView = itemView.findViewById(R.id.txtNomeProduto)
        private val txtValidade: TextView = itemView.findViewById(R.id.txtValidade)
        private val txtDias: TextView = itemView.findViewById(R.id.txtDias)
        private val txtEstoque: TextView = itemView.findViewById(R.id.txtEstoque)

        private val btnAprovacao: Button = itemView.findViewById(R.id.btnAprovacao)
        private val btnPreco: Button = itemView.findViewById(R.id.btnPreco)
        private val btnExcluir: Button = itemView.findViewById(R.id.btnExcluir)

        fun bind(produto: Produto) {

            txtNome.text = produto.descricao
            txtValidade.text =
                "Validade: ${DateFormatter.isoParaBr(produto.validadeAtual)}"

            txtEstoque.text = "Qtd: ${produto.quantidadeAtual ?: 0}"

            val validade = produto.validadeAtual
            if (validade != null) {
                val dias = ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    LocalDate.parse(validade)
                )
                txtDias.text = "Dias restantes: $dias"

                itemView.setBackgroundColor(
                    if (dias <= 1)
                        Color.parseColor("#FFE0B2")
                    else
                        Color.parseColor("#FFF9C4")
                )
            } else {
                txtDias.text = "Sem validade"
            }

            btnAprovacao.setOnClickListener { onAprovacao(produto) }
            btnPreco.setOnClickListener { onPreco(produto) }
            btnExcluir.setOnClickListener {
                onExcluir(produto, itemView.context)
            }

        }
    }
}
