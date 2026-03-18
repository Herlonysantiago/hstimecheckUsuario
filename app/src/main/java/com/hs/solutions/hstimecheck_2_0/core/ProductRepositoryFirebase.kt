package com.hs.solutions.hstimecheck_2_0.core

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.hs.solutions.hstimecheck_2_0.models.Produto
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepositoryFirebase {

    // Referência única para o nó principal de produtos no Realtime Database
    private val db = FirebaseDatabase.getInstance().reference.child("produtos")

    /**
     * Salva ou atualiza um produto no Firebase.
     */
    suspend fun salvarRemoto(produto: Produto) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("fire", "Enviando para o Firebase: ${produto.descricao}")
                db.child(produto.id).setValue(produto).await()
                Log.d("fire", "✅ Sucesso ao salvar: ${produto.id}")
            } catch (e: Exception) {
                Log.e("fire", "❌ Erro ao salvar: ${e.message}")
            }
        }
    }

    /**
     * Busca TODOS os produtos lançados no Firebase.
     */
    suspend fun carregarTodos(): List<Produto> {
        return withContext(Dispatchers.IO) {
            try {
                val snap = db.get().await()
                // Converte cada "filho" do nó produtos de volta para o objeto Produto
                snap.children.mapNotNull { it.getValue(Produto::class.java) }
            } catch (e: Exception) {
                Log.e("fire", "Erro ao carregar lista: ${e.message}")
                emptyList()
            }
        }
    }

    /**
     * Salva uma lista inteira de uma vez (útil para migrações ou backups)
     */
    suspend fun salvarTodosRemoto(lista: List<Produto>) {
        lista.forEach { salvarRemoto(it) }
    }

    /**
     * Remove um produto pelo ID.
     * Este é o método que o seu ProductService agora chama.
     */
    suspend fun remover(id: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("fire", "Removendo do Firebase: $id")
                db.child(id).removeValue().await()
                Log.d("fire", "🗑️ Removido com sucesso!")
            } catch (e: Exception) {
                Log.e("fire", "❌ Erro ao remover: ${e.message}")
            }
        }
    }
    // Adicione esta função ao seu ProductRepositoryFirebase.kt
    fun iniciarEscutaRemota(onUpdate: (List<Produto>) -> Unit) {
        db.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val listaAtualizada = snapshot.children.mapNotNull {
                    it.getValue(Produto::class.java)
                }
                // 🚀 Envia a lista nova para o Service
                onUpdate(listaAtualizada)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("fire", "Erro na escuta: ${error.message}")
            }
        })
    }
}