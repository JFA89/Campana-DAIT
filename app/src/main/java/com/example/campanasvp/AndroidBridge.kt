package com.example.campanasvp

import android.content.ContentValues
import android.content.Context
import android.webkit.JavascriptInterface
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

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
            val values = armarValues(json, "PENDIENTE")
            val id = db.insert("inspecciones", null, values)
            // Acá iría el POST al servidor
            """{"ok": true, "id": $id}"""
        } catch (e: Exception) {
            """{"ok": false, "error": "${e.message}"}"""
        }
    }

    private fun armarValues(json: JSONObject, estado: String): ContentValues {
        val ahora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        return ContentValues().apply {
            put("fecha_guardado",  ahora)
            put("estado",          estado)
            put("coordenadas",     json.optString("coordenadas"))
            put("recrel",          json.optString("recrel"))
            put("fecha_carga",     json.optString("fechaCarga"))
            put("empresa",         json.optString("empresa"))
            put("partido",         json.optString("Partido"))
            put("localidad",       json.optString("Localidad"))
            put("sucursal",        json.optString("sucursal"))
            put("zona",            json.optString("zona"))
            put("calle",           json.optString("calle"))
            put("num_calle",       json.optString("num_calle"))
            put("entre_calle",     json.optString("entreCalle"))
            put("inspector",       json.optString("inspector"))
            put("descripcion",     json.optString("descripcion"))
            put("presencia",       json.optString("presencia"))
            put("normativa",       json.optString("normativa"))
            put("conclusion",      json.optString("conclusion"))
            put("instalacion",     json.optString("instalacion"))
            put("informacion_disp",json.optString("informacionDisp"))
            put("foto1",           json.optString("foto1"))
            put("foto2",           json.optString("foto2"))
            put("foto3",           json.optString("foto3"))
        }
    }
}