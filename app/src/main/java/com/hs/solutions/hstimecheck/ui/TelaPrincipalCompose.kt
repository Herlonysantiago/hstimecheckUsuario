package com.hs.solutions.hstimecheck.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hs.solutions.hstimecheck.core.AppContainer
import com.hs.solutions.hstimecheck.core.ProductService
import com.hs.solutions.hstimecheck.cadastro.CadastroProdutoActivity
import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto
import kotlinx.coroutines.launch

class TelaPrincipalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        val service = AppContainer.productService

        setContent {
            MaterialTheme {
                TelaPrincipal(service)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(service: ProductService) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    val produtos by service.produtos.collectAsState()

    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    val launcherCadastro =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch { service.carregar() }
            }
        }

    LaunchedEffect(Unit) { service.carregar() }

    val listaFiltrada = produtos.filter {
        query.isBlank() ||
                it.descricao.contains(query, true) ||
                it.codigoBarras.contains(query)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                onProdutos = { },
                onPainel = { },
                onScanner = { }
            )
        }
    ) {

        Scaffold(
            modifier = Modifier
                .pointerInput(selectionMode) {
                    detectTapGestures {
                        if (selectionMode) {
                            selectedIds.clear()
                            selectionMode = false
                        }
                    }
                },

            topBar = {
                if (selectionMode) {

                    TopAppBar(
                        title = { Text("${selectedIds.size} selecionado(s)") },
                        navigationIcon = {
                            IconButton(onClick = {
                                selectedIds.clear()
                                selectionMode = false
                            }) {
                                Icon(Icons.Default.Close, null)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                scope.launch {
                                    produtos.filter { selectedIds.contains(it.id) }
                                        .forEach {
                                            service.mudarStatus(it, StatusProduto.AGUARDANDO_APROVACAO)
                                        }
                                    selectedIds.clear()
                                    selectionMode = false
                                    service.carregar()
                                }
                            }) {
                                Icon(Icons.Default.Done, null)
                            }

                            IconButton(onClick = {
                                scope.launch {
                                    produtos.filter { selectedIds.contains(it.id) }
                                        .forEach {
                                            service.remover(it.id)
                                        }
                                    selectedIds.clear()
                                    selectionMode = false
                                    service.carregar()
                                }
                            }) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    )

                } else {

                    TopAppBar(
                        title = { Text("HS TimeCheck") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, null)
                            }
                        }
                    )
                }
            },

            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        launcherCadastro.launch(
                            Intent(context, CadastroProdutoActivity::class.java)
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        ) { padding ->

            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    placeholder = { Text("Buscar produto...") }
                )

                LazyColumn(Modifier.fillMaxSize()) {
                    items(listaFiltrada) { produto ->

                        val isSelected = produto.id in selectedIds

                        ProdutoItem(
                            produto = produto,
                            isSelected = isSelected,

                            onClick = {
                                if (selectionMode) {
                                    if (isSelected) selectedIds.remove(produto.id)
                                    else selectedIds.add(produto.id)

                                    if (selectedIds.isEmpty()) {
                                        selectionMode = false
                                    }

                                } else {
                                    val intent = Intent(context, CadastroProdutoActivity::class.java)
                                    intent.putExtra("produto_id", produto.id)
                                    context.startActivity(intent)
                                }
                            },

                            onLongPress = {
                                if (!selectionMode) {
                                    selectionMode = true
                                    selectedIds.clear()
                                }
                                selectedIds.add(produto.id)
                            },

                            onDoubleClick = {
                                if (!selectionMode) {
                                    scope.launch {
                                        service.mudarStatus(produto, StatusProduto.TRABALHANDO_PRECO)
                                    }
                                } else {
                                    if (isSelected) selectedIds.remove(produto.id)
                                    else selectedIds.add(produto.id)

                                    if (selectedIds.isEmpty()) {
                                        selectionMode = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenu(
    onProdutos: () -> Unit,
    onPainel: () -> Unit,
    onScanner: () -> Unit
) {
    ModalDrawerSheet {
        Text(
            "Menu",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        NavigationDrawerItem(label = { Text("Produtos") }, selected = false, onClick = onProdutos)
        NavigationDrawerItem(label = { Text("Painel Operacional") }, selected = false, onClick = onPainel)
        NavigationDrawerItem(label = { Text("Scanner") }, selected = false, onClick = onScanner)
    }
}

@Composable
fun ProdutoItem(
    produto: Produto,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDoubleClick: () -> Unit
) {
    var lastTapTime by remember { mutableStateOf(0L) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .pointerInput(produto.id) {
                detectTapGestures(

                    onLongPress = {
                        onLongPress()
                    },

                    onTap = {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 200) {
                            onDoubleClick()
                        } else {
                            onClick()
                        }
                        lastTapTime = now
                    }
                )
            },

        colors = if (isSelected)
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        else CardDefaults.cardColors()
    ) {

        Column(Modifier.padding(12.dp)) {
            Text(produto.descricao, fontWeight = FontWeight.Bold)
            Text("Código: ${produto.codigoBarras}")
            Text("Qtd: ${produto.quantidadeAtual ?: 0}")
            Text("Validade: ${produto.validadeAtual ?: "—"}")
        }
    }
}
