package com.hs.solutions.hstimecheck.ui
// FOTO / COIL
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.Alignment

// ANDROID
import android.app.Activity
import android.content.Intent
import android.os.Bundle

// COMPOSE
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

// APP
import com.hs.solutions.hstimecheck.core.AppContainer
import com.hs.solutions.hstimecheck.core.ProductService
import com.hs.solutions.hstimecheck.cadastro.CadastroProdutoActivity
import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto

// CORROTINAS
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
fun SectionHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f))
        Text(
            "  $text  ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Divider(modifier = Modifier.weight(1f))
    }
}

@Composable
fun DrawerMenu(
    onDashboard: () -> Unit = {},
    onProdutos: () -> Unit = {},
    onImportacao: () -> Unit = {},
    onExportacao: () -> Unit = {},
    onAprovacao: () -> Unit = {},
    onGerencial: () -> Unit = {},
    onQueimaPreco: () -> Unit = {},
    onEstoque: () -> Unit = {},
    onVencimentos: () -> Unit = {},
    onHistorico: () -> Unit = {},
    onConfiguracoes: () -> Unit = {},
    onSobre: () -> Unit = {},
    onCreditos: () -> Unit = {},
) {

    ModalDrawerSheet {

        Text(
            text = "Menu",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        /* ------------------------------------
                ÁREA PRINCIPAL
        ------------------------------------ */

        NavigationDrawerItem(
            label = { Text("Dashboard") },
            selected = false,
            onClick = onDashboard
        )

        NavigationDrawerItem(
            label = { Text("Produtos") },
            selected = false,
            onClick = onProdutos
        )

        /* ------------------------------------
                FLUXOS
        ------------------------------------ */
        SectionHeader("FLUXOS")

        NavigationDrawerItem(
            label = { Text("Aprovação Comercial") },
            selected = false,
            onClick = onAprovacao
        )

        NavigationDrawerItem(
            label = { Text("Gerencial (Pendências)") },
            selected = false,
            onClick = onGerencial
        )

        NavigationDrawerItem(
            label = { Text("Trabalhando Preço / Queima de Estoque") },
            selected = false,
            onClick = onQueimaPreco
        )

        NavigationDrawerItem(
            label = { Text("Verificação de Estoque") },
            selected = false,
            onClick = onEstoque
        )

        NavigationDrawerItem(
            label = { Text("Produtos Vencendo / Vencidos") },
            selected = false,
            onClick = onVencimentos
        )


        /* ------------------------------------
                DADOS
        ------------------------------------ */
        SectionHeader("DADOS")

        NavigationDrawerItem(
            label = { Text("Importar Planilha") },
            selected = false,
            onClick = onImportacao
        )

        NavigationDrawerItem(
            label = { Text("Exportar Dados") },
            selected = false,
            onClick = onExportacao
        )


        /* ------------------------------------
                RELATÓRIOS
        ------------------------------------ */
        SectionHeader("RELATÓRIOS")

        NavigationDrawerItem(
            label = { Text("Histórico Geral") },
            selected = false,
            onClick = onHistorico
        )


        /* ------------------------------------
                SISTEMA
        ------------------------------------ */
        SectionHeader("SISTEMA")

        NavigationDrawerItem(
            label = { Text("Configurações") },
            selected = false,
            onClick = onConfiguracoes
        )

        NavigationDrawerItem(
            label = { Text("Sobre") },
            selected = false,
            onClick = onSobre
        )

        NavigationDrawerItem(
            label = { Text("Créditos") },
            selected = false,
            onClick = onCreditos
        )
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
                    onLongPress = { onLongPress() },
                    onTap = {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 200) onDoubleClick()
                        else onClick()
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /* ============================================================
               FOTO PREMIUM QUADRADA (NÃO REMOVE NENHUM CÓDIGO EXISTENTE)
               ============================================================ */
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0x22000000),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                if (!produto.fotoUrl.isNullOrBlank()) {

                    AsyncImage(
                        model = produto.fotoUrl,
                        contentDescription = "Foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Sem foto",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            /* ============================================================
               INFORMAÇÕES DO PRODUTO (SEU CÓDIGO ORIGINAL, INTACTO)
               ============================================================ */
            Column(Modifier.weight(1f)) {

                Text(produto.descricao, fontWeight = FontWeight.Bold)

                Text("Código Barras: ${produto.codigoBarras}")

                Text(
                    "Código Interno: ${produto.codigoInterno ?: "—"}"
                )


                // Lógica para exibir quantidade (CX + UN quando possível)
                val total = produto.quantidadeAtual ?: 0
                val qpc = produto.quantidadePorCaixa ?: 0

                val quantidadeTexto =
                    if (qpc > 0 && total > 0) {
                        val cx = total / qpc
                        val un = total % qpc
                        "Qtd: ${cx} cx • ${un} un (cx de ${qpc})"
                    } else {
                        "Qtd: ${total} un"
                    }

                Text(quantidadeTexto)
                Text("Validade: ${produto.validadeAtual ?: "—"}")
            }
        }
    }
}

