package com.hs.solutions.hstimecheck_2_0.core

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hs.solutions.hstimecheck_2_0.auth.AuthSession
import com.hs.solutions.hstimecheck_2_0.models.Produto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductRepositoryFirebase {

    private fun produtosRef(): DatabaseReference {
        val uid = AuthSession.requireUserId()
        return FirebaseDatabase.getInstance()
            .reference
            .child("usuarios")
            .child(uid)
            .child("produtos")
    }

    suspend fun salvarRemoto(produto: Produto) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("fire", "Enviando para Firebase do usuario: ${produto.descricao}")
                produtosRef().child(produto.id).setValue(produto).await()
                Log.d("fire", "Produto salvo: ${produto.id}")
            } catch (e: Exception) {
                Log.e("fire", "Erro ao salvar: ${e.message}", e)
            }
        }
    }

    suspend fun carregarTodos(): List<Produto> {
        return withContext(Dispatchers.IO) {
            try {
                val snap = produtosRef().get().await()
                snap.children.mapNotNull { it.getValue(Produto::class.java) }
            } catch (e: Exception) {
                Log.e("fire", "Erro ao carregar lista: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun salvarTodosRemoto(lista: List<Produto>) {
        lista.forEach { salvarRemoto(it) }
    }

    suspend fun remover(id: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("fire", "Removendo do Firebase: $id")
                produtosRef().child(id).removeValue().await()
                Log.d("fire", "Produto removido.")
            } catch (e: Exception) {
                Log.e("fire", "Erro ao remover: ${e.message}", e)
            }
        }
    }

    fun iniciarEscutaRemota(onUpdate: (List<Produto>) -> Unit) {
        try {
            produtosRef().addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val listaAtualizada = snapshot.children.mapNotNull {
                        it.getValue(Produto::class.java)
                    }
                    onUpdate(listaAtualizada)
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e("fire", "Erro na escuta: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("fire", "Escuta remota nao iniciada: ${e.message}", e)
        }
    }
}
