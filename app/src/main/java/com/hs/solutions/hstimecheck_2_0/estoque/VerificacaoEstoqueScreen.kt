package com.hs.solutions.hstimecheck_2_0.estoque

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hs.solutions.hstimecheck_2_0.models.Produto   // ✅ IMPORT CORRETO
import com.hs.solutions.hstimecheck_2_0.estoque.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificacaoEstoqueScreen(
    viewModel: VerificacaoEstoqueViewModel
) {
    val produtos by viewModel.produtos.collectAsState()

    // ✅ STATE NO ESCOPO CERTO
    var produtoSelecionado by remember { mutableStateOf<Produto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Verificação de Estoque") }) }
    ) { padding ->

        if (produtos.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum produto em verificação")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                items(produtos) { produto ->
                    ItemVerificacaoEstoqueResumo(
                        produto = produto,
                        onClick = {
                            produtoSelecionado = produto
                        }
                    )
                }
            }
        }

        // ✅ DIALOG SEMPRE NO MESMO ESCOPO
        produtoSelecionado?.let { produto ->
            DialogAjusteEstoque(
                produto = produto,
                onSalvar = { novaQtd ->
                    viewModel.confirmarEstoque(produto, novaQtd)


                    produtoSelecionado = null
                },
                onCancelar = {
                    produtoSelecionado = null
                }
            )
        }
    }
}
