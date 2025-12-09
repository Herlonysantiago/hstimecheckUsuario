package com.hs.solutions.hstimecheck.paineldecontrole

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hs.solutions.hstimecheck.R

class IndicadoresAdapter(
    private val lista: List<IndicadorItem>,
    private val onClick: (IndicadorItem) -> Unit
) : RecyclerView.Adapter<IndicadoresAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icone: ImageView = itemView.findViewById(R.id.imgIcone)
        val titulo: TextView = itemView.findViewById(R.id.txtTitulo)
        val valor: TextView = itemView.findViewById(R.id.txtValor)

        init {
            itemView.setOnClickListener {
                onClick(lista[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_indicador, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.titulo.text = item.titulo
        holder.valor.text = item.valor
        holder.icone.setImageResource(item.icone)
    }
}
