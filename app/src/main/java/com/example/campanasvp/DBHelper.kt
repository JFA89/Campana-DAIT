package com.example.campanasvp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "inspecciones.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE inspecciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha_guardado TEXT,
                estado TEXT,
                coordenadas TEXT,
                recrel TEXT,
                fecha_carga TEXT,
                empresa TEXT,
                partido TEXT,
                localidad TEXT,
                sucursal TEXT,
                zona TEXT,
                calle TEXT,
                num_calle TEXT,
                entre_calle TEXT,
                inspector TEXT,
                descripcion TEXT,
                presencia TEXT,
                normativa TEXT,
                conclusion TEXT,
                instalacion TEXT,
                informacion_disp TEXT,
                foto1 TEXT,
                foto2 TEXT,
                foto3 TEXT
            )
        """.trimIndent())
    }

    fun obtenerTodas(): List<Inspeccion> {
        val lista = mutableListOf<Inspeccion>()
        val db = readableDatabase
        val cursor = db.query("inspecciones", null, null, null, null, null, "id DESC")

        while (cursor.moveToNext()) {
            val inspeccion = Inspeccion(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                fechaGuardado = cursor.getString(cursor.getColumnIndexOrThrow("fecha_guardado")),
                estado = cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                coordenadas = cursor.getString(cursor.getColumnIndexOrThrow("coordenadas")),
                recrel = cursor.getString(cursor.getColumnIndexOrThrow("recrel")),
                fechaCarga = cursor.getString(cursor.getColumnIndexOrThrow("fecha_carga")),
                empresa = cursor.getString(cursor.getColumnIndexOrThrow("empresa")),
                partido = cursor.getString(cursor.getColumnIndexOrThrow("partido")),
                localidad = cursor.getString(cursor.getColumnIndexOrThrow("localidad")),
                sucursal = cursor.getString(cursor.getColumnIndexOrThrow("sucursal")),
                zona = cursor.getString(cursor.getColumnIndexOrThrow("zona")),
                calle = cursor.getString(cursor.getColumnIndexOrThrow("calle")),
                numCalle = cursor.getString(cursor.getColumnIndexOrThrow("num_calle")),
                entreCalle = cursor.getString(cursor.getColumnIndexOrThrow("entre_calle")),
                inspector = cursor.getString(cursor.getColumnIndexOrThrow("inspector")),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                presencia = cursor.getString(cursor.getColumnIndexOrThrow("presencia")),
                normativa = cursor.getString(cursor.getColumnIndexOrThrow("normativa")),
                conclusion = cursor.getString(cursor.getColumnIndexOrThrow("conclusion")),
                instalacion = cursor.getString(cursor.getColumnIndexOrThrow("instalacion")),
                informacionDisp = cursor.getString(cursor.getColumnIndexOrThrow("informacion_disp")),
                foto1 = cursor.getString(cursor.getColumnIndexOrThrow("foto1")),
                foto2 = cursor.getString(cursor.getColumnIndexOrThrow("foto2")),
                foto3 = cursor.getString(cursor.getColumnIndexOrThrow("foto3"))
            )
            lista.add(inspeccion)
        }
        cursor.close()
        db.close()
        return lista
    }

    fun obtenerPorEstado(estado: String): List<Inspeccion> {
        val lista = mutableListOf<Inspeccion>()
        val cursor = readableDatabase.query(
            "inspecciones",
            null,
            "estado = ?",
            arrayOf(estado),
            null,
            null,
            "id DESC"
        )
        while (cursor.moveToNext()) {
            val inspeccion = Inspeccion(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                fechaGuardado = cursor.getString(cursor.getColumnIndexOrThrow("fecha_guardado")),
                estado = cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                coordenadas = cursor.getString(cursor.getColumnIndexOrThrow("coordenadas")),
                recrel = cursor.getString(cursor.getColumnIndexOrThrow("recrel")),
                fechaCarga = cursor.getString(cursor.getColumnIndexOrThrow("fecha_carga")),
                empresa = cursor.getString(cursor.getColumnIndexOrThrow("empresa")),
                partido = cursor.getString(cursor.getColumnIndexOrThrow("partido")),
                localidad = cursor.getString(cursor.getColumnIndexOrThrow("localidad")),
                sucursal = cursor.getString(cursor.getColumnIndexOrThrow("sucursal")),
                zona = cursor.getString(cursor.getColumnIndexOrThrow("zona")),
                calle = cursor.getString(cursor.getColumnIndexOrThrow("calle")),
                numCalle = cursor.getString(cursor.getColumnIndexOrThrow("num_calle")),
                entreCalle = cursor.getString(cursor.getColumnIndexOrThrow("entre_calle")),
                inspector = cursor.getString(cursor.getColumnIndexOrThrow("inspector")),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                presencia = cursor.getString(cursor.getColumnIndexOrThrow("presencia")),
                normativa = cursor.getString(cursor.getColumnIndexOrThrow("normativa")),
                conclusion = cursor.getString(cursor.getColumnIndexOrThrow("conclusion")),
                instalacion = cursor.getString(cursor.getColumnIndexOrThrow("instalacion")),
                informacionDisp = cursor.getString(cursor.getColumnIndexOrThrow("informacion_disp")),
                foto1 = cursor.getString(cursor.getColumnIndexOrThrow("foto1")),
                foto2 = cursor.getString(cursor.getColumnIndexOrThrow("foto2")),
                foto3 = cursor.getString(cursor.getColumnIndexOrThrow("foto3"))
            )
            lista.add(inspeccion)
        }
        cursor.close()
        return lista
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS inspecciones")
        onCreate(db)
    }
}