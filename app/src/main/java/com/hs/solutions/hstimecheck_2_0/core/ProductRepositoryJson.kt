package com.hs.solutions.hstimecheck_2_0.core

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hs.solutions.hstimecheck_2_0.models.Produto
import java.io.File

class ProductRepositoryJson(private val context: Context) : ProductRepository {

    private val file = File(context.filesDir, "produtos_new.json")
    private val gson = Gson()

    private fun readFile(): MutableList<Produto> {
        return try {

            if (!file.exists()) {
                Log.e("JSON_REPO", "Arquivo não existe, criando vazio")
                file.writeText("[]")
                return mutableListOf()
            }

            val json = file.readText()

            if (json.isBlank()) {
                Log.e("JSON_REPO", "JSON vazio detectado, resetando arquivo")
                file.writeText("[]")
                return mutableListOf()
            }

            val type = object : TypeToken<MutableList<Produto>>() {}.type
            val lista: MutableList<Produto>? = gson.fromJson(json, type)

            if (lista == null) {
                Log.e("JSON_REPO", "Erro de conversão, resetando arquivo")
                file.writeText("[]")
                mutableListOf()
            } else {
                Log.e("JSON_REPO", "Carregado ${lista.size} itens do JSON")
                lista
            }

        } catch (e: Exception) {
            Log.e("JSON_REPO", "Erro crítico ao ler JSON (resetando)", e)
            file.writeText("[]")
            mutableListOf()
        }
    }


    private fun writeFile(list: List<Produto>) {
        try {
            val tmp = File(file.parentFile, file.name + ".tmp")
            tmp.writeText(gson.toJson(list))

            if (tmp.exists()) {
                tmp.renameTo(file)
            } else {
                file.writeText(gson.toJson(list))
            }

            Log.e("JSON_REPO", "JSON salvo com sucesso (${list.size} itens)")

        } catch (e: Exception) {
            Log.e("JSON_REPO", "ERRO ao salvar JSON!", e)
        }
    }


    override suspend fun carregar(): List<Produto> {
        Log.e("JSON_REPO", "carregar() chamado")
        return readFile()
    }

    override suspend fun salvar(produto: Produto) {
        Log.e("JSON_REPO", "Salvar produto id=${produto.id}")

        val lista = readFile()
        val index = lista.indexOfFirst { it.id == produto.id }

        if (index >= 0) {
            lista[index] = produto
            Log.e("JSON_REPO", "Atualizando produto existente")
        } else {
            lista.add(produto)
            Log.e("JSON_REPO", "Adicionando novo produto")
        }

        writeFile(lista)
    }

    override suspend fun salvarTodos(produtos: List<Produto>) {
        Log.e("JSON_REPO", "Salvar TODOS produtos (${produtos.size})")
        writeFile(produtos)
    }

    override suspend fun remover(id: String) {
        Log.e("JSON_REPO", "Remover produto id=$id")
        val lista = readFile().filterNot { it.id == id }
        writeFile(lista)
    }
}
