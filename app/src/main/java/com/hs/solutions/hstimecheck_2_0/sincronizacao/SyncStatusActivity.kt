package com.hs.solutions.hstimecheck_2_0.sincronizacao

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hs.solutions.hstimecheck_2_0.auth.AuthSession
import com.hs.solutions.hstimecheck_2_0.auth.SignInActivity
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.core.ProductRepositoryFirebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class SyncStatusActivity : AppCompatActivity() {

    private val firebaseRepo = ProductRepositoryFirebase()

    private lateinit var tvConta: TextView
    private lateinit var tvBase: TextView
    private lateinit var tvMensagem: TextView
    private lateinit var progress: ProgressBar
    private lateinit var btnSincronizar: Button
    private lateinit var btnAtualizar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthSession.hasActiveSession(this)) {
            voltarParaLogin()
            return
        }

        try {
            AppContainer.init(this)
        } catch (_: Exception) {
            voltarParaLogin()
            return
        }

        montarTela()
        carregarStatus()
    }

    private fun montarTela() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val titulo = TextView(this).apply {
            text = "Status da sincronizacao"
            textSize = 22f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        tvConta = TextView(this).apply {
            textSize = 15f
            setPadding(0, 28, 0, 16)
        }

        tvBase = TextView(this).apply {
            textSize = 15f
            setPadding(0, 0, 0, 16)
        }

        tvMensagem = TextView(this).apply {
            textSize = 14f
            setPadding(0, 0, 0, 16)
        }

        progress = ProgressBar(this).apply {
            visibility = View.GONE
        }

        btnSincronizar = Button(this).apply {
            text = if (AppContainer.productService.firebaseEnabled) {
                "Sincronizar agora"
            } else {
                "Sincronizacao indisponivel no offline"
            }
            setOnClickListener { sincronizarAgora() }
        }

        btnAtualizar = Button(this).apply {
            text = "Atualizar status"
            setOnClickListener { carregarStatus("Status atualizado.") }
        }

        val btnVoltar = Button(this).apply {
            text = "Voltar"
            setOnClickListener { finish() }
        }

        root.addView(titulo)
        root.addView(tvConta)
        root.addView(tvBase)
        root.addView(tvMensagem)
        root.addView(progress)
        root.addView(btnSincronizar)
        root.addView(btnAtualizar)
        root.addView(btnVoltar)
        setContentView(root)
    }

    private fun carregarStatus(mensagem: String = "") {
        lifecycleScope.launch {
            setLoading(true, mensagem.ifBlank { "Carregando status..." })
            try {
                AppContainer.productService.carregar()
                val local = AppContainer.productService.produtos.value.size
                val remoto = if (AppContainer.productService.firebaseEnabled) {
                    firebaseRepo.carregarTodos().size.toString()
                } else {
                    "Modo offline"
                }
                val user = AuthSession.currentUser
                val ultimaLeitura = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
                    .format(Date())

                tvConta.text = buildString {
                    if (AppContainer.productService.firebaseEnabled) {
                        appendLine("Conta Google")
                        appendLine("Nome: ${user?.displayName ?: "Nao informado"}")
                        appendLine("E-mail: ${user?.email ?: "Nao informado"}")
                        append("UID: ${user?.uid?.take(12) ?: "Nao informado"}")
                    } else {
                        appendLine("Modo offline")
                        appendLine("Conta Google: nao usada")
                        append("Base local: offline_local")
                    }
                }

                tvBase.text = buildString {
                    appendLine("Base de dados")
                    appendLine("Produtos no celular: $local")
                    appendLine("Produtos no Firebase: $remoto")
                    append("Ultima leitura: $ultimaLeitura")
                }

                setLoading(false, mensagem.ifBlank { "Status carregado." })
            } catch (e: Exception) {
                setLoading(false, "Nao foi possivel carregar o status: ${e.message ?: "erro desconhecido"}")
            }
        }
    }

    private fun sincronizarAgora() {
        lifecycleScope.launch {
            setLoading(true, "Sincronizando base...")
            try {
                if (!AppContainer.productService.firebaseEnabled) {
                    setLoading(false, "Modo offline: nada foi enviado para o Firebase.")
                    return@launch
                }
                AppContainer.productService.carregar()
                AppContainer.productService.sincronizarTudoComFirebase()
                carregarStatus("Sincronizacao concluida.")
            } catch (e: Exception) {
                setLoading(false, "Nao foi possivel sincronizar: ${e.message ?: "erro desconhecido"}")
            }
        }
    }

    private fun setLoading(loading: Boolean, mensagem: String) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnSincronizar.isEnabled = !loading
        btnAtualizar.isEnabled = !loading
        tvMensagem.text = mensagem
    }

    private fun voltarParaLogin() {
        startActivity(
            Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
