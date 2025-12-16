package com.hs.solutions.hstimecheck_2_0.core

import java.net.URL
import org.json.JSONObject

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductLookupService(private val context: Context) {

    companion object {
        private var cache: List<ProdutoJsonEntrada>? = null
        private var carregado = false
    }

    /** PRELOAD – carregado apenas 1 vez no app inteiro */
    suspend fun preload() {
        if (!carregado || cache == null) {
            cache = loadJsonInternal()
            carregado = true
        }
    }
    fun buscarFoto(codigo: String): String? {
        val url = "https://world.openfoodfacts.org/api/v0/product/$codigo.json"
        val json = URL(url).readText()
        val obj = JSONObject(json)
        val product = obj.optJSONObject("product") ?: return null
        return product.optString("image_front_url", null)
    }

    private suspend fun loadJsonInternal(): List<ProdutoJsonEntrada> =
        withContext(Dispatchers.IO) {

            val stream = context.assets.open("produtos.json")
            val json = stream.bufferedReader().use { it.readText() }

            val type = object : TypeToken<List<ProdutoJsonEntrada>>() {}.type
            Gson().fromJson(json, type)
        }

    /** Busca por código de barras */
    suspend fun buscarPorCodigoBarras(codigo: String): ProdutoJsonEntrada? {
        if (!carregado) preload()
        return cache?.firstOrNull { it.bar_cod?.toString() == codigo }
    }

    /** Busca por código interno (balança 20xxxxx) */
    suspend fun buscarPorCodigoInterno(cod: String): ProdutoJsonEntrada? {
        if (!carregado) preload()
        return cache?.firstOrNull { it.codigo?.toString() == cod }
    }
}
