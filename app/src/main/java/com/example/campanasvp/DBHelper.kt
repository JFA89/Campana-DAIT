package com.example.campanasvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "inspecciones.db", null, 2) {

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
                urlfoto1 TEXT,
                urlfoto2 TEXT,
                urlfoto3 TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Agrega las columnas nuevas sin borrar registros existentes
            db.execSQL("ALTER TABLE inspecciones ADD COLUMN urlfoto1 TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE inspecciones ADD COLUMN urlfoto2 TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE inspecciones ADD COLUMN urlfoto3 TEXT DEFAULT ''")
        }
    }

    // ── ACTUALIZAR ESTADO ─────────────────────────────────────────────────────

    fun actualizarEstado(id: Long, nuevoEstado: String) {
        val values = ContentValues().apply {
            put("estado", nuevoEstado)
        }
        writableDatabase.update("inspecciones", values, "id = ?", arrayOf(id.toString()))
    }

    fun eliminar(id: Long) {
        writableDatabase.delete("inspecciones", "id = ?", arrayOf(id.toString()))
    }

    // ── CONSULTAS ─────────────────────────────────────────────────────────────

    fun obtenerTodas(): List<Inspeccion> {
        return obtenerCursor(readableDatabase.query("inspecciones", null, null, null, null, null, "id DESC"))
    }

    fun obtenerPorEstado(estado: String): List<Inspeccion> {
        val cursor = readableDatabase.query(
            "inspecciones", null,
            "estado = ?", arrayOf(estado),
            null, null, "id DESC"
        )
        return obtenerCursor(cursor)
    }

    private fun obtenerCursor(cursor: android.database.Cursor): List<Inspeccion> {
        val lista = mutableListOf<Inspeccion>()
        while (cursor.moveToNext()) {
            lista.add(Inspeccion(
                id              = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                fechaGuardado   = cursor.getString(cursor.getColumnIndexOrThrow("fecha_guardado")),
                estado          = cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                coordenadas     = cursor.getString(cursor.getColumnIndexOrThrow("coordenadas")),
                recrel          = cursor.getString(cursor.getColumnIndexOrThrow("recrel")),
                fechaCarga      = cursor.getString(cursor.getColumnIndexOrThrow("fecha_carga")),
                empresa         = cursor.getString(cursor.getColumnIndexOrThrow("empresa")),
                partido         = cursor.getString(cursor.getColumnIndexOrThrow("partido")),
                localidad       = cursor.getString(cursor.getColumnIndexOrThrow("localidad")),
                sucursal        = cursor.getString(cursor.getColumnIndexOrThrow("sucursal")),
                zona            = cursor.getString(cursor.getColumnIndexOrThrow("zona")),
                calle           = cursor.getString(cursor.getColumnIndexOrThrow("calle")),
                numCalle        = cursor.getString(cursor.getColumnIndexOrThrow("num_calle")),
                entreCalle      = cursor.getString(cursor.getColumnIndexOrThrow("entre_calle")),
                inspector       = cursor.getString(cursor.getColumnIndexOrThrow("inspector")),
                descripcion     = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                presencia       = cursor.getString(cursor.getColumnIndexOrThrow("presencia")),
                normativa       = cursor.getString(cursor.getColumnIndexOrThrow("normativa")),
                conclusion      = cursor.getString(cursor.getColumnIndexOrThrow("conclusion")),
                instalacion     = cursor.getString(cursor.getColumnIndexOrThrow("instalacion")),
                informacionDisp = cursor.getString(cursor.getColumnIndexOrThrow("informacion_disp")),
                urlfoto1        = cursor.getString(cursor.getColumnIndexOrThrow("urlfoto1")),
                urlfoto2        = cursor.getString(cursor.getColumnIndexOrThrow("urlfoto2")),
                urlfoto3        = cursor.getString(cursor.getColumnIndexOrThrow("urlfoto3"))
            ))
        }
        cursor.close()
        return lista
    }
}