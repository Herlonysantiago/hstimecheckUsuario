package com.hs.solutions.hstimecheck_2_0.core

import android.content.Context
import org.json.JSONObject
import java.io.File

object FotoRepository {

    private const val FILE_NAME = "fotos.json"
    private val mapa = mutableMapOf<String, String>()
    private var carregado = false

    private fun getFile(context: Context): File =
        File(context.filesDir, FILE_NAME)

    // 🔹 Carrega do disco (uma vez)
    fun carregar(context: Context) {
        if (carregado) return
        val file = getFile(context)
        if (!file.exists()) {
            carregado = true
            return
        }

        val json = JSONObject(file.readText())
        json.keys().forEach { key ->
            mapa[key] = json.getString(key)
        }
        carregado = true
    }

    // 🔹 Salva somente se não existir ou se mudou
    fun salvar(context: Context, codigo: String, fotoUrl: String) {
        val atual = mapa[codigo]
        if (atual == fotoUrl) return

        mapa[codigo] = fotoUrl
        persistir(context)
    }

    // 🔹 Busca foto por código
    fun buscar(codigo: String): String? =
        mapa[codigo]

    private fun persistir(context: Context) {
        val json = JSONObject()
        mapa.forEach { (k, v) -> json.put(k, v) }
        getFile(context).writeText(json.toString())
    }
}
