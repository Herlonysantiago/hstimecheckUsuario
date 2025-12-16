package com.hs.solutions.hstimecheck_2_0.core

import android.content.Context
import com.hs.solutions.hstimecheck_2_0.models.Produto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncManager(
    private val context: Context,
    private val repoLocal: ProductRepositoryJson,
    private val repoRemote: ProductRepositoryFirebase
) {

    suspend fun sincronizar() = withContext(Dispatchers.IO) {

        val local = repoLocal.carregar()
        val remoto = repoRemote.carregarTodos()

        val mapaLocal = local.associateBy { it.id }.toMutableMap()
        val mapaRemoto = remoto.associateBy { it.id }.toMutableMap()

        val final = mutableListOf<Produto>()

        for (p in mapaLocal.values) {
            val remotoMatch = mapaRemoto[p.id]
            if (remotoMatch == null) final.add(p)
            else {
                //final.add(
//if (p.updatedAt > remotoMatch.updatedAt) p else remotoMatch
                //)
            }
            mapaRemoto.remove(p.id)
        }

        final.addAll(mapaRemoto.values)

        repoLocal.salvarTodos(final)
        repoRemote.salvarTodosRemoto(final)
    }
}
