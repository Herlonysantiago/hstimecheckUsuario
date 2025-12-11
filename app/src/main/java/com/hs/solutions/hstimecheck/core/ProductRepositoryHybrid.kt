package com.hs.solutions.hstimecheck.core

import android.content.Context
import com.hs.solutions.hstimecheck.models.Produto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import android.util.Log
class ProductRepositoryHybrid(
    private val local: ProductRepositoryJson,
    private val remote: ProductRepositoryFirebase,
    private val context: Context
) : ProductRepository {

    private suspend fun hasInternet(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // método simples: tenta resolver DNS do google
            InetAddress.getByName("8.8.8.8")
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun carregar(): List<Produto> {
        return try {
            // Carrega sempre o local primeiro (cache)
            val locais = local.carregar()

            if (hasInternet()) {
                val remotos = remote.carregarTodos()
                if (remotos.isNotEmpty()) {
                    // Se remoto tem dados, atualiza cache local e retorna o cache (consistente)
                    local.salvarTodos(remotos)
                    return local.carregar()
                }
                // remoto vazio -> fallback ao local já carregado
            }

            // Sem internet ou remoto vazio -> usar local
            locais
        } catch (e: Exception) {
            // Qualquer erro -> fallback seguro para local
            try { local.carregar() } catch (_: Exception) { emptyList() }
        }
    }




    override suspend fun salvar(produto: Produto) {
        // salva local sempre
        Log.e("HYBRID_REPO", "Chamou salvar() no híbrido para: ${produto.id}")
        local.salvar(produto)
        // tenta enviar para remoto (não bloqueante do ponto de vista do chamador)
        try {
            if (hasInternet()) {
                Log.e("jason_REPO", "Chamou salvar() no salvar remoto para: ${produto.id}")
                remote.salvarRemoto(produto)
            }
        } catch (_: Exception) {
            // falha de rede remota ignorada (ficará no local)
        }
    }

    override suspend fun salvarTodos(produtos: List<Produto>) {
        local.salvarTodos(produtos)
        try {
            if (hasInternet()) {
                remote.salvarTodosRemoto(produtos)
            }
        } catch (_: Exception) {
        }
    }
    override suspend fun remover(id: String) {
        local.remover(id)
        remote.remover(id)
    }


}
