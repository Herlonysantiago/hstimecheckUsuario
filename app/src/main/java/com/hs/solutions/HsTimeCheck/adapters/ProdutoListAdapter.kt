package com.hs.solutions.Hstimecheck.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hs.solutions.Hstimecheck.R
import com.hs.solutions.Hstimecheck.models.Produto

class ProdutoListAdapter(
    private val produtos: MutableList<Produto>
) : RecyclerView.Adapter<ProdutoListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescricao: TextView = view.findViewById(R.id.tvDescricao)
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val tvValidade: TextView = view.findViewById(R.id.tvValidade)
        val tvQuantidade: TextView = view.findViewById(R.id.tvQuantidade)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = produtos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = produtos[position]
        holder.tvDescricao.text = p.descricao
        holder.tvCodigo.text = "Código: ${p.codigoBarras}"
        holder.tvValidade.text = "Validade: ${p.validadeAtual ?: "—"}"
        holder.tvQuantidade.text = "Qtde: ${p.quantidadeAtual ?: "—"}"
        holder.tvStatus.text = p.status.name
    }

    fun update(newList: List<Produto>) {
        produtos.clear()
        produtos.addAll(newList)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Produto = produtos[position]
}
