package com.hs.solutions.hstimecheck_2_0.historico

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHistoricoRobusto(viewModel: HistoricoViewModel) {

    val lista by viewModel.historico.collectAsState()
    var busca by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico Geral") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // 🔍 BUSCA
            OutlinedTextField(
                value = busca,
                onValueChange = {
                    busca = it
                    viewModel.setQuery(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = {
                    Text("Buscar produto, código ou evento")
                },
                singleLine = true
            )

            // 🎯 FILTROS
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                FiltroChip("Todos") {
                    viewModel.setFiltroEvento(null)
                }
                FiltroChip("Cadastro") {
                    viewModel.setFiltroEvento("Cadastro do produto")
                }
                FiltroChip("Atualização") {
                    viewModel.setFiltroEvento("Atualização do produto")
                }
                FiltroChip("Aprovação") {
                    viewModel.setFiltroEvento("Aprovação comercial")
                }
            }

            Spacer(Modifier.height(8.dp))

            // 📋 LISTA
            if (lista.isEmpty()) {
                Text(
                    text = "Nenhum histórico encontrado",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(lista) { item ->
                        HistoricoCard(item)
                    }
                }
            }
        }
    }
}
