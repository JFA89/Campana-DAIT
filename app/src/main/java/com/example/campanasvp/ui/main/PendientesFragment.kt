package com.example.campanasvp.ui.main

import android.app.AlertDialog
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

    fun cargarLista() {
        val inspecciones = dbHelper.obtenerPorEstado("PENDIENTE")
        adapter = InspeccionAdapter(inspecciones) { inspeccion ->
            AlertDialog.Builder(requireContext())
                .setTitle("Inspección #${inspeccion.id}")
                .setMessage("${inspeccion.empresa}\n${inspeccion.localidad} — ${inspeccion.fechaCarga}\n${inspeccion.inspector}")
                .setPositiveButton("Ir al formulario") { _, _ ->
                    (activity as? MainMenu)?.abrirFormularioConDatos(inspeccion)
                }
                .setNegativeButton("Eliminar") { _, _ ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Eliminar la inspección #${inspeccion.id}?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            dbHelper.eliminar(inspeccion.id)
                            cargarLista()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                .setNeutralButton("Cancelar", null)
                .show()
        }
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarLista() // refresca al volver a la pestaña
    }
}