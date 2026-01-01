package com.hs.solutions.hstimecheck_2_0.ui.verificacaoqualidade

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hs.solutions.hstimecheck_2_0.models.HistoricoItem
import com.hs.solutions.hstimecheck_2_0.models.Produto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificacaoQualidadeProdutoScreen(
    codigoBarras: String?,
    codigoInterno: String?,
    viewModel: VerificacaoQualidadeProdutoViewModel,
    onBack: () -> Unit,
    onAbrirScanner: () -> Unit
) {

    // ✅ estado local do campo
    var codigoDigitado by rememberSaveable {
        mutableStateOf(codigoInterno ?: "")
    }

    // ✅ busca automática APENAS quando vem do scanner / intent
    LaunchedEffect(codigoBarras) {
        if (!codigoBarras.isNullOrBlank()) {
            viewModel.carregar(codigoBarras, null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📦 Validades do Produto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            when {
                viewModel.erro != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(viewModel.erro!!)
                    }
                }

                viewModel.produto == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {

                        OutlinedTextField(
                            value = codigoDigitado,
                            onValueChange = { codigoDigitado = it },
                            label = { Text("Código interno ou barras") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (codigoDigitado.isNotBlank()) {
                                    viewModel.carregar(null, codigoDigitado)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Buscar produto")
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onAbrirScanner,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Abrir Scanner")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {

                        ProdutoDetalheHeader(viewModel.produto!!)

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Validades registradas",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                        )

                        LazyColumn {
                            items(viewModel.historicoValidades) { item ->
                                ValidadeHistoricoRow(item)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ProdutoDetalheHeader(produto: Produto) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = produto.descricao ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text("Código interno: ${produto.codigoInterno ?: "-"}")
            Text("Código de barras: ${produto.codigoBarras ?: "-"}")

            produto.validadeAtual?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Validade atual: $it",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
@Composable
fun ValidadeHistoricoRow(item: HistoricoItem) {

    val corStatus = when (item.tipoEvento.name) {
        "VENCIDO" -> Color(0xFFD32F2F)
        "VENCENDO" -> Color(0xFFF9A825)
        else -> Color(0xFF388E3C)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, corStatus.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Validade: ${item.validade}",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = item.tipoEvento.name,
                    color = corStatus,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(item.titulo)
            Text(
                item.descricao,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Data do evento: ${item.dataEvento}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
