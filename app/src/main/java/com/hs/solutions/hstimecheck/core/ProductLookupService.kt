package com.hs.solutions.hstimecheck.core

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hs.solutions.hstimecheck.models.Produto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductLookupService(private val context: Context) {

    private var cache: List<ProdutoJsonEntrada>? = null

    // carrega JSON somente 1 vez
    private suspend fun loadJson(): List<ProdutoJsonEntrada> =
        withContext(Dispatchers.IO) {
            if (cache != null) return@withContext cache!!

            val json = context.assets.open("produtos.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<ProdutoJsonEntrada>>() {}.type
            cache = Gson().fromJson(json, type)
            cache!!
        }

    // procura por código de barras
    suspend fun buscarPorCodigoBarras(codigo: String): ProdutoJsonEntrada? {
        val lista = loadJson()
        return lista.find { it.bar_cod?.toString() == codigo }
    }

    // procura por código interno (balança)
    suspend fun buscarPorCodigoInterno(codigoInterno: String): ProdutoJsonEntrada? {
        val lista = loadJson()
        return lista.find {
            it.codigo ?.toString() == codigoInterno

        }
    }
}
