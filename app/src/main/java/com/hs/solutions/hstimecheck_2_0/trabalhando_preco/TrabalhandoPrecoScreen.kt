package com.hs.solutions.hstimecheck_2_0.trabalhando_preco

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabalhandoPrecoScreen(
    viewModel: TrabalhandoPrecoViewModel
) {
    val produtos by viewModel.produtos.collectAsState()
    val produtosSelecionados = remember {
        mutableStateListOf<Produto>()
    }

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Trabalhando Preço") })
        }
    ) { padding ->

        if (produtos.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum produto trabalhando preço")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                items(produtos) { produto ->
                    ItemTrabalhandoPreco(
                        produto = produto,
                        onEnviarParaAprovacao = {
                            // por enquanto só visual
                            // depois liga com a Tela 63
                        },
                        onMarcarPrecoEmNegociacao = { motivo ->
                            viewModel.marcarPrecoEmNegociacao(produto, motivo)
                        }
                    )
                }
            }
        }
    }
    fun alternarSelecao(produto: Produto) {
        if (produtosSelecionados.contains(produto)) {
            produtosSelecionados.remove(produto)
        } else {
            produtosSelecionados.add(produto)
        }
    }

}
