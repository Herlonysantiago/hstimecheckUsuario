package com.hs.solutions.hstimecheck_2_0.configuracoes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.auth.AuthSession
import com.hs.solutions.hstimecheck_2_0.auth.SignInActivity
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.core.AppPreferences
import kotlinx.coroutines.launch

class ConfiguracoesSistemaActivity : AppCompatActivity() {

    private lateinit var switchOnline: Switch
    private lateinit var switchValidade: Switch
    private lateinit var switchAprovacao: Switch
    private lateinit var switchFotoObrigatoria: Switch
    private lateinit var switchValidadeObrigatoria: Switch
    private lateinit var switchBloquearSemAprovacao: Switch

    private lateinit var btnSincronizar: Button
    private lateinit var btnLimparCache: Button
    private lateinit var btnRestaurar: Button
    private lateinit var btnSairConta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracoes_sistema)

        bindViews()
        carregarConfiguracoes()
        setupListeners()
    }

    private fun bindViews() {
        switchOnline = findViewById(R.id.switchOnline)
        switchValidade = findViewById(R.id.switchValidade)
        switchAprovacao = findViewById(R.id.switchAprovacao)
        switchFotoObrigatoria = findViewById(R.id.switchFotoObrigatoria)
        switchValidadeObrigatoria = findViewById(R.id.switchValidadeObrigatoria)
        switchBloquearSemAprovacao = findViewById(R.id.switchBloquearSemAprovacao)

        btnSincronizar = findViewById(R.id.btnSincronizar)
        btnLimparCache = findViewById(R.id.btnLimparCache)
        btnRestaurar = findViewById(R.id.btnRestaurar)
        btnSairConta = findViewById(R.id.btnSairConta)
    }

    private fun carregarConfiguracoes() {
        lifecycleScope.launch {
            AppPreferences.read(this@ConfiguracoesSistemaActivity, AppPreferences.MODO_ONLINE, true)
                .collect { switchOnline.isChecked = it }
        }

        lifecycleScope.launch {
            AppPreferences.read(this@ConfiguracoesSistemaActivity, AppPreferences.ALERTA_VALIDADE, true)
                .collect { switchValidade.isChecked = it }
        }

        lifecycleScope.launch {
            AppPreferences.read(this@ConfiguracoesSistemaActivity, AppPreferences.ALERTA_APROVACAO, true)
                .collect { switchAprovacao.isChecked = it }
        }

        lifecycleScope.launch {
            AppPreferences.read(this@ConfiguracoesSistemaActivity, AppPreferences.FOTO_OBRIGATORIA, false)
                .collect { switchFotoObrigatoria.isChecked = it }
        }

        lifecycleScope.launch {
            AppPreferences.read(this@ConfiguracoesSistemaActivity, AppPreferences.VALIDADE_OBRIGATORIA, true)
                .collect { switchValidadeObrigatoria.isChecked = it }
        }

        lifecycleScope.launch {
            AppPreferences.read(this@ConfiguracoesSistemaActivity, AppPreferences.BLOQUEAR_SEM_APROVACAO, true)
                .collect { switchBloquearSemAprovacao.isChecked = it }
        }
    }

    private fun setupListeners() {

        switchOnline.setOnCheckedChangeListener { _, v ->
            salvar(AppPreferences.MODO_ONLINE, v)
        }

        switchValidade.setOnCheckedChangeListener { _, v ->
            salvar(AppPreferences.ALERTA_VALIDADE, v)
        }

        switchAprovacao.setOnCheckedChangeListener { _, v ->
            salvar(AppPreferences.ALERTA_APROVACAO, v)
        }

        switchFotoObrigatoria.setOnCheckedChangeListener { _, v ->
            salvar(AppPreferences.FOTO_OBRIGATORIA, v)
        }

        switchValidadeObrigatoria.setOnCheckedChangeListener { _, v ->
            salvar(AppPreferences.VALIDADE_OBRIGATORIA, v)
        }

        switchBloquearSemAprovacao.setOnCheckedChangeListener { _, v ->
            salvar(AppPreferences.BLOQUEAR_SEM_APROVACAO, v)
        }

        btnSairConta.setOnClickListener {
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            WorkManager.getInstance(this).cancelUniqueWork("alertas_produtos")
            AuthSession.signOut()
            AppContainer.reset()
            val intent = Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun salvar(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        lifecycleScope.launch {
            AppPreferences.save(this@ConfiguracoesSistemaActivity, key, value)
        }
    }
}
