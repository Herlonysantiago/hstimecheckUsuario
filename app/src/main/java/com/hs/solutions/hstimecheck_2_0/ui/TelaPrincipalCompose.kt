package com.hs.solutions.hstimecheck_2_0.ui

// ================= ANDROID =================
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.io.File
// ================= COMPOSE =================
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ================= COIL =================
import coil.compose.AsyncImage

// ================= APP =================
import com.hs.solutions.hstimecheck_2_0.configuracoes.ConfiguracoesSistemaActivity

import com.hs.solutions.hstimecheck_2_0.R
import com.hs.solutions.hstimecheck_2_0.core.AppContainer
import com.hs.solutions.hstimecheck_2_0.core.AppPreferences
import com.hs.solutions.hstimecheck_2_0.core.ProductService
import com.hs.solutions.hstimecheck_2_0.cadastro.CadastroProdutoActivity
import com.hs.solutions.hstimecheck_2_0.aprovacao.AprovacaoComercialActivity
import com.hs.solutions.hstimecheck_2_0.models.Produto
import com.hs.solutions.hstimecheck_2_0.models.StatusProduto
import com.hs.solutions.hstimecheck_2_0.historico.HistoricoGeralActivity
import com.hs.solutions.hstimecheck_2_0.ui.FullImageActivity
// ================= CORROTINAS =================
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
// =======================================================
// ACTIVITY PRINCIPAL (ÚNICO onCreate — CORRETO)
// =======================================================
class TelaPrincipalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(this)
        val service = AppContainer.productService
        val fotoUrl = intent.getStringExtra("fotoUrl")

        setContent {
            MaterialTheme {
                AplicarConfiguracoesGlobais()
                TelaPrincipal(service)
            }
        }
    }
}

// =======================================================
// CONFIGURAÇÕES GLOBAIS (DataStore)
// =======================================================
@Composable
fun AplicarConfiguracoesGlobais() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AppPreferences.read(context, AppPreferences.MODO_ONLINE, true).collect { }
    }

    LaunchedEffect(Unit) {
        AppPreferences.read(context, AppPreferences.ALERTA_VALIDADE, true).collect { }
    }

    LaunchedEffect(Unit) {
        AppPreferences.read(context, AppPreferences.ALERTA_APROVACAO, true).collect { }
    }
}

// =======================================================
// FUNÇÕES AUXILIARES (SUAS)
// =======================================================
fun getDiasRestantes(validade: String?): Int {
    if (validade.isNullOrBlank()) return Int.MAX_VALUE

    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val validadeDate = sdf.parse(validade) ?: return Int.MAX_VALUE

        val hojeCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val validadeCal = Calendar.getInstance().apply {
            time = validadeDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = validadeCal.timeInMillis - hojeCal.timeInMillis
        (diffMillis / (1000 * 60 * 60 * 24)).toInt()

    } catch (_: Exception) {
        Int.MAX_VALUE
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
        Divider(Modifier.weight(1f))
        Text(
            "  $text  ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Divider(Modifier.weight(1f))
    }
}

// =======================================================
// DRAWER MENU (SEU, INTEIRO)
// =======================================================
@OptIn(ExperimentalMaterial3Api::class)
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
    onCreditos: () -> Unit = {}
) {
    ModalDrawerSheet {

        Text("Menu", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)

        NavigationDrawerItem(
            label = { Text("Dashboard") },
            icon = { Icon(Icons.Default.Home, null) },
            selected = false,
            onClick = onDashboard
        )

        NavigationDrawerItem(
            label = { Text("Produtos") },
            icon = { Icon(Icons.Default.Inventory, null) },
            selected = false,
            onClick = onProdutos
        )

        SectionHeader("FLUXOS")

        NavigationDrawerItem(
            label = { Text("Aprovação Comercial") },
            icon = { Icon(Icons.Default.ThumbUp, null) },
            selected = false,
            onClick = onAprovacao
        )

        NavigationDrawerItem(
            label = { Text("Gerencial (Pendências)") },
            icon = { Icon(Icons.Default.FactCheck, null) },
            selected = false,
            onClick = onGerencial
        )

        NavigationDrawerItem(
            label = { Text("Trabalhando Preço / Queima de Estoque") },
            icon = { Icon(Icons.Default.LocalFireDepartment, null) },
            selected = false,
            onClick = onQueimaPreco
        )

        NavigationDrawerItem(
            label = { Text("Verificação de Estoque") },
            icon = { Icon(Icons.Default.BarChart, null) },
            selected = false,
            onClick = onEstoque
        )

        NavigationDrawerItem(
            label = { Text("Produtos Vencendo / Vencidos") },
            icon = { Icon(Icons.Default.AccessTime, null) },
            selected = false,
            onClick = onVencimentos
        )

        SectionHeader("DADOS")

        NavigationDrawerItem(
            label = { Text("Importar Planilha") },
            icon = { Icon(Icons.Default.FileUpload, null) },
            selected = false,
            onClick = onImportacao
        )

        NavigationDrawerItem(
            label = { Text("Exportar Dados") },
            icon = { Icon(Icons.Default.FileDownload, null) },
            selected = false,
            onClick = onExportacao
        )

        SectionHeader("RELATÓRIOS")

        NavigationDrawerItem(
            label = { Text("Histórico Geral") },
            icon = { Icon(Icons.Default.History, null) },
            selected = false,
            onClick = onHistorico
        )

        SectionHeader("SISTEMA")

        NavigationDrawerItem(
            label = { Text("Configurações") },
            icon = { Icon(Icons.Default.Settings, null) },
            selected = false,
            onClick = onConfiguracoes
        )

        NavigationDrawerItem(
            label = { Text("Sobre") },
            icon = { Icon(Icons.Default.Info, null) },
            selected = false,
            onClick = onSobre
        )

        NavigationDrawerItem(
            label = { Text("Créditos") },
            icon = { Icon(Icons.Default.Star, null) },
            selected = false,
            onClick = onCreditos
        )
    }
}

// =======================================================
// TELA PRINCIPAL (SUA LÓGICA COMPLETA)
// =======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(service: ProductService) {

    // ---------------- STATE ----------------
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    val produtos by service.produtos.collectAsState()

    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    val launcherCadastro =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch { service.carregar() }
            }
        }

    LaunchedEffect(Unit) {
        service.carregar()
    }

    // ---------------- FILTRO ----------------
    val listaFiltrada = produtos.filter {
        query.isBlank() ||
                it.descricao.contains(query, ignoreCase = true) ||
                it.codigoBarras.contains(query)
    }

    val grupos = listaFiltrada.groupBy {
        getDiasRestantes(it.validadeAtual)
    }

    val chavesOrdenadas = grupos.keys.sorted()

    // ---------------- DRAWER ----------------
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                onAprovacao = {
                    context.startActivity(
                        Intent(context, AprovacaoComercialActivity::class.java)
                    )
                },
                onConfiguracoes = {
                    context.startActivity(
                        Intent(context, ConfiguracoesSistemaActivity::class.java)
                    )
                },
                onHistorico = {
                    context.startActivity(
                        Intent(context, HistoricoGeralActivity::class.java)
                    )
                }
            )
        }
    ) {

        // ---------------- SCAFFOLD ÚNICO ----------------
        Scaffold(
            topBar = {

                if (selectionMode) {
                    TopAppBar(
                        title = { Text("${selectedIds.size} selecionado(s)") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            ) {
                                Icon(Icons.Default.Close, null)
                            }
                        },
                        actions = {

                            IconButton(onClick = {
                                scope.launch {
                                    selectedIds.forEach { id ->
                                        produtos.find { it.id == id }?.let {
                                            service.mudarStatus(
                                                it,
                                                StatusProduto.AGUARDANDO_APROVACAO
                                            )
                                        }
                                    }
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Default.ThumbUp, null)
                            }

                            IconButton(onClick = {
                                scope.launch {
                                    selectedIds.forEach { id ->
                                        produtos.find { it.id == id }?.let {
                                            service.mudarStatus(
                                                it,
                                                StatusProduto.TRABALHANDO_PRECO
                                            )
                                        }
                                    }
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Default.LocalFireDepartment, null)
                            }

                            IconButton(onClick = {
                                scope.launch {
                                    selectedIds.forEach { id ->
                                        produtos.find { it.id == id }?.let {
                                            service.mudarStatus(
                                                it,
                                                StatusProduto.VERIFICACAO_ESTOQUE
                                            )
                                        }
                                    }
                                    selectionMode = false
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Default.Inventory, null)
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text("HS TimeCheck") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch { drawerState.open() }
                                }
                            ) {
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

            // ---------------- CONTEÚDO ----------------
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    placeholder = { Text("Buscar produto...") }
                )

                LazyColumn {

                    chavesOrdenadas.forEach { chave ->

                        val grupo = grupos[chave] ?: emptyList()

                        val label = when {
                            chave == Int.MAX_VALUE -> "Sem validade"
                            chave < 0 -> "Vencido"
                            chave == 0 -> "Hoje"
                            chave == 1 -> "1 dia restante"
                            else -> "$chave dias restantes"
                        }

                        item {
                            SectionHeader(label)
                        }

                        items(grupo) { produto ->

                            val isSelected = produto.id in selectedIds

                            ProdutoItem(
                                produto = produto,
                                isSelected = isSelected,
                                onClick = {
                                    if (selectionMode) {
                                        if (isSelected) {
                                            selectedIds.remove(produto.id)
                                        } else {
                                            selectedIds.add(produto.id)
                                        }
                                        if (selectedIds.isEmpty()) {
                                            selectionMode = false
                                        }
                                    } else {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                CadastroProdutoActivity::class.java
                                            ).putExtra("produto_id", produto.id)
                                        )
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
                                    scope.launch {
                                        service.mudarStatus(
                                            produto,
                                            StatusProduto.TRABALHANDO_PRECO
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHistoricoGeral(service: ProductService) {

    val produtos by service.produtos.collectAsState()

    val historicoGeral = produtos
        .flatMap { p -> p.historico.map { h -> p.descricao to h } }
        .sortedByDescending { it.second.dataEvento }

    LaunchedEffect(Unit) { service.carregar() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Histórico Geral") })
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            if (historicoGeral.isEmpty()) {
                item {
                    Text(
                        "Nenhum histórico registrado",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(historicoGeral) { (descricao, item) ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {

                        // 🔹 TÍTULO DO EVENTO
                        Text(
                            text = item.titulo,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        // 🔹 CÓDIGOS
                        Text(
                            text = buildString {
                                item.codigoInterno?.let { append("CI: $it  ") }
                                item.codigoBarras?.let { append("CB: $it") }
                            },
                            style = MaterialTheme.typography.labelSmall
                        )

                        // 🔹 VALIDADE
                        item.validade?.let {
                            Text(
                                text = "Validade: $it",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // 🔹 DESCRIÇÃO
                        Text(
                            text = item.descricao,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(6.dp))

                        // 🔹 DATA
                        Text(
                            text = item.dataEvento,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                }
            }
        }
    }
}

// =======================================================
// ITEM DO PRODUTO (COMPLETO)
// =======================================================
@Composable
fun ProdutoItem(
    produto: Produto,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDoubleClick: () -> Unit
) {
    var lastTapTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current

    val dias = getDiasRestantes(produto.validadeAtual)

    val corFundo = when {
        dias == Int.MAX_VALUE -> Color(0xFFF5F5F5)
        dias < 0 -> Color.Red.copy(alpha = 0.10f)
        dias <= 2 -> Color(0xFFFFE0E0)
        dias <= 5 -> Color.Yellow.copy(alpha = 0.10f)
        else -> Color(0xFFE8F5E9)
    }

    val statusBadges = mutableListOf<Pair<String, Color>>()

    when (produto.status) {
        StatusProduto.AGUARDANDO_APROVACAO ->
            statusBadges += "Aguardando aprovação" to Color(0xFFFF9800)

        StatusProduto.TRABALHANDO_PRECO ->
            statusBadges += "Trabalhando preço" to Color(0xFFD32F2F)

        else -> {}
    }

    if (produto.emVerificacaoEstoque) {
        statusBadges += "Verificação estoque" to Color(0xFF1976D2)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp),
        colors = if (isSelected)
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        else
            CardDefaults.cardColors(containerColor = corFundo)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .pointerInput(produto.id) {
                    detectTapGestures(
                        onLongPress = { onLongPress() },
                        onTap = {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 220) onDoubleClick()
                            else onClick()
                            lastTapTime = now
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x22000000), RoundedCornerShape(8.dp))
                    .clickable {
                        if (!produto.fotoUrl.isNullOrBlank()) {
                            context.startActivity(
                                Intent(context, FullImageActivity::class.java)
                                    .putExtra("fotoUrl", produto.fotoUrl)
                            )
                        }
                    }
            ) {
                if (!produto.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = resolverImagem(produto.fotoUrl!!),
                        contentDescription = "Foto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.align(Alignment.Center).size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(Modifier.weight(1f)) {

                Text(
                    produto.descricao,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )

                Text(
                    "CB: ${produto.codigoBarras}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    "CI: ${produto.codigoInterno ?: "—"}",
                    style = MaterialTheme.typography.bodySmall
                )

                val total = produto.quantidadeAtual ?: 0
                val qpc = produto.quantidadePorCaixa ?: 0
                val estoqueTexto =
                    if (qpc > 0 && total > 0) {
                        val cx = total / qpc
                        val un = total % qpc
                        "$cx cx • $un un"
                    } else {
                        "$total un"
                    }

                Text(
                    "Estoque: $estoqueTexto",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    "Validade: ${produto.validadeAtual ?: "—"}",
                    style = MaterialTheme.typography.bodySmall
                )

                Row {
                    statusBadges.forEach { (texto, cor) ->
                        Text(
                            texto,
                            color = cor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
