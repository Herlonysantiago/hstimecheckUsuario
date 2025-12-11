package com.hs.solutions.hstimecheck.core

import com.google.firebase.database.FirebaseDatabase
import com.hs.solutions.hstimecheck.models.Produto
import kotlinx.coroutines.tasks.await
import android.util.Log
class ProductRepositoryFirebase {

    private val db = FirebaseDatabase.getInstance().reference.child("produtos")

    suspend fun carregarTodos(): List<Produto> {
        val snap = db.get().await()
        return snap.children.mapNotNull { it.getValue(Produto::class.java) }
    }

    suspend fun salvarRemoto(produto: Produto) {
        Log.e("fire", "Chamou salvar() no híbrido para: ${produto.id}")
        db.child(produto.id).setValue(produto).await()
    }

    suspend fun salvarTodosRemoto(lista: List<Produto>) {
        lista.forEach { salvarRemoto(it) }
    }

    suspend fun remover(id: String) {
        db.child(id).removeValue().await()
    }
}
