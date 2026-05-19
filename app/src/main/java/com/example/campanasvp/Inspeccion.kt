package com.example.campanasvp

data class Inspeccion(
    val id: Long,
    val fechaGuardado: String,
    val estado: String,
    val coordenadas: String,
    val recrel: String,
    val fechaCarga: String,
    val empresa: String,
    val partido: String,
    val localidad: String,
    val sucursal: String,
    val zona: String,
    val calle: String,
    val numCalle: String,
    val entreCalle: String,
    val inspector: String,
    val descripcion: String,
    val presencia: String,
    val normativa: String,
    val conclusion: String,
    val instalacion: String,
    val informacionDisp: String,
    val foto1: String,
    val foto2: String,
    val foto3: String
)