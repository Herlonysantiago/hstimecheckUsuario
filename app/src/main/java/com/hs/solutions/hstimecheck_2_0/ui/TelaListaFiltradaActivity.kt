package com.hs.solutions.hstimecheck_2_0.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.models.FiltroLista

class TelaListaFiltradaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)

        val filtro = intent.getStringExtra("filtro")
            ?.let { FiltroLista.valueOf(it) }
            ?: FiltroLista.TODOS

        setContent {
            MaterialTheme {
                TelaPrincipalBase(
                    service = AppContainer.productService,
                    filtro = filtro
                )
            }
        }
    }
}
