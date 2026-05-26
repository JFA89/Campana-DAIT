package com.example.campanasvp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campanasvp.DBHelper
import com.example.campanasvp.Inspeccion
import com.example.campanasvp.InspeccionAdapter
import com.example.campanasvp.MainMenu
import com.example.campanasvp.R

class PendientesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InspeccionAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista_inspecciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerViewInspecciones)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dbHelper = DBHelper(requireContext())
        cargarLista()
    }

    private fun cargarLista() {
        val inspecciones = dbHelper.obtenerPorEstado("PENDIENTE") // nuevo método
        adapter = InspeccionAdapter(inspecciones) { inspeccion ->
            // Al tocar un item, llamar a MainMenu para abrir el formulario con los datos
            (activity as? MainMenu)?.abrirFormularioConDatos(inspeccion)
        }
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarLista() // refresca al volver a la pestaña
    }
}