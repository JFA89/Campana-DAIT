package com.example.campanasvp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InspeccionAdapter(
    private var inspecciones: List<Inspeccion>,
    private val onItemClick: (Inspeccion) -> Unit
) : RecyclerView.Adapter<InspeccionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tvId)
        val tvEmpresa: TextView = itemView.findViewById(R.id.tvEmpresa)
        val tvPartidoLocalidad: TextView = itemView.findViewById(R.id.tvPartidoLocalidad)
        val tvCalle: TextView = itemView.findViewById(R.id.tvCalle)
        val tvFechaCarga: TextView = itemView.findViewById(R.id.tvFechaCarga)
        val tvConclusion: TextView = itemView.findViewById(R.id.tvConclusion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inspeccion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = inspecciones[position]
        holder.tvId.text = "ID: ${item.id}"
        holder.tvEmpresa.text = "Empresa: ${item.empresa}"
        holder.tvPartidoLocalidad.text = "Partido: ${item.partido} - Localidad: ${item.localidad}"
        holder.tvCalle.text = "Calle: ${item.calle} ${item.numCalle}"
        holder.tvFechaCarga.text = "Fecha: ${item.fechaCarga}"
        holder.tvConclusion.text = "Conclusión: ${item.conclusion}"

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = inspecciones.size

    fun updateList(newList: List<Inspeccion>) {
        inspecciones = newList
        notifyDataSetChanged()
    }
}