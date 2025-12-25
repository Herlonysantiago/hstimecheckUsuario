package com.hs.solutions.hstimecheck_2_0.trabalhando_preco

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hs.solutions.hstimecheck_2_0.models.Produto
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTrabalhandoPreco(
    produto: Produto,
    onDismiss: () -> Unit,
    onWhatsapp: () -> Unit,
    onPrecoNegociacao: () -> Unit,
    onEnviarAprovacao: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = produto.descricao,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(16.dp))

            ListItem(
                headlineContent = { Text("Enviar WhatsApp") },
                modifier = Modifier.clickable { onWhatsapp() }
            )

            ListItem(
                headlineContent = { Text("Preço em negociação") },
                modifier = Modifier.clickable { onPrecoNegociacao() }
            )

            ListItem(
                headlineContent = { Text("Enviar para aprovação") },
                modifier = Modifier.clickable { onEnviarAprovacao() }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabalhandoPrecoScreen(
    viewModel: TrabalhandoPrecoViewModel
) {
    val context = LocalContext.current
    val produtos by viewModel.produtos.collectAsState()

    val produtosSelecionados = remember { mutableStateListOf<Produto>() }
    var produtoMenu by remember { mutableStateOf<Produto?>(null) }

    val modoSelecao = produtosSelecionados.isNotEmpty()

    fun alternarSelecao(produto: Produto) {
        if (produtosSelecionados.contains(produto)) {
            produtosSelecionados.remove(produto)
        } else {
            produtosSelecionados.add(produto)
        }
    }

    fun enviarWhatsapp(mensagem: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://wa.me/?text=${Uri.encode(mensagem)}")
        )
        context.startActivity(intent)
    }

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (modoSelecao) {
                        Text("${produtosSelecionados.size} selecionados")
                    } else {
                        Text("Trabalhando Preço")
                    }
                },
                navigationIcon = {
                    if (modoSelecao) {
                        IconButton(onClick = { produtosSelecionados.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar")
                        }
                    }
                },
                actions = {
                    if (modoSelecao) {

                        // 🛒 Enviar para CLIENTES (WhatsApp em lote)
                        IconButton(
                            onClick = {
                                val msgClientes = buildString {
                                    append("📢 OFERTAS DISPONÍVEIS\n\n")
                                    produtosSelecionados.forEach {
                                        append(
                                            "• ${it.descricao}\n" +
                                                    "  Validade: ${it.validadeAtual ?: "-"}\n"+
                                                    "  Preço : ${it.precoAtual?:"-"}\n"
                                        )
                                    }
                                }
                                enviarWhatsapp(msgClientes)
                                produtosSelecionados.clear()
                            }
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Enviar para clientes"
                            )
                        }

                        // 📲 WhatsApp Gerente / Comprador
                        IconButton(
                            onClick = {
                                val msg = buildString {
                                    append("⚠️ PRODUTOS PARA MELHORAR PREÇO\n\n")
                                    produtosSelecionados.forEach {
                                        append(
                                            "• ${it.descricao}\n" +
                                                    "  Código: ${it.codigoInterno ?: "-"}\n" +
                                                    "  Validade: ${it.validadeAtual ?: "-"}\n" +
                                                    "  Estoque: ${it.quantidadeAtual ?: 0}\n\n"
                                        )
                                    }
                                    append("📱 HS TimeCheck\n")
                                }
                                enviarWhatsapp(msg)
                                produtosSelecionados.clear()
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "WhatsApp")
                        }

                        // ✅ Enviar para aprovação (comprador)
                        IconButton(
                            onClick = {
                                produtosSelecionados.forEach {
                                    viewModel.enviarParaComprador(it)
                                }
                                produtosSelecionados.clear()
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Enviar aprovação")
                        }
                    }
                }
            )
        }
    ) { padding ->

        if (produtos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum produto trabalhando preço")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(produtos) { produto ->
                    ItemTrabalhandoPreco(
                        produto = produto,
                        selecionado = produtosSelecionados.contains(produto),
                        modoSelecao = modoSelecao,
                        onSelecionar = { alternarSelecao(produto) },
                        onAbrirMenu = { produtoMenu = produto }
                    )
                }
            }
        }
    }

    // 🔽 Menu individual
    produtoMenu?.let { produto ->
        MenuTrabalhandoPreco(
            produto = produto,
            onDismiss = { produtoMenu = null },
            onWhatsapp = {
                enviarWhatsapp(
                    """
                    Produto: ${produto.codigoInterno ?: "-"} - ${produto.descricao}
                    Validade: ${produto.validadeAtual ?: "-"}
                    Estoque: ${produto.quantidadeAtual ?: 0}

                    Pode verificar melhor preço?
                    """.trimIndent()
                )
                produtoMenu = null
            },
            onPrecoNegociacao = {
                viewModel.marcarPrecoEmNegociacao(produto, null)
                produtoMenu = null
            },
            onEnviarAprovacao = {
                viewModel.enviarParaComprador(produto)
                produtoMenu = null
            }
        )
    }
}
