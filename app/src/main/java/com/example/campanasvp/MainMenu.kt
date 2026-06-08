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
    private var cargandoDatosGuardados = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        viewPager.offscreenPageLimit = 3
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        // Limpiar formulario al tocar el tab 3, salvo que vengas de abrirFormularioConDatos
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                if (position == 3) {
                    if (!cargandoDatosGuardados) {
                        val f = supportFragmentManager.findFragmentByTag(
                            "android:switcher:${R.id.view_pager}:3"
                        ) as? FormularioFragment
                        f?.webView?.evaluateJavascript("limpiarFormulario()", null)
                    }
                    cargandoDatosGuardados = false
                }
            }
            override fun onPageScrolled(p: Int, o: Float, px: Int) {}
            override fun onPageScrollStateChanged(s: Int) {}
        })
    }

    fun abrirFormularioConDatos(inspeccion: Inspeccion) {
        // Marcar que venimos de un registro guardado para no limpiar
        cargandoDatosGuardados = true

        // Cambiar al tab del formulario (tab 3)
        binding.viewPager.currentItem = 3

        // Esperar a que el WebView esté listo y cargar los datos
        binding.viewPager.postDelayed({
            val formularioFragment = supportFragmentManager.findFragmentByTag(
                "android:switcher:${R.id.view_pager}:3"
            ) as? FormularioFragment

            formularioFragment?.let { fragment ->
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
                        "informacionDisp": "${escaparJson(inspeccion.informacionDisp)}",
                        "urlfoto1": "${escaparJson(inspeccion.urlfoto1)}",
                        "urlfoto2": "${escaparJson(inspeccion.urlfoto2)}",
                        "urlfoto3": "${escaparJson(inspeccion.urlfoto3)}"
                    }
                """.trimIndent()

                fragment.webView?.evaluateJavascript("cargarDatosFormulario($json)", null)

                // Cargar previews de fotos después de que el formulario cargue
                binding.viewPager.postDelayed({
                    val f = supportFragmentManager.findFragmentByTag(
                        "android:switcher:${R.id.view_pager}:3"
                    ) as? FormularioFragment
                    if (inspeccion.urlfoto1.isNotEmpty()) f?.cargarFotoDesdeRuta(inspeccion.urlfoto1, 1)
                    if (inspeccion.urlfoto2.isNotEmpty()) f?.cargarFotoDesdeRuta(inspeccion.urlfoto2, 2)
                    if (inspeccion.urlfoto3.isNotEmpty()) f?.cargarFotoDesdeRuta(inspeccion.urlfoto3, 3)
                }, 800)
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