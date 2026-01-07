package com.hs.solutions.hstimecheck_2_0.aprovacao

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.hs.solutions.hstimecheck_2_0.R
class AprovacaoAdapter(
    private val lista: List<AprovacaoItem>,
    private val onAprovar: (AprovacaoItem) -> Unit,
    private val onRejeitar: (AprovacaoItem) -> Unit,
    private val onEditar: (AprovacaoItem) -> Unit,
    private val onClickCodigoBarras: (String) -> Unit,
    private val onLongClickWhatsapp: (AprovacaoItem) -> Unit
) : RecyclerView.Adapter<AprovacaoAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtDescricao: TextView = itemView.findViewById(R.id.txtDescricao)
        val txtCodigoInterno: TextView = itemView.findViewById(R.id.txtCodigoInterno)
        val txtValidade: TextView = itemView.findViewById(R.id.txtValidade)
        val txtPrecoAtual: TextView = itemView.findViewById(R.id.txtPrecoAtual)
        val txtPrecoSugerido: TextView = itemView.findViewById(R.id.txtPrecoSugerido)
        val txtDiferenca: TextView = itemView.findViewById(R.id.txtDiferenca)
        val btnAprovar: MaterialButton = itemView.findViewById(R.id.btnAprovar)
        val btnRejeitar: MaterialButton = itemView.findViewById(R.id.btnRejeitar)
        val btnEditar: MaterialButton = itemView.findViewById(R.id.btnEditar)

        fun bind(item: AprovacaoItem) {

            txtDescricao.text = item.descricao
            txtCodigoInterno.text = "Código interno: ${item.codigoInterno ?: "-"}"
            txtValidade.text = "Validade: ${item.validade ?: "-"}"

            txtPrecoAtual.text = "Preço atual: R$ %.2f".format(item.precoAtual)
            txtPrecoSugerido.text = "Sugerido: R$ %.2f".format(item.precoSugerido)

            val diff = item.diferencaPercentual
            txtDiferenca.text = "$diff%"
            txtDiferenca.setTextColor(
                if (diff < 0)
                    itemView.context.getColor(android.R.color.holo_red_dark)
                else
                    itemView.context.getColor(android.R.color.holo_green_dark)
            )

            // 👆 CLIQUE SIMPLES → imagem do código de barras

            itemView.setOnClickListener {
                item.codigoBarras?.let { codigo ->
                    onClickCodigoBarras(codigo)
                }
            }


            // 👆👆 CLIQUE LONGO → WhatsApp pedindo preço melhor
            itemView.setOnLongClickListener {
                onLongClickWhatsapp(item)
                true // importante: consome o evento
            }

            btnAprovar.setOnClickListener { onAprovar(item) }
            btnRejeitar.setOnClickListener { onRejeitar(item) }
            btnEditar.setOnClickListener { onEditar(item) }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aprovacao_comercial, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.bind(item)
    }
}
