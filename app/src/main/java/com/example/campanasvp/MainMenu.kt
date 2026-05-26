package com.example.campanasvp

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.example.campanasvp.ui.main.SectionsPagerAdapter
import com.example.campanasvp.ui.main.FormularioFragment
import com.example.campanasvp.databinding.ActivityMainMenuBinding

class MainMenu : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        viewPager.offscreenPageLimit = 3   // mantiene los 4 fragments en memoria
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
    }

    fun abrirFormularioConDatos(inspeccion: Inspeccion) {
        // 1. Cambiar al tab del formulario (tab 3)
        binding.viewPager.currentItem = 3

        // 2. Esperar a que el WebView esté listo y cargar los datos
        binding.viewPager.postDelayed({
            val formularioFragment = supportFragmentManager.findFragmentByTag("android:switcher:${R.id.view_pager}:3") as? FormularioFragment
            formularioFragment?.let { fragment ->
                // Escapar comillas y saltos de línea en los strings
                val json = """
                    {
                        "id": ${inspeccion.id},
                        "coordenadas": "${escaparJson(inspeccion.coordenadas)}",
                        "estado": "${escaparJson(inspeccion.estado)}",
                        "recrel": "${escaparJson(inspeccion.recrel)}",
                        "fechaCarga": "${escaparJson(inspeccion.fechaCarga)}",
                        "empresa": "${escaparJson(inspeccion.empresa)}",
                        "Partido": "${escaparJson(inspeccion.partido)}",
                        "Localidad": "${escaparJson(inspeccion.localidad)}",
                        "sucursal": "${escaparJson(inspeccion.sucursal)}",
                        "zona": "${escaparJson(inspeccion.zona)}",
                        "calle": "${escaparJson(inspeccion.calle)}",
                        "num_calle": "${escaparJson(inspeccion.numCalle)}",
                        "entreCalle": "${escaparJson(inspeccion.entreCalle)}",
                        "inspector": "${escaparJson(inspeccion.inspector)}",
                        "descripcion": "${escaparJson(inspeccion.descripcion)}",
                        "presencia": "${escaparJson(inspeccion.presencia)}",
                        "normativa": "${escaparJson(inspeccion.normativa)}",
                        "conclusion": "${escaparJson(inspeccion.conclusion)}",
                        "instalacion": "${escaparJson(inspeccion.instalacion)}",
                        "informacionDisp": "${escaparJson(inspeccion.informacionDisp)}"
                    }
                """.trimIndent()

                fragment.webView?.evaluateJavascript("cargarDatosFormulario($json)", null)
            }
        }, 500)
    }

    private fun escaparJson(texto: String): String {
        return texto
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}