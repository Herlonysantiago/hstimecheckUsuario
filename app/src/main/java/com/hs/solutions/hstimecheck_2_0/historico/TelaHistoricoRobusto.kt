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
import com.hs.solutions.hstimecheck_2_0.models.*
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
            // BUSCA
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
            // FILTROS
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                FiltroChip("Todos") {
                    viewModel.setFiltroEvento(null)
                }
                FiltroChip("Cadastro") {
                    viewModel.setFiltroEvento(TipoEventoHistorico.CADASTRO_PRODUTO)
                }
                FiltroChip("Atualização") {
                    viewModel.setFiltroEvento(TipoEventoHistorico.EDICAO_PRODUTO)
                }
                FiltroChip("Aprovação") {
                    viewModel.setFiltroEvento(TipoEventoHistorico.APROVACAO_COMERCIAL)
                }
                FiltroChip("Preço") {
                    viewModel.setFiltroEvento(TipoEventoHistorico.TRABALHANDO_PRECO)
                }
            }
            // LISTA
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(lista) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            // TÍTULO DO EVENTO
                            Text(
                                text = item.titulo,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))
                            // CÓDIGOS
                            Text(
                                text = buildString {
                                    item.codigoInterno?.let { append("CI: $it  ") }
                                    item.codigoBarras?.let { append("CB: $it") }
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                            // VALIDADE
                            item.validade?.let {
                                Text(
                                    text = "Validade: $it",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Spacer(Modifier.height(6.dp))
                            // DESCRIÇÃO DO EVENTO
                            Text(
                                text = item.descricao
                            )

                            Spacer(Modifier.height(6.dp))
                            // DATA
                            Text(
                                text = item.dataEvento,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
       }   }
    }
}
