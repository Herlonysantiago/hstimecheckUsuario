package com.hs.solutions.hstimecheck.ui

import androidx.compose.foundation.clickable
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.hs.solutions.hstimecheck.core.AppContainer
import com.hs.solutions.hstimecheck.core.ProductLookupService
import com.hs.solutions.hstimecheck.core.ProductService
import com.hs.solutions.hstimecheck.cadastro.CadastroProdutoActivity
import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto
import com.hs.solutions.hstimecheck.scanner.ScannerActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/* ---------------------------------------------------------------------- */
/* ACTIVITY PRINCIPAL                                                     */
/* ---------------------------------------------------------------------- */

class TelaPrincipalActivity : ComponentActivity() {

    private lateinit var lookup: ProductLookupService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        val productService = AppContainer.productService

        lookup = ProductLookupService(this)
        lifecycleScope.launch { lookup.preload() }

        setContent {
            MaterialTheme {
                TelaPrincipalB(productService)
            }
        }
    }
}

/* ---------------------------------------------------------------------- */
/* TOP BARS                                                               */
/* ---------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
    titulo: String,
    onSearchChanged: (String) -> Unit,
    onToggleView: () -> Unit
) {
    TopAppBar(
        title = { Text(titulo) },
        actions = {
            IconButton(onClick = onToggleView) {
                Icon(Icons.Default.ViewModule, contentDescription = "Alterar visualização")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    count: Int,
    onExit: () -> Unit,
    onSendApproval: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "Sair")
            }
        },
        title = { Text("$count selecionados") },
        actions = {
            IconButton(onClick = onSendApproval) {
                Icon(Icons.Default.Send, contentDescription = "Enviar para aprovação")
            }
        }
    )
}

/* ---------------------------------------------------------------------- */
/* FUNÇÕES ÚTEIS                                                          */
/* ---------------------------------------------------------------------- */

fun diasAteValidadeFromStringB(dataStr: String?): Long? {
    if (dataStr.isNullOrBlank()) return null
    return try {
        val d = LocalDate.parse(dataStr)
        ChronoUnit.DAYS.between(LocalDate.now(), d)
    } catch (e: DateTimeParseException) {
        null
    }
}

fun corPorDiasB(dias: Long?): Color {
    if (dias == null) return Color.LightGray
    return when {
        dias > 15 -> Color(0xFFBEE6B0)
        dias in 8..15 -> Color(0xFFF4E3B2)
        dias in 3..7 -> Color(0xFFFFC4B2)
        dias == 2L -> Color(0xFFFF9A8A)
        dias == 1L -> Color(0xFFFF6B6B)
        dias == 0L -> Color(0xFFFF3B3B)
        dias < 0 -> Color(0xFF8B0000)
        else -> Color.LightGray
    }
}

/* ---------------------------------------------------------------------- */
/* DASHBOARD                                                               */
/* ---------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardB(produtos: List<Produto>) {
    val total = produtos.size
    val vencendo = produtos.count {
        val d = diasAteValidadeFromStringB(it.validadeAtual)
        d != null && d in 0..15
    }
    val aguardando = produtos.count { it.status == StatusProduto.AGUARDANDO_APROVACAO }
    val trabalhando = produtos.count { it.status == StatusProduto.TRABALHANDO_PRECO }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Card(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = CardDefaults.cardColors(Color(0xFFE3F2FD))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text("$total itens", color = Color.Gray)
                }
            }

            Card(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = CardDefaults.cardColors(Color(0xFFFFF3E0))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Vencendo (0–15 dias)", fontWeight = FontWeight.Bold)
                    Text("$vencendo itens", color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Card(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = CardDefaults.cardColors(Color(0xFFE8F5E9))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Aguardando Aprovação", fontWeight = FontWeight.Bold)
                    Text("$aguardando itens", color = Color.Gray)
                }
            }

            Card(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = CardDefaults.cardColors(Color(0xFFFFF8E1))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Trabalhando Preço", fontWeight = FontWeight.Bold)
                    Text("$trabalhando itens", color = Color.Gray)
                }
            }
        }
    }
}

/* ---------------------------------------------------------------------- */
/* AÇÕES E CARD DO PRODUTO                                                */
/* ---------------------------------------------------------------------- */

fun abrirProdutoB(context: android.content.Context, id: String) {
    val intent = Intent(context, CadastroProdutoActivity::class.java).apply {
        putExtra("produto_id", id)
    }
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun abrirMenuOpcoesB(context: android.content.Context, produto: Produto, onClose: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Ações – ${produto.descricao}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(16.dp))

            @Composable
            fun ItemAcao(icon: ImageVector, texto: String, click: () -> Unit) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp).clickable {
                        click()
                        onClose()
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF1976D2))
                    Spacer(Modifier.width(12.dp))
                    Text(texto, fontSize = 17.sp)
                }
            }

            val service = AppContainer.productService

            ItemAcao(Icons.Default.Send, "Enviar para Aprovação") {
                CoroutineScope(Dispatchers.IO).launch {
                    service.mudarStatus(produto, StatusProduto.AGUARDANDO_APROVACAO)
                }
            }

            ItemAcao(Icons.Default.Work, "Trabalhar Preço") {
                CoroutineScope(Dispatchers.IO).launch {
                    service.mudarStatus(produto, StatusProduto.TRABALHANDO_PRECO)
                }
            }

            ItemAcao(Icons.Default.Inventory, "Verificação de Estoque") {
                CoroutineScope(Dispatchers.IO).launch {
                    service.mudarStatus(produto, StatusProduto.VERIFICACAO_ESTOQUE)
                }
            }
        }
    }
}

/* ---------------------------------------------------------------------- */
/* CARD DO PRODUTO — CLIQUES CORRETOS                                     */
/* ---------------------------------------------------------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProdutoCardB(
    produto: Produto,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val dias = diasAteValidadeFromStringB(produto.validadeAtual)
    val corReforco by animateColorAsState(corPorDiasB(dias))

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    Log.e("CARD_CLICK", "onClick produto=${produto.id}")
                    onClick()
                },
                onLongClick = {
                    Log.e("CARD_CLICK", "onLongClick produto=${produto.id}")
                    onLongClick()
                },
                onDoubleClick = {
                    Log.e("CARD_CLICK", "onDoubleClick produto=${produto.id}")
                    onDoubleClick()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(corPorDiasB(dias))
            )

            Column(Modifier.padding(12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(produto.descricao, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Spacer(Modifier.weight(1f))
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2196F3))
                    }
                }
                Text("Código: ${produto.codigoBarras}")
                Text("Qtd: ${produto.quantidadeAtual ?: 0}")
                Text("Validade: ${produto.validadeAtual ?: "—"}")

                when (produto.status) {
                    StatusProduto.TRABALHANDO_PRECO -> Text("TRABALHANDO PREÇO", color = Color(0xFFEF6C00))
                    StatusProduto.AGUARDANDO_APROVACAO -> Text("AGUARDANDO APROVAÇÃO", color = Color(0xFFC62828))
                    StatusProduto.VENCENDO -> Text("VENCENDO", color = Color(0xFFFF9800))
                    StatusProduto.VERIFICACAO_ESTOQUE -> Text("VERIFICAÇÃO DE ESTOQUE", color = Color(0xFF0288D1))
                    StatusProduto.NORMAL -> {}
                }
            }

            IconButton(onClick = { /* Ações do botão lateral */ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Mais")
            }
        }
    }
}

/* ---------------------------------------------------------------------- */
/* EMPTY STATE                                                            */
/* ---------------------------------------------------------------------- */

@Composable
fun EmptyStateB(onAddClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF90A4AE), modifier = Modifier.size(120.dp))
        Spacer(Modifier.height(16.dp))
        Text("Nenhum produto cadastrado", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Use o scanner para começar o cadastro.", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = ButtonDefaults.buttonColors(Color(0xFF1976D2))
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Adicionar Produto")
        }
    }
}

/* ---------------------------------------------------------------------- */
/* TELA PRINCIPAL                                                         */
/* ---------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipalB(productService: ProductService) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val service = remember { productService }

    var isGrid by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }
    var showSheet by remember { mutableStateOf(false) }

    val produtos by service.produtos.collectAsState()

    val launcherCadastro = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) scope.launch { service.carregar() }
    }

    val launcherScanner = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val codigo = result.data?.getStringExtra("codigo_barras") ?: return@rememberLauncherForActivityResult
            val descricao = result.data?.getStringExtra("descricao") ?: ""
            val interno = result.data?.getStringExtra("codigo_interno") ?: ""

            val intent = Intent(context, CadastroProdutoActivity::class.java).apply {
                putExtra("codigo_barras", codigo)
                putExtra("descricao", descricao)
                putExtra("codigo_interno", interno)
            }

            launcherCadastro.launch(intent)
        }
    }

    LaunchedEffect(Unit) { service.carregar() }

    val produtosFiltrados = produtos.filter {
        query.isBlank() ||
                it.descricao.contains(query, ignoreCase = true) ||
                it.codigoBarras.contains(query) ||
                (it.codigoInterno?.contains(query) ?: false)
    }

    Scaffold(
        topBar = {
            if (!selectionMode) {
                DefaultTopBar(
                    titulo = "HS TimeCheck",
                    onSearchChanged = { query = it },
                    onToggleView = { isGrid = !isGrid }
                )
            } else {
                SelectionTopBar(
                    count = selectedIds.size,
                    onExit = {
                        selectionMode = false
                        selectedIds.clear()
                    },
                    onSendApproval = {}
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->

        Column(Modifier.padding(padding).fillMaxSize()) {

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Buscar por nome, código, interno...") },
                singleLine = true
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { isGrid = !isGrid }) {
                    Icon(if (isGrid) Icons.Default.ViewModule else Icons.Default.ViewList, contentDescription = null)
                }
            }

            DashboardB(produtos)

            if (produtosFiltrados.isEmpty()) {
                EmptyStateB(onAddClick = { showSheet = true })
                return@Column
            }

            if (isGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(250.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(produtosFiltrados) { produto ->

                        var abrirMenu by remember { mutableStateOf(false) }

                        ProdutoCardB(
                            produto = produto,
                            isSelected = selectedIds.contains(produto.id),
                            onClick = {
                                if (selectionMode) {
                                    if (selectedIds.contains(produto.id)) selectedIds.remove(produto.id)
                                    else selectedIds.add(produto.id)
                                } else {
                                    abrirProdutoB(context, produto.id)
                                }
                            },
                            onLongClick = {
                                if (!selectionMode) selectionMode = true
                                selectedIds.add(produto.id)
                            },
                            onDoubleClick = { abrirMenu = true }
                        )

                        if (abrirMenu) abrirMenuOpcoesB(context, produto) { abrirMenu = false }
                    }
                }

            } else {

                LazyColumn(Modifier.fillMaxSize()) {
                    items(produtosFiltrados) { produto ->

                        var abrirMenu by remember { mutableStateOf(false) }

                        ProdutoCardB(
                            produto = produto,
                            isSelected = selectedIds.contains(produto.id),
                            onClick = {
                                if (selectionMode) {
                                    if (selectedIds.contains(produto.id)) selectedIds.remove(produto.id)
                                    else selectedIds.add(produto.id)
                                } else {
                                    abrirProdutoB(context, produto.id)
                                }
                            },
                            onLongClick = {
                                if (!selectionMode) selectionMode = true
                                selectedIds.add(produto.id)
                            },
                            onDoubleClick = { abrirMenu = true }
                        )

                        if (abrirMenu) abrirMenuOpcoesB(context, produto) { abrirMenu = false }
                    }
                }
            }
        }
    }

    if (showSheet) {

        ModalBottomSheet(onDismissRequest = { showSheet = false }) {

            Column(Modifier.fillMaxWidth().padding(24.dp)) {

                Text("Adicionar Produto", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        showSheet = false
                        launcherScanner.launch(Intent(context, ScannerActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar via Scanner")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        showSheet = false
                        launcherCadastro.launch(Intent(context, CadastroProdutoActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar Manualmente")
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}
