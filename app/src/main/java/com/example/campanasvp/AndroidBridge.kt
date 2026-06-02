package com.example.campanasvp

import android.content.ContentValues
import android.content.Context
import android.util.Base64
import android.webkit.JavascriptInterface
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.io.File
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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

            // 2. Intentar enviar al servidor por POST multipart
            val respuesta = hacerPost(json)

            if (respuesta.trim() == "OK") {
                // 3. Servidor respondió OK → actualizar a ENVIADO
                DBHelper(context).actualizarEstado(id, "ENVIADO")
                """{"ok": true, "id": $id, "estado": "ENVIADO"}"""
            } else {
                // Quedó como PENDIENTE
                """{"ok": true, "id": $id, "estado": "PENDIENTE", "detalle": "Sin respuesta del servidor"}"""
            }

        } catch (e: Exception) {
            """{"ok": false, "error": "${e.message}"}"""
        }
    }

    private fun hacerPost(json: JSONObject): String {
        return try {
            val client = clienteHttpConfiable()

            // ── Fotos como base64 ─────────────────────────────────────────────
            val rutaFoto1 = json.optString("foto1")
            val rutaFoto2 = json.optString("foto2")
            val rutaFoto3 = json.optString("foto3")

            val base64Foto1 = archivoABase64(rutaFoto1)
            val base64Foto2 = archivoABase64(rutaFoto2)
            val base64Foto3 = archivoABase64(rutaFoto3)

            val tienesFotos = base64Foto1.isNotEmpty() ||
                    base64Foto2.isNotEmpty() ||
                    base64Foto3.isNotEmpty()

            // ── Campos de texto + fotos base64 como form-data ─────────────────
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("empresa",         json.optString("empresa"))
                .addFormDataPart("recrel",          json.optString("recrel"))
                .addFormDataPart("fecha",           json.optString("fechaCarga"))
                .addFormDataPart("Partido",         json.optString("Partido"))
                .addFormDataPart("Localidad",       json.optString("Localidad"))
                .addFormDataPart("sucursal",        json.optString("sucursal"))
                .addFormDataPart("zona",            json.optString("zona"))
                .addFormDataPart("calle",           json.optString("calle"))
                .addFormDataPart("num_calle",       json.optString("num_calle"))
                .addFormDataPart("entreCalle",      json.optString("entreCalle"))
                .addFormDataPart("inspector",       json.optString("inspector"))
                .addFormDataPart("descripcion",     json.optString("descripcion"))
                .addFormDataPart("presencia",       json.optString("presencia"))
                .addFormDataPart("normativa",       json.optString("normativa"))
                .addFormDataPart("conclusion",      json.optString("conclusion"))
                .addFormDataPart("instalacion",     json.optString("instalacion"))
                .addFormDataPart("informacionDisp", json.optString("informacionDisp"))
                .addFormDataPart("coordenadas",     json.optString("coordenadas"))
                .addFormDataPart("fotos",           if (tienesFotos) "S" else "N")
                .addFormDataPart("foto1",           base64Foto1)
                .addFormDataPart("foto2",           base64Foto2)
                .addFormDataPart("foto3",           base64Foto3)

            val request = Request.Builder()
                .url("http://www.enre.gov.ar/InspeccionesGAP.nsf/RecibirFormulario3?openagent")
                .post(builder.build())
                .build()

            val response = client.newCall(request).execute()
            response.body?.string() ?: ""

        } catch (e: Exception) {
            ""
        }
    }

    // Convierte archivo a base64, retorna string vacío si no existe
    private fun archivoABase64(ruta: String): String {
        if (ruta.isBlank()) return ""
        val archivo = File(ruta)
        if (!archivo.exists()) return ""
        val bytes = archivo.readBytes()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    // Cliente que acepta certificados autofirmados (servidor interno)
    private fun clienteHttpConfiable(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
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