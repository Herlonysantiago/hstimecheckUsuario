package com.hs.solutions.Hstimecheck.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hs.solutions.Hstimecheck.R
import com.hs.solutions.Hstimecheck.models.Produto

class ProdutoCardAdapter(
    private val produtos: MutableList<Produto>
) : RecyclerView.Adapter<ProdutoCardAdapter.ViewHolder>() {

    inner class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val imgProduto: ImageView = item.findViewById(R.id.imgProduto)
        val tvDescricao: TextView = item.findViewById(R.id.tvDescricaoCard)
        val tvValidade: TextView = item.findViewById(R.id.tvValidadeCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto_card, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = produtos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = produtos[position]

        holder.tvDescricao.text = p.descricao
        holder.tvValidade.text = "Validade: ${p.validadeAtual ?: "—"}"

        val url = obterUrlImagem(p.codigoBarras)

        Glide.with(holder.itemView.context)
            .load(url)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.imgProduto)
    }

    private fun obterUrlImagem(cod: String): String {
        return "https://images.openfoodfacts.org/images/products/$cod/front_pt.400.jpg"
    }

    fun getItem(position: Int): Produto = produtos[position]

    fun update(newList: List<Produto>) {
        produtos.clear()
        produtos.addAll(newList)
        notifyDataSetChanged()
    }
}
