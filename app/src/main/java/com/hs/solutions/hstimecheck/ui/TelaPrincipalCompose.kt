package com.hs.solutions.hstimecheck.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.Intent
import com.hs.solutions.hstimecheck.scanner.ScannerActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hs.solutions.hstimecheck.core.ProductRepositoryImpl

// Lazy imports
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items as gridItems

// Material 3
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*

// Animation
import androidx.compose.animation.animateColorAsState

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

import com.hs.solutions.hstimecheck.cadastro.CadastroProdutoActivity

import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

import com.hs.solutions.hstimecheck.models.Produto
import com.hs.solutions.hstimecheck.models.StatusProduto
import com.hs.solutions.hstimecheck.core.ProductService
import androidx.compose.material3.ExperimentalMaterial3Api


/* ----------------------------- UTIL -------------------------------- */

fun diasAteValidadeFromString(dataStr: String?): Long? {
    if (dataStr.isNullOrBlank()) return null
    return try {
        val d = LocalDate.parse(dataStr)
        ChronoUnit.DAYS.between(LocalDate.now(), d)
    } catch (e: DateTimeParseException) {
        null
    }
}

fun corPorDias(dias: Long?): Color {
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


/* ----------------------------- ACTIVITY ----------------------------- */

class TelaPrincipalActivity : ComponentActivity() {

    private val productService = ProductService(ProductRepositoryImpl())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TelaPrincipal(productService)
            }
        }
    }
}


/* ----------------------------- TELA PRINCIPAL ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(productService: ProductService) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isGrid by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val produtos by productService.produtos.collectAsState()


    /* ---------------------- LAUNCHERS CORRIGIDOS ---------------------- */

    // 📷 SCANNER
    val launcherScanner = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val codigo = result.data?.getStringExtra("codigo_barras") ?: return@rememberLauncherForActivityResult

            scope.launch {
                productService.inserirOuAtualizar(
                    Produto(
                        codigoBarras = codigo,
                        descricao = "Produto via scanner",
                        quantidadeAtual = 1
                    )
                )
            }
        }
    }

    // 📝 CADASTRO MANUAL
    val launcherCadastro = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch {
                productService.carregar()   // atualiza lista
            }
        }
    }


    /* ----------------------- FILTRO ----------------------- */

    val produtosFiltrados = produtos.filter {
        query.isBlank() ||
                it.descricao.contains(query, ignoreCase = true) ||
                it.codigoBarras.contains(query) ||
                (it.codigoInterno?.contains(query) ?: false)
    }

    LaunchedEffect(Unit) { productService.carregar() }


    /* ----------------------- UI ----------------------- */

    Scaffold(
        topBar = {
            if (!selectionMode)
                DefaultTopBar(
                    titulo = "HS TimeCheck",
                    onSearchChanged = { query = it },
                    onToggleView = { isGrid = !isGrid }
                )
            else
                SelectionTopBar(
                    count = selectedIds.size,
                    onExit = {
                        selectionMode = false
                        selectedIds.clear()
                    },
                    onSendApproval = {
                        selectedIds.forEach { id ->
                            produtos.find { it.id == id }?.let { p ->
                                scope.launch {
                                    productService.mudarStatus(p, StatusProduto.AGUARDANDO_APROVACAO)
                                }
                            }
                        }
                        selectedIds.clear()
                        selectionMode = false
                    }
                )
        },

        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                placeholder = { Text("Buscar por nome, código, interno...") },
                singleLine = true
            )


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { isGrid = !isGrid }) {
                    Icon(if (isGrid) Icons.Filled.ViewModule else Icons.Filled.ViewList, null)
                }
            }

            Dashboard()


            if (produtosFiltrados.isEmpty()) {
                EmptyState(onAddClick = { showSheet = true })
                return@Column
            }


            if (isGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(250.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    gridItems(produtosFiltrados) { produto ->
                        ProdutoCard(
                            produto = produto,
                            isSelected = selectedIds.contains(produto.id),
                            onClick = {
                                if (selectionMode) {
                                    if (selectedIds.contains(produto.id)) selectedIds.remove(produto.id)
                                    else selectedIds.add(produto.id)
                                }
                            },
                            onLongClick = {
                                if (!selectionMode) selectionMode = true
                                selectedIds.add(produto.id)
                            },
                            onDoubleClick = {}
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(produtosFiltrados) { produto ->
                        ProdutoCard(
                            produto = produto,
                            isSelected = selectedIds.contains(produto.id),
                            onClick = {
                                if (selectionMode) {
                                    if (selectedIds.contains(produto.id)) selectedIds.remove(produto.id)
                                    else selectedIds.add(produto.id)
                                }
                            },
                            onLongClick = {
                                if (!selectionMode) selectionMode = true
                                selectedIds.add(produto.id)
                            },
                            onDoubleClick = {}
                        )
                    }
                }
            }
        }
    }


    /* ------------------------- BOTTOM SHEET -------------------------- */

    if (showSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showSheet = false }
        ) {

            Column(Modifier.fillMaxWidth().padding(24.dp)) {

                Text("Adicionar Produto", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        showSheet = false
                        val intent = Intent(context, ScannerActivity::class.java)
                        launcherScanner.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CameraAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar via Scanner")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        showSheet = false
                        val intent = Intent(context, CadastroProdutoActivity::class.java)
                        launcherCadastro.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Filled.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar Manualmente")
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}


/* ---------------------- COMPONENTES DE UI ---------------------- */

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
            IconButton(onClick = onToggleView) { Icon(Icons.Filled.ViewModule, null) }
            IconButton(onClick = { }) { Icon(Icons.Filled.Refresh, null) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(count: Int, onExit: () -> Unit, onSendApproval: () -> Unit) {
    TopAppBar(
        title = { Text("$count selecionado(s)") },
        navigationIcon = {
            IconButton(onClick = onExit) { Icon(Icons.Filled.Close, null) }
        },
        actions = {
            IconButton(onClick = onSendApproval) { Icon(Icons.Filled.Send, null) }
        }
    )
}


@Composable
fun ProdutoCard(
    produto: Produto,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDoubleClick: () -> Unit
) {

    val dias = diasAteValidadeFromString(produto.validadeAtual)
    val corReforco by animateColorAsState(corPorDias(dias))

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() },
                    onDoubleTap = { onDoubleClick() }
                )
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {

        Row(Modifier.fillMaxWidth()) {

            Box(
                Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(corReforco)
            )

            Column(Modifier.padding(12.dp).weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(produto.descricao, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Spacer(Modifier.weight(1f))

                    if (isSelected)
                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2196F3))
                }

                Text("Código: ${produto.codigoBarras}")
                Text("Qtd: ${produto.quantidadeAtual ?: 0}")
                Text("Validade: ${produto.validadeAtual ?: "—"}")

                when (produto.status) {

                    StatusProduto.TRABALHANDO_PRECO ->
                        Text(
                            "TRABALHANDO PREÇO",
                            color = Color(0xFFEF6C00)
                        )

                    StatusProduto.AGUARDANDO_APROVACAO ->
                        Text(
                            "AGUARDANDO APROVAÇÃO",
                            color = Color(0xFFC62828)
                        )

                    StatusProduto.VENCENDO ->
                        Text(
                            "VENCENDO",
                            color = Color(0xFFFF9800)
                        )

                    StatusProduto.VERIFICACAO_ESTOQUE ->
                        Text(
                            "VERIFICAÇÃO DE ESTOQUE",
                            color = Color(0xFF0288D1)
                        )

                    StatusProduto.NORMAL -> {
                        // Não exibe nada, produto sem alerta
                    }
                }




            }

            IconButton(onClick = { }) {
                Icon(Icons.Filled.MoreVert, null)
            }
        }
    }
}


@Composable
fun Dashboard() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {

        Text("Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

            Card(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = CardDefaults.cardColors(Color(0xFFE3F2FD))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Vencendo", fontWeight = FontWeight.Bold)
                    Text("0 itens", color = Color.Gray)
                }
            }

            Card(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = CardDefaults.cardColors(Color(0xFFFFEBEE))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Aprovação", fontWeight = FontWeight.Bold)
                    Text("0 pendentes", color = Color.Gray)
                }
            }
        }
    }
}


@Composable
fun EmptyState(onAddClick: () -> Unit) {

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(Icons.Filled.Inventory, null, tint = Color(0xFF90A4AE), modifier = Modifier.size(120.dp))

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
            Icon(Icons.Filled.CameraAlt, null)
            Spacer(Modifier.width(8.dp))
            Text("Adicionar Produto")
        }
    }
}
