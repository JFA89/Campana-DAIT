package com.example.campanasvp

import android.content.ContentValues
import android.content.Context
import android.webkit.JavascriptInterface
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import java.security.cert.X509Certificate

class AndroidBridge(private val context: Context) {

    private val db = DBHelper(context).writableDatabase

    @JavascriptInterface
    fun guardar(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            val values = armarValues(json, "GUARDADO")
            val id = db.insert("inspecciones", null, values)
            """{"ok": true, "id": $id}"""
        } catch (e: Exception) {
            """{"ok": false, "error": "${e.message}"}"""
        }
    }

    @JavascriptInterface
    fun enviar(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)

            // 1. Guardar en SQLite como PENDIENTE
            val values = armarValues(json, "PENDIENTE")
            val id = db.insert("inspecciones", null, values)

            // 2. Intentar enviar al servidor
            val empresa   = URLEncoder.encode(json.optString("empresa"), "UTF-8")
            val recrel    = URLEncoder.encode(json.optString("recrel"), "UTF-8")
            val fechaCarga     = URLEncoder.encode(json.optString("fechaCarga"), "UTF-8")
            val Partido     = URLEncoder.encode(json.optString("Partido"), "UTF-8")
            val Localidad     = URLEncoder.encode(json.optString("Localidad"), "UTF-8")
            val sucursal     = URLEncoder.encode(json.optString("sucursal"), "UTF-8")
            val zona     = URLEncoder.encode(json.optString("zona"), "UTF-8")
            val calle     = URLEncoder.encode(json.optString("calle"), "UTF-8")
            val num_calle     = URLEncoder.encode(json.optString("num_calle"), "UTF-8")
            val entreCalle     = URLEncoder.encode(json.optString("entreCalle"), "UTF-8")
            val inspector = URLEncoder.encode(json.optString("inspector"), "UTF-8")
            val descripcion = URLEncoder.encode(json.optString("descripcion"), "UTF-8")
            val presencia = URLEncoder.encode(json.optString("presencia"), "UTF-8")
            val normativa = URLEncoder.encode(json.optString("normativa"), "UTF-8")
            val conclusion = URLEncoder.encode(json.optString("conclusion"), "UTF-8")
            val instalacion = URLEncoder.encode(json.optString("instalacion"), "UTF-8")
            val informacionDisp = URLEncoder.encode(json.optString("informacionDisp"), "UTF-8")
            val fotos = "N"//FOTOS S/N este despues preguntar por las url guardadas si estan vacias es N sino S
            val coordenadas = URLEncoder.encode(json.optString("coordenadas"), "UTF-8")
            //fechaCreacion






            val url = "http://www.enre.gov.ar/InspeccionesGAP.nsf/RecibirFormulario?openagent" +
                    "&empresa=$empresa" +
                    "&recrel=$recrel" +
                    "&fecha=$fechaCarga" +
                    "&Partido=$Partido" +
                    "&Localidad=$Localidad" +
                    "&sucursal=$sucursal" +
                    "&zona=$zona" +
                    "&calle=$calle" +
                    "&num_calle=$num_calle" +
                    "&entreCalle=$entreCalle" +
                    "&inspector=$inspector" +
                    "&descripcion=$descripcion" +
                    "&presencia=$presencia" +
                    "&normativa=$normativa" +
                    "&conclusion=$conclusion" +
                    "&instalacion=$instalacion" +
                    "&informacionDisp=$informacionDisp" +
                    "&fotos=$fotos" +
                    "&coordenadas=$coordenadas"

            val respuesta = hacerGet(url)

            if (respuesta.trim() == "OK") {
                // 3. Si el servidor responde OK, actualizar estado a ENVIADO
                DBHelper(context).actualizarEstado(id, "ENVIADO")
                """{"ok": true, "id": $id, "estado": "ENVIADO"}"""
            } else {
                // Quedó como PENDIENTE, se reintentará luego
                """{"ok": true, "id": $id, "estado": "PENDIENTE", "detalle": "Sin respuesta del servidor"}"""
            }

        } catch (e: Exception) {
            """{"ok": false, "error": "${e.message}"}"""
        }
    }

    private fun hacerGet(url: String): String {
        return try {
            val client = clienteHttpConfiable()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            response.body?.string() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // Cliente que acepta certificados autofirmados (para servidor interno con IP)
    private fun clienteHttpConfiable(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    private fun armarValues(json: JSONObject, estado: String): ContentValues {
        val ahora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        return ContentValues().apply {
            put("fecha_guardado",   ahora)
            put("estado",           estado)
            put("coordenadas",      json.optString("coordenadas"))
            put("recrel",           json.optString("recrel"))
            put("fecha_carga",      json.optString("fechaCarga"))
            put("empresa",          json.optString("empresa"))
            put("partido",          json.optString("Partido"))
            put("localidad",        json.optString("Localidad"))
            put("sucursal",         json.optString("sucursal"))
            put("zona",             json.optString("zona"))
            put("calle",            json.optString("calle"))
            put("num_calle",        json.optString("num_calle"))
            put("entre_calle",      json.optString("entreCalle"))
            put("inspector",        json.optString("inspector"))
            put("descripcion",      json.optString("descripcion"))
            put("presencia",        json.optString("presencia"))
            put("normativa",        json.optString("normativa"))
            put("conclusion",       json.optString("conclusion"))
            put("instalacion",      json.optString("instalacion"))
            put("informacion_disp", json.optString("informacionDisp"))
            put("foto1",            json.optString("foto1"))
            put("foto2",            json.optString("foto2"))
            put("foto3",            json.optString("foto3"))
        }
    }
}